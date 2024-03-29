package com.budgetview.functests.checkers;

import com.budgetview.utils.Lang;
import org.uispec4j.Button;
import org.uispec4j.TextBox;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.io.File;

import static org.uispec4j.assertion.UISpecAssert.*;

public class ImportDialogChecker extends GuiChecker {
  private Window dialog;
  private TextBox fileField;
  private Button importButton;

  public static ImportDialogChecker open(Trigger trigger) {
    Window window = WindowInterceptor.getModalDialog(trigger);
    return new ImportDialogChecker(window);
  }

  public ImportDialogChecker(Window dialog) {
    this.dialog = dialog;
  }

  Window getDialog() {
    return dialog;
  }

  private ImportDialogChecker() {
  }

  public static ImportDialogChecker create(Window dialog) {
    ImportDialogChecker importDialog = new ImportDialogChecker();
    importDialog.dialog = dialog;
    return importDialog;
  }

  public ImportDialogChecker setFilePath(String text) {
    getFileField().setText(text, false);
    return this;
  }

  public ImportDialogChecker selectFiles(String... path) {
    StringBuilder builder = new StringBuilder();
    for (String file : path) {
      if (builder.length() != 0) {
        builder.append(";");
      }
      builder.append(file);
    }
    getFileField().setText(builder.toString());
    return this;
  }

  public ImportDialogChecker checkFileImportEnabled() {
    assertTrue(getImportButton().isEnabled());
    return this;
  }

  public ImportDialogChecker checkFileImportDisabled() {
    assertFalse(getImportButton().isEnabled());
    return this;
  }

  public ImportDialogPreviewChecker importFileAndPreview(String path) {
    setFilePath(path);
    return importFileAndPreview();
  }

  public ImportDialogPreviewChecker importFileAndPreview() {
    checkTitle("import.fileSelection.title");
    getImportButton().click();
    return new ImportDialogPreviewChecker(dialog);
  }

  public ImportDialogChecker importFileWithError() {
    checkTitle("import.fileSelection.title");
    getImportButton().click();
    checkTitle("import.fileSelection.title");
    return this;
  }

  public ImportDialogChecker importFileWithError(String messageKey, String... args) {
    checkTitle("import.fileSelection.title");
    getImportButton().click();
    checkTitle("import.fileSelection.title");
    checkErrorMessage(messageKey, args);
    return this;
  }

  public ImportDialogChecker importFileWithHtmlError(String messageKey, String... args) {
    checkTitle("import.fileSelection.title");
    getImportButton().click();
    checkTitle("import.fileSelection.title");
    checkHtmlErrorMessage(messageKey, args);
    return this;
  }

  public void checkTitle(String titleKey) {
    assertThat(dialog.getTextBox(ComponentMatchers.innerNameIdentity("title")).textEquals(Lang.get(titleKey)));
  }

  public ImportDialogChecker checkHeaderMessage(String text) {
    TextBox fileMessage = dialog.findUIComponent(TextBox.class, text);
    assertTrue(fileMessage.isVisible());
    return this;
  }

  public ImportDialogChecker checkNoErrorMessage() {
    TextBox message = (TextBox) dialog.findUIComponent(ComponentMatchers.innerNameIdentity("errorMessage"));
    if (message != null) {
      assertTrue(message.textIsEmpty());
    }
    return this;
  }

  public void close() {
    dialog.getButton(Lang.get("import.close")).click();
    checkClosed();
  }

  public void checkClosed() {
    assertFalse(dialog.isVisible());
  }

  public ImportDialogChecker checkErrorMessage(String message, String... arg) {
    assertTrue(dialog.getTextBox("errorMessage").textEquals(Lang.get(message, arg)));
    return this;
  }

  public ImportDialogChecker checkHtmlErrorMessage(String message, String... arg) {
    assertTrue(dialog.getTextBox("errorMessage").htmlEquals(Lang.get(message, arg)));
    return this;
  }

  public MessageAndDetailsDialogChecker clickErrorMessage() {
    return MessageAndDetailsDialogChecker.init(
      dialog.getTextBox("errorMessage").triggerClickOnHyperlink("Click here"));
  }

  public ImportDialogChecker checkCloseButton(String text) {
    Button close = dialog.getButton(Lang.get("close"));
    assertThat(close.textEquals(text));
    return this;
  }

  public ImportDialogPreviewChecker browseAndPreview(String path) {
    WindowInterceptor.init(dialog.getButton(Lang.get("browse")))
      .process(FileChooserHandler.init().select(new String[]{path}))
      .run();
    return new ImportDialogPreviewChecker(dialog);
  }

  public ImportDialogChecker checkDirectory(String directory) {
    WindowInterceptor.init(dialog.getButton(Lang.get("browse")))
      .process(FileChooserHandler.init().assertCurrentDirEquals(new File(directory)).cancelSelection())
      .run();
    return this;
  }

  public ImportDialogChecker checkMessageEmptyFile() {
    dialog.getTextBox(Lang.get("import.file.empty"));
    return this;
  }

  public void waitForPreview() {
    UISpecAssert.waitUntil(new Assertion() {
      private String expectedTitle = Lang.get("import.preview.title");

      public void check() {
        if (!dialog.containsLabel(expectedTitle).isTrue()) {
          UISpecAssert.fail("Current title is '" + dialog.getTextBox("title").getText()
                            + "' instead of '" + expectedTitle + "'");
        }
      }
    }, 10000);
  }


  public void importDeferred(String accountName, String fileName, boolean withMainAccount, String targetAccountName) {
    setFilePath(fileName)
      .importFileAndPreview()
      .importDeferred(accountName, fileName, withMainAccount, targetAccountName);
  }


  public CsvImporterChecker acceptCsvFile() {
    return new CsvImporterChecker(this, WindowInterceptor.getModalDialog(getImportButton().triggerClick()));
  }

  private TextBox getFileField() {
    if (fileField == null) {
      fileField = dialog.getInputTextBox("fileField");
    }
    return fileField;
  }

  private Button getImportButton() {
    if (importButton == null) {
      importButton = dialog.getButton("Import");
    }
    return importButton;
  }

  public void enterTransactionsManually() {
    dialog.getButton("manualEntry").click();
    checkClosed();
  }

  public CloudSignupChecker selectCloudForNewUser() {
    dialog.getPanel("cloudIntro").getButton("openCloudSynchro").click();
    return new CloudSignupChecker(dialog);
  }

  public CloudEditionChecker editCloudConnections() {
    dialog.getPanel("cloudIntro").getButton("editCloudConnections").click();
    return new CloudEditionChecker(dialog);
  }

  public ImportDialogChecker checkCloudRefreshNotVisible() {
    checkComponentVisible(dialog.getPanel("cloudIntro"), JPanel.class, "refresh", false);
    return this;
  }

  public ImportDialogPreviewChecker selectCloudRefresh() {
    doRefresh();
    return new ImportDialogPreviewChecker(dialog);
  }

  public CloudSubscriptionErrorChecker selectCloudRefreshAndGetSubscriptionError() {
    doRefresh();
    return new CloudSubscriptionErrorChecker(dialog);
  }

  public ImportDialogCompletionChecker selectCloudRefreshAndGetSummary() {
    doRefresh();
    return new ImportDialogCompletionChecker(dialog);
  }

  public CloudFirstDownloadChecker selectCloudRefreshAndGetFirstDowload() {
    doRefresh();
    return new CloudFirstDownloadChecker(dialog);
  }

  public void doRefresh() {
    checkComponentVisible(dialog.getPanel("cloudIntro"), JPanel.class, "refresh", true);
    dialog.getPanel("cloudIntro").getButton("refreshCloud").click();
  }

  public ImportDialogPreviewChecker toPreview() {
    return new ImportDialogPreviewChecker(dialog);
  }
}
