package com.wabot.tasks;

import com.wabot.components.UndecoratedAlert;
import com.wabot.jobs.Notifier;
import com.wabot.jobs.Sender;
import com.wabot.model.ExcelRow;
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

    private List<ExcelRow> excelList;

    /**
     * Excel list constructor
     */
    public SendTask(ChromeDriver driver, Notifier notifier, List<ExcelRow> excelList) {
        this(driver, notifier, Sender.Types.EXCEL_LIST, null, null, null);
        this.excelList = excelList;
        this.totalFound = excelList.size();
    }

    public SendTask(ChromeDriver driver, Notifier notifier, Sender.Types type, String message, File media,
            String labelName) {
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
        if (type == Sender.Types.EXCEL_LIST) {
            sendToExcelList();
        } else if (type == Sender.Types.SAVED_LIST) {
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
        wait.until(ExpectedConditions
                .elementToBeClickable(By.cssSelector("div[role='gridcell'][aria-colindex='2'] span[title]")));
        WebElement sideList = driver.findElement(By.id("pane-side"));
        // Get receivers
        Set<String> receivers = collectReceivers(sideList);
        // Execute sending
        sendToReceivers(receivers, false);
    }

    private void sendToSavedList() {
        try {
            wait.until(ExpectedConditions
                    .elementToBeClickable(By.cssSelector("div[role='gridcell'][aria-colindex='2'] span[title]")));

            WebElement contactIcon = getContactsIcon();
            contactIcon.click();
            WebElement contactArea = driver.findElement(By.className("copyable-area"));
            WebElement contacts = contactArea.findElement(By.cssSelector("div[data-tab='4']"))
                    .findElement(By.xpath("./.."));

            // Get receivers
            Set<String> receivers = collectReceivers(contacts);
            // Execute sending
            sendToReceivers(receivers, true);

        } catch (Exception e) {
            Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, "???????? ???????????? ???? ????????????");
            alert.setHeaderText("?????? ???? ???????????? ?????? ?????????? ????????????????");
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
            WebElement label = labelsArea
                    .findElement(By.cssSelector("div[role='gridcell'] span[title='" + labelName + "']"));
            label.click();
            Thread.sleep(500);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-animate-drawer-title]")));
        } catch (Exception e) {
            throw new RuntimeException("???? ?????? ???????????? ?????? ?????????????? " + labelName + ". ???????? ???? ?????? ?????????????? ?????????? ??????????");
        }

        // Get receivers
        WebElement sideList = driver.findElement(By.id("pane-side"));
        Set<String> receivers = collectReceivers(sideList);

        try {
            // Go back - need to select back button each time since it gets detached from
            // DOM
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

    private void sendToExcelList() {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
        for (ExcelRow row : excelList) {
            try {
                String number = row.getNumber();
                String message = row.getMessage();
                insertClickableAnchor(number);

                wait.until(ExpectedConditions.elementToBeClickable(By.id("excel-list-anchor")));
                WebElement anchor = driver.findElement(By.id("excel-list-anchor"));
                anchor.click();

                // Wait for loader to finish
                shortWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("div[role='dialog']")));

                // Double check in case missed
                WebElement dialog = numberNotExists();
                if (dialog != null) {
                    throw new RuntimeException();
                }
                // Fill message
                WebElement main = driver.findElement(By.id("main"));
                WebElement textBox = main.findElement(By.cssSelector("div[role='textbox']"));
                textBox.clear();

                // last button is send button
                insertTextInput(textBox, message);
                List<WebElement> buttons = main.findElements(By.tagName("button"));
                WebElement sendButton = buttons.get(buttons.size() - 1);

                sendButton.click();
                wait.until(
                        ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("span[data-icon='msg-time']")));

                totalSent++;
                updateMessage("?????????? " + totalSent + " ???? " + totalFound);

            } catch (Exception e) {
                if (e instanceof TimeoutException) {
                    WebElement dialog = numberNotExists();
                    if (dialog != null) {
                        dialog.findElement(By.cssSelector("div[role='dialog'] div[role='button']")).click();
                    }
                } else if (e instanceof NoSuchSessionException) {
                    throw new RuntimeException(e);
                } else {
                    System.out.println("Sender: " + e.getMessage());
                }
            }
        }
    }

    private Set<String> collectReceivers(WebElement list) {
        long scrollHeight = getElementScrollHeight(list);
        long sidePaneHeight = getElementClientHeight(list);
        long currentHeight = 0;

        Set<String> receivers = new HashSet<>();
        OUTER: do {
            try {
                List<WebElement> names = list
                        .findElements(By.cssSelector("div[role='gridcell'][aria-colindex='2'] span[title]"));
                for (WebElement name : names) {
                    if (isCancelled()) {
                        break OUTER;
                    }
                    // Check for updated only during collecting numbers/names
                    updateBrowserIfNeeded();

                    receivers.add(name.getText());
                    updateMessage("???? ???????????? ?????? " + receivers.size() + " ??????????");
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
                // in saved list, we need query element each time due to DOM detachment of the
                // side pane
                if (isSavedList) {
                    if (totalSent > 0) {
                        WebElement contactIcon = getContactsIcon();
                        contactIcon.click();
                        wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector("div[data-animate-drawer-title]")));
                    }
                    WebElement contactArea = driver.findElement(By.className("copyable-area"));
                    searchBox = contactArea.findElement(By.cssSelector("div[role='textbox']"));
                } else {
                    WebElement side = driver.findElement(By.id("side"));
                    searchBox = side.findElement(By.cssSelector("div[role='textbox']"));
                }
                // insert name
                searchBox.clear();
                insertTextInput(searchBox, name); // "+966 50 748 7620"); //
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[data-icon='x-alt']")));
                Thread.sleep(200);
                // click on name - first instance
                WebElement row = driver.findElement(By.cssSelector("div[role='gridcell'] span[title='" + name + "']"));
                row.click();

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
                    wait.until(
                            ExpectedConditions.elementToBeClickable(By.cssSelector("span[data-icon='emoji-input']")));
                    Thread.sleep(200);
                    WebElement middleArea = driver.findElement(By.className("copyable-area"));
                    // if media != null - message could be null
                    if (this.message != null) {
                        WebElement mediaTextBox = middleArea.findElement(By.cssSelector("div[role='textbox']"));
                        insertTextInput(mediaTextBox, this.message);
                    }
                    List<WebElement> buttons = middleArea.findElements(By.cssSelector("div[role='button']"));
                    sendButton = buttons.get(buttons.size() - 1);
                } else {
                    insertTextInput(textBox, this.message);
                    // last button is send button
                    List<WebElement> buttons = main.findElements(By.tagName("button"));
                    sendButton = buttons.get(buttons.size() - 1);
                }

                sendButton.click();
                wait.until(
                        ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("span[data-icon='msg-time']")));

                totalSent++;
                updateMessage("?????????? " + totalSent + " ???? " + totalFound);
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
        wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("div[role='button'] > span[data-icon='chat']")));
        WebElement side = driver.findElement(By.id("side"));
        WebElement header = side.findElement(By.tagName("header"));
        return header.findElement(By.cssSelector("div[role='button'] > span[data-icon='chat']"));
    }

    private void goToLabelsList() {
        try {
            // wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[role='gridcell'][aria-colindex='2']
            // span[title]")));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("side")));
            WebElement side = driver.findElement(By.id("side"));
            wait.until(ExpectedConditions.visibilityOf(side.findElement(By.tagName("header"))));
            WebElement header = side.findElement(By.tagName("header"));
            // Whatsapp header menu is flickry!!
            Thread.sleep(2000);
            List<WebElement> menu = header.findElements(By.cssSelector("div[role='button']"));
            WebElement menuItem = menu.get(menu.size() - 1);
            wait.until(ExpectedConditions.elementToBeClickable(menuItem));
            menuItem.click();
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li > div[role='button']")));

            List<WebElement> menuList = header.findElements(By.cssSelector("li > div[role='button']"));

            if (menuList.size() != 7) {
                throw new RuntimeException("???????? ???? ???????? ???????????????? ???????? ???????????? ??????????");
            }

            // Labels is the fifth item
            menuList.get(4).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-animate-drawer-title]")));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage()); // "?????? ???? ???????????? ?????? ?????????? ??????????????????");
        }

    }

    private void insertClickableAnchor(String number) {
        try {
            String script = "const el = document.getElementById('excel-list-anchor');" +
                    "if (el) { el.remove(); }" +
                    "document.querySelector('.app-wrapper-web').insertAdjacentHTML('afterbegin', `<a id='excel-list-anchor' href='https://api.whatsapp.com/send?phone=${arguments[0]}'>${arguments[0]}</a>`);";
            ((JavascriptExecutor) driver).executeScript(script, number);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //
        }
    }

    private WebElement numberNotExists() {
        try {
            return driver.findElement(By.cssSelector("div[role=dialog]"));
        } catch (Exception e) {
            return null;
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

    private void updateBrowserIfNeeded() {
        try {
            WebElement updater = driver.findElement(By.cssSelector("span[data-icon='alert-update']"));
            updater.click();
            WebDriverWait longWait = new WebDriverWait(driver, Duration.ofSeconds(30));
            longWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("side")));
        } catch (Exception e) {
            // Nothing to do unless it's a timeout after update
            if (e instanceof TimeoutException) {
                throw new RuntimeException("?????? ???? ?????????? ?????????????????? ??????????????");
            }
        }
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
        String script = "arguments[0].innerHTML = arguments[1];"
                + "arguments[0].dispatchEvent(new Event('keydown', {bubbles: true}));" +
                "arguments[0].dispatchEvent(new Event('keypress', {bubbles: true}));" +
                "arguments[0].dispatchEvent(new Event('input', {bubbles: true}));" +
                "arguments[0].dispatchEvent(new Event('keyup', {bubbles: true}));";
        ((JavascriptExecutor) driver).executeScript(script, element, text);
        // wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span[data-icon='x-alt']")));
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
        Alert alert = new UndecoratedAlert(Alert.AlertType.INFORMATION, "?????????? ???? ???? ????????????: " + totalSent + " ??????????");
        alert.setHeaderText("?????? ???????????? ??????????");
        alert.show();
    }

    @Override
    protected void failed() {
        this.driver.quit();
        this.notifier.closeNotif();
        String message = getException().getMessage();
        if (message == null) {
            message = "?????????? ???? ???????????? ????????????????";
        }
        Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, message);
        alert.setHeaderText("?????? ?????? ????");
        alert.show();
    }

    @Override
    protected void cancelled() {
        this.driver.quit();
        this.notifier.closeNotif();
        StringBuilder sb = new StringBuilder("???? ?????????? ????????????");
        if (totalSent > 0) {
            sb.append(". ???? ?????????? ").append(totalSent).append(" ??????????");
        }
        Alert alert = new UndecoratedAlert(Alert.AlertType.WARNING, sb.toString());
        alert.setHeaderText("?????????? ????????????");
        alert.show();
    }

}
