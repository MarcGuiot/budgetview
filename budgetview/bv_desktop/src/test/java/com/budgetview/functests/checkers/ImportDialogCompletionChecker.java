package com.budgetview.functests.checkers;

import com.budgetview.utils.Lang;
import junit.framework.Assert;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.finder.ComponentMatchers;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class ImportDialogCompletionChecker extends DialogChecker {

  public ImportDialogCompletionChecker(Window dialog) {
    super(dialog);
    checkTitle("import.completion.title");
  }

  public static void complete(int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount, Panel dialogToClose) {
    CompletionChecker handler = new CompletionChecker(importedTransactionCount, ignoredTransactionCount, autocategorizedTransactionCount);
    handler.checkAndClose(dialogToClose);
  }

  public void validate() {
    doClose(dialog);
  }

  private static void doClose(Panel dialog) {
    dialog.getButton(Lang.get("import.completion.button")).click();
    assertFalse(dialog.isVisible());
  }

  public void cancel() {
    dialog.getButton("cancel").click();
    assertFalse(dialog.isVisible());
  }

  public static class CompletionChecker {
    private final int importedTransactionCount;
    private final int ignoredTransactionCount;
    private final int autocategorizedTransactionCount;

    public CompletionChecker(int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount) {
      this.importedTransactionCount = importedTransactionCount;
      this.ignoredTransactionCount = ignoredTransactionCount;
      this.autocategorizedTransactionCount = autocategorizedTransactionCount;
    }

    private String toSummaryString(String imported, String ignored, String autocategorized) {
      return "imported:" + imported + "  - ignored:" + ignored + "  - autocategorized:" + autocategorized;
    }

    public void checkAndClose(Panel dialogToClose) {
      DialogChecker.checkTitle(dialogToClose, "import.completion.title");
      if (importedTransactionCount != -1) {
        Panel content = dialogToClose.getPanel("labels");
        Assert.assertEquals(toSummaryString(Integer.toString(importedTransactionCount),
                                            Integer.toString(ignoredTransactionCount),
                                            Integer.toString(autocategorizedTransactionCount)),
                            toSummaryString(content.getTextBox("importedCount").getText(),
                                            content.getTextBox("ignoredCount").getText(),
                                            content.getTextBox("categorizedCount").getText()));
      }
      doClose(dialogToClose);
    }
  }

  public void checkSummaryAndValidate(int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransactionCount) {
    ImportDialogCompletionChecker.CompletionChecker handler =
      new CompletionChecker(importedTransactionCount, ignoredTransactionCount, autocategorizedTransactionCount);
    handler.checkAndClose(dialog);
  }

  public ImportDialogCompletionChecker checkLastStep() {
    checkTitle("import.completion.title");
    return this;
  }
}
