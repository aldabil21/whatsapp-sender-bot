package com.wabot.tasks;

import com.wabot.components.UndecoratedAlert;
import com.wabot.jobs.Notifier;
import com.wabot.jobs.Sender;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SendTask extends Task<Boolean> {
    private final Notifier notifier;
    private final String message;
    private final ChromeDriver driver;
    private Sender.Types type = Sender.Types.CHAT_LIST;

    private int totalSent;

    public SendTask(String message, ChromeDriver driver, Notifier notifier) {
        this.message = message;
        this.driver = driver;
        this.notifier = notifier;
    }

    public void setType(Sender.Types type) {
        this.type = type;
    }

    @Override
    protected Boolean call() {
        if (type == Sender.Types.SAVED_LIST) {
            sendToSavedList();
        } else {
            // Default fallback to CHAT_LIST - won't check it
            sendToChatList();
        }
        return true;
    }

    private void sendToSavedList() {
        try {
            WebElement contactIcon = getContactsIcon();
            contactIcon.click();
            WebElement contactArea = driver.findElement(By.className("copyable-area"));
            WebElement contacts = contactArea.findElement(By.cssSelector("div[data-tab='4']")).findElement(By.xpath("./.."));

            // Get receivers
            Set<String> receivers = collectReceivers(contacts);
            // Execute sending
            sendToReceivers(receivers, true);

        } catch (Exception e) {
            Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, "أطلب مساعدة من الأدمن");
            alert.setHeaderText("خطأ في العثور على قائمة الأصدقاء");
            alert.showAndWait();
            cancel();
        }

    }

    private void sendToChatList() {
        WebElement sideList = driver.findElement(By.id("pane-side"));
        // Get receivers
        Set<String> receivers = collectReceivers(sideList);

        // Execute sending
        sendToReceivers(receivers, false);
    }

    private Set<String> collectReceivers(WebElement list) {
        long scrollHeight = getElementScrollHeight(list);
        long sidePaneHeight = getElementClientHeight(list);
        long currentHeight = 0;
        Set<String> receivers = new HashSet<>();
        OUTER:
        do {
            try {
                List<WebElement> names = list.findElements(By.cssSelector("div[role='gridcell'][aria-colindex='2'] span[title]"));
                for (WebElement name : names) {
                    if (isCancelled()) {
                        break OUTER;
                    }
                    receivers.add(name.getText());
                    updateMessage("تم العثور على " + receivers.size() + " مستلم");
                }
                currentHeight += sidePaneHeight * 0.8;
                scrollTopPaneTo(list, currentHeight);
                Thread.sleep(500);
            } catch (Exception e) {
                String msg = e.getMessage();
                if (e instanceof NoSuchSessionException) {
                    throw new RuntimeException(e);
                } else {
                    System.out.println("Receivers: " + msg);
                }
            }
        } while (currentHeight < scrollHeight);

        return receivers;
    }

    private void sendToReceivers(Set<String> receivers, boolean isSavedList) {
        int i = 1;
        for (String name : receivers) {
            if (isCancelled()) {
                break;
            }
            try {
                WebElement searchBox;
                // in saved list, we need query element each time due to DOM detachment of the side pane
                if (isSavedList) {
                    if (i > 1) {
                        WebElement contactIcon = getContactsIcon();
                        contactIcon.click();
                        Thread.sleep(500);
                    }
                    WebElement contactArea = driver.findElement(By.className("copyable-area"));
                    searchBox = contactArea.findElement(By.cssSelector("div[role='textbox']"));
                } else {
                    WebElement side = driver.findElement(By.id("side"));
                    searchBox = side.findElement(By.cssSelector("div[role='textbox']"));
                }
                // insert name
                searchBox.clear();
                insertTextInput(searchBox, name);
                Thread.sleep(500);
                // click on name
                WebElement row = driver.findElement(By.cssSelector("div[role='gridcell']"));
                row.click();
                Thread.sleep(200);
                // Fill message
                WebElement main = driver.findElement(By.id("main"));
                WebElement textBox = main.findElement(By.cssSelector("div[role='textbox']"));
                textBox.clear();
                insertTextInput(textBox, this.message);

                Thread.sleep(200);
                // last button is send button
                List<WebElement> buttons = main.findElements(By.tagName("button"));
                WebElement send = buttons.get(buttons.size() - 1);
                send.click();
                updateMessage("إرسال " + i + " من " + receivers.size());

                totalSent = i;
                i++;
                Thread.sleep(200);

            } catch (Exception e) {
                String msg = e.getMessage();
                if (e instanceof NoSuchElementException) {
                    System.out.println("No message text field - maybe its a broadcast?");
                } else if (e instanceof NoSuchSessionException) {
                    throw new RuntimeException(e);
                } else {
                    System.out.println("Sender: " + msg);
                }
            }
        }
    }

    private WebElement getContactsIcon() {
        WebElement side = driver.findElement(By.id("side"));
        WebElement header = side.findElement(By.tagName("header"));
        return header.findElement(By.cssSelector("div[role='button'] > span[data-icon='chat']"));
    }

    private long getElementScrollHeight(WebElement el) {
        return (long) ((JavascriptExecutor) driver).executeScript("return arguments[0].scrollHeight", el);
    }

    private long getElementClientHeight(WebElement el) {
        return (long) ((JavascriptExecutor) driver).executeScript("return arguments[0].clientHeight", el);
    }

    private void scrollTopPaneTo(WebElement element, long to) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTo(0, arguments[1])", element, to);
    }

    private void insertTextInput(WebElement element, String text) {
        element.sendKeys("1"); // important to show send button
        StringBuilder script = new StringBuilder("arguments[0].innerHTML = arguments[1];");
        script.append("arguments[0].dispatchEvent(new Event('keydown', {bubbles: true}));")
                .append("arguments[0].dispatchEvent(new Event('keypress', {bubbles: true}));")
                .append("arguments[0].dispatchEvent(new Event('input', {bubbles: true}));")
                .append("arguments[0].dispatchEvent(new Event('keyup', {bubbles: true}));");
        ((JavascriptExecutor) driver).executeScript(script.toString(), element, text);
    }

    @Override
    protected void succeeded() {
        this.notifier.closeNotif();
        this.driver.quit();
        Alert alert = new UndecoratedAlert(Alert.AlertType.INFORMATION, "مجموع ما تم إرساله: " + totalSent + " رسالة");
        alert.setHeaderText("تم الإنتهاء");
        alert.show();
    }

    @Override
    protected void failed() {
        this.driver.quit();
        this.notifier.closeNotif();
        Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, "تواصل مع الأدمن للمساعدة");
        alert.setHeaderText("حدث خطأ ما");
        alert.show();
    }

    @Override
    protected void cancelled() {
        this.driver.quit();
        this.notifier.closeNotif();
        StringBuilder sb = new StringBuilder("تم إلغاء المهمة");
        if (totalSent > 0) {
            sb.append(". تم إرسال ").append(totalSent).append(" رسالة");
        }
        Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, sb.toString());
        alert.setHeaderText("إلغاء المهمة");
        alert.show();
    }

}
