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
    /**
     * Only used in type = Types.LABELED_LIST
     */
    private String labelName;
    private int totalFound;
    private int totalSent;

    public SendTask(String message, ChromeDriver driver, Notifier notifier) {
        this.message = message;
        this.driver = driver;
        this.notifier = notifier;
    }

    public void setType(Sender.Types type, String labelName) {
        this.type = type;
        this.labelName = labelName;
    }

    @Override
    protected Boolean call() {
        if (type == Sender.Types.SAVED_LIST) {
            sendToSavedList();
        } else if (type == Sender.Types.LABELED_LIST) {
            sendToLabelList();
        } else {
            // Default fallback to CHAT_LIST - won't check it
            sendToChatList();
        }
        return true;
    }

    private void sendToChatList() {
        WebElement sideList = driver.findElement(By.id("pane-side"));
        // Get receivers
        Set<String> receivers = collectReceivers(sideList);
        // Execute sending
        sendToReceivers(receivers, false);
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

    private void sendToLabelList() {
        // Go to labels
        goToLabelsList();

        // Find label if exists
        try {
            WebElement labelsArea = driver.findElement(By.className("copyable-area"));
            WebElement label = labelsArea.findElement(By.cssSelector("div[role='gridcell'] span[title='" + labelName + "']"));
            label.click();
            Thread.sleep(500);
        } catch (Exception e) {
            throw new RuntimeException("لم يتم العثور على التصنيف " + labelName + ". تأكد أن إسم التصنيف مطابق تماما");
        }

        // Get receivers
        WebElement sideList = driver.findElement(By.id("pane-side"));
        Set<String> receivers = collectReceivers(sideList);
        System.out.println(receivers.size());
        System.out.println(receivers);

        try {
            // Go back - need to select back button each time since it gets detached from DOM
            WebElement back = getLabelsBackButton();
            back.click();
            Thread.sleep(200);
            back = getLabelsBackButton();
            back.click();
            Thread.sleep(200);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        // Separate numbers from names
        // numbers will use saveList searchBox
        // names will use chatList searchBox
        Set<String> numbers = new HashSet<>();
        Set<String> names = new HashSet<>();
        receivers.forEach(s -> {
            if (isPhoneNumber(s)) {
                numbers.add(s);
            } else {
                names.add(s);
            }
        });

        try {
            // Execute send for numbers
            sendToReceivers(numbers, false);
            // Execute send for names
//            WebElement contactIcon = getContactsIcon();
//            contactIcon.click();
//            Thread.sleep(200);
            sendToReceivers(names, true);
        } catch (Exception e) {
            // just for the sleep
        }

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

        totalFound = receivers.size();

        return receivers;
    }

    private void sendToReceivers(Set<String> receivers, boolean isSavedList) {

        for (String name : receivers) {
            if (isCancelled()) {
                break;
            }
            try {
                WebElement searchBox;
                // in saved list, we need query element each time due to DOM detachment of the side pane
                if (isSavedList) {
                    if (totalSent > 0) {
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
//                textBox.sendKeys(Keys.CONTROL + "v");
                insertTextInput(textBox, this.message);

                Thread.sleep(200);
                // last button is send button
                List<WebElement> buttons = main.findElements(By.tagName("button"));
                WebElement send = buttons.get(buttons.size() - 1);
                //send.click();

                totalSent++;
                updateMessage("إرسال " + totalSent + " من " + totalFound);
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

    private void goToLabelsList() {
        // Menu seems to be loading slow
        // I think whatsapp checks for version before fetching menu
        // so here will make things pretty slow
        // it's a single action anyway, not repetitive
        try {
            Thread.sleep(2000);
            WebElement side = driver.findElement(By.id("side"));
            WebElement header = side.findElement(By.tagName("header"));
            WebElement menu = header.findElement(By.cssSelector("div[role='button'] > span[data-icon='menu']"));
            menu.click();
            // If we enter labels menu so fast, chatList rows may end up in label as well
            Thread.sleep(2000);
            List<WebElement> menuList = header.findElements(By.tagName("li"));
            // Labels is the fifth item
            menuList.get(4).click();
            Thread.sleep(1000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("خطأ في العثور على قائمة التصنيفات");
        }

    }

    private long getElementScrollHeight(WebElement el) {
        return (long) ((JavascriptExecutor) driver).executeScript("return arguments[0].scrollHeight", el);
    }

    private long getElementClientHeight(WebElement el) {
        return (long) ((JavascriptExecutor) driver).executeScript("return arguments[0].clientHeight", el);
    }

    private WebElement getLabelsBackButton() {
        return driver.findElement(By.cssSelector("button > span[data-icon='back']")).findElement(By.xpath("./.."));
    }

    private void scrollTopPaneTo(WebElement element, long to) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollTo(0, arguments[1])", element, to);
    }

    /**
     * This is needed cuz Whatsapp does not use semantic HTML input field,
     * so will need to trick the "div[role=textbox]" to simulate real input event
     *
     * @param element The textbox
     * @param text    the string to insert
     */
    private void insertTextInput(WebElement element, String text) {
        element.sendKeys("1"); // important to show send button
        String script = "arguments[0].innerHTML = arguments[1];" + "arguments[0].dispatchEvent(new Event('keydown', {bubbles: true}));" +
                "arguments[0].dispatchEvent(new Event('keypress', {bubbles: true}));" +
                "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));" +
                "arguments[0].dispatchEvent(new Event('keyup', {bubbles: true}));";
        ((JavascriptExecutor) driver).executeScript(script, element, text);
    }

    private boolean isPhoneNumber(String string) {
        try {
            // remove the first +
            String number = string.substring(1).replaceAll(" ", "");
            Long.parseLong(number);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void succeeded() {
        this.notifier.closeNotif();
        this.driver.quit();
        Alert alert = new UndecoratedAlert(Alert.AlertType.INFORMATION, "مجموع ما تم إرساله: " + totalSent + " رسالة");
        alert.setHeaderText("تمت المهمة بنجاح");
        alert.show();
    }

    @Override
    protected void failed() {
        this.driver.quit();
        this.notifier.closeNotif();
        String message = getException().getMessage();
        if (message == null) {
            message = "تواصل مع الأدمن للمساعدة";
        }
        Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, message);
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
