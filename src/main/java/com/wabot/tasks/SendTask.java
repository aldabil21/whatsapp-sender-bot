package com.wabot.tasks;

import com.wabot.components.UndecoratedAlert;
import com.wabot.jobs.Notifier;
import com.wabot.jobs.Sender;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SendTask extends Task<Boolean> {
    private final ChromeDriver driver;
    private final Notifier notifier;
    private final Sender.Types type;
    private final String message;
    private final File media;
    /**
     * Only used in type = Types.LABELED_LIST
     */
    private final String labelName;
    private final WebDriverWait wait;
    private int totalFound;
    private int totalSent;

    public SendTask(ChromeDriver driver, Notifier notifier, Sender.Types type, String message, File media, String labelName) {
        this.driver = driver;
        this.notifier = notifier;
        this.type = type;
        this.message = message;
        this.media = media;
        this.labelName = labelName;

        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        if (media != null) {
            putMediaInClipboard();
        }
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
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[role='gridcell'][aria-colindex='2'] span[title]")));
        WebElement sideList = driver.findElement(By.id("pane-side"));
        // Get receivers
        Set<String> receivers = collectReceivers(sideList);
        // Execute sending
        sendToReceivers(receivers, false);
    }

    private void sendToSavedList() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[role='gridcell'][aria-colindex='2'] span[title]")));

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
            Thread.sleep(3000);
            WebElement labelsArea = driver.findElement(By.className("copyable-area"));
            WebElement label = labelsArea.findElement(By.cssSelector("div[role='gridcell'] span[title='" + labelName + "']"));
            label.click();
            Thread.sleep(500);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-animate-drawer-title]")));
        } catch (Exception e) {
            throw new RuntimeException("لم يتم العثور على التصنيف " + labelName + ". تأكد أن إسم التصنيف مطابق تماما");
        }

        // Get receivers
        WebElement sideList = driver.findElement(By.id("pane-side"));
        Set<String> receivers = collectReceivers(sideList);

        try {
            // Go back - need to select back button each time since it gets detached from DOM
            WebElement back = getLabelsBackButton();
            back.click();
            Thread.sleep(500);
            back = getLabelsBackButton();
            back.click();
            Thread.sleep(500);
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

        // Execute send for numbers
        sendToReceivers(numbers, false);

        // Execute send for names
        sendToReceivers(names, true);

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
                if (e instanceof WebDriverException) {
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
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-animate-drawer-title]")));
                    }
                    WebElement contactArea = driver.findElement(By.className("copyable-area"));
                    searchBox = contactArea.findElement(By.cssSelector("div[role='textbox']"));
                } else {
                    WebElement side = driver.findElement(By.id("side"));
                    searchBox = side.findElement(By.cssSelector("div[role='textbox']"));
                }
                // insert name
                searchBox.clear();
                insertTextInput(searchBox, name); //"+966 50 748 7620"); //
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[data-icon='x-alt']")));
                Thread.sleep(200);
                // click on name - first instance
                List<WebElement> row = driver.findElements(By.cssSelector("div[role='gridcell'] span[title]"));
                row.get(0).click();

                // Fill message
                Thread.sleep(200);
                WebElement main = driver.findElement(By.id("main"));
                WebElement textBox = main.findElement(By.cssSelector("div[role='textbox']"));
                textBox.clear();

                // Check if message is with media
                WebElement sendButton;
                if (media != null) {
                    // Will copy to clipboard everytime in the loop
                    // in case the user worked in other apps and copied something
                    putMediaInClipboard();
                    textBox.sendKeys(Keys.CONTROL + "v");
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[data-icon='emoji-input']")));
                    Thread.sleep(500);
                    WebElement middleArea = driver.findElement(By.className("copyable-area"));
                    // if media != null  - message could be null
                    if (this.message != null) {
                        WebElement mediaTextBox = middleArea.findElement(By.cssSelector("div[role='textbox']"));
                        insertTextInput(mediaTextBox, this.message);
                    }
                    Thread.sleep(200);
                    List<WebElement> buttons = middleArea.findElements(By.cssSelector("div[role='button']"));
                    sendButton = buttons.get(buttons.size() - 1);
                } else {
                    insertTextInput(textBox, this.message);
                    Thread.sleep(200);
                    // last button is send button
                    List<WebElement> buttons = main.findElements(By.tagName("button"));
                    sendButton = buttons.get(buttons.size() - 1);
                }

                sendButton.click();

                totalSent++;
                updateMessage("إرسال " + totalSent + " من " + totalFound);
                Thread.sleep(500);
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
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[role='button'] > span[data-icon='chat']")));
        WebElement side = driver.findElement(By.id("side"));
        WebElement header = side.findElement(By.tagName("header"));
        return header.findElement(By.cssSelector("div[role='button'] > span[data-icon='chat']"));
    }

    private void goToLabelsList() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[role='gridcell'][aria-colindex='2'] span[title]")));
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[role='button'] > span[data-icon='menu']")));
            WebElement side = driver.findElement(By.id("side"));
            WebElement header = side.findElement(By.tagName("header"));
            WebElement menu = header.findElement(By.cssSelector("div[role='button'] > span[data-icon='menu']"));
            menu.click();
            Thread.sleep(1000);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("li")));
            List<WebElement> menuList = header.findElements(By.tagName("li"));

            if (menuList.size() != 7) {
                throw new RuntimeException("It seems that this is not a Whatsapp business version");
            }

            // Labels is the fifth item
            menuList.get(4).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-animate-drawer-title]")));
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

    private void putMediaInClipboard() {
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putFiles(Collections.singletonList(media));
            clipboard.setContent(content);
        });
    }

    /**
     * This is needed cuz Whatsapp does not use semantic HTML input field,
     * so will need to trick the "div[role=textbox]" to simulate real input event
     *
     * @param element The textbox
     * @param text    the string to insert
     */
    private void insertTextInput(WebElement element, String text) {
        element.sendKeys("000000000000"); // important to show send button
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
