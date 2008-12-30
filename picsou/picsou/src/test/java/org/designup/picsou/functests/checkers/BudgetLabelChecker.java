package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.globsframework.utils.Strings;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.utils.Utils;

public class BudgetLabelChecker extends GuiChecker {
  private Panel mainWindow;

  public BudgetLabelChecker(Panel mainWindow) {
    this.mainWindow = mainWindow;
  }

  public BudgetLabelChecker checkMulti(int i) {
    checkContains(i + " months total", true);
    return this;
  }

  public BudgetLabelChecker checkMultiNotShown() {
    checkContains("months total", false);
    return this;
  }

  public BudgetLabelChecker checkMonthBalance(double amount) {
    checkContains("Balance: " + toString(amount, true), true);
    return this;
  }

  public BudgetLabelChecker checkEndBalance(double amount) {
    checkContains("End balance: " + toString(amount, false), true);
    return this;
  }

  public BudgetLabelChecker checkUncategorized(double value) {
    checkContains("Uncategorized: " + toString(value, false), true);
    return this;
  }

  public BudgetLabelChecker checkUncategorizedNotShown() {
    checkContains("Uncategorized:", false);
    return this;
  }

  private String getLabelText() {
    TextBox label = mainWindow.getTextBox("budgetLabel");
    return Utils.cleanupHtml(label.getText());
  }

  protected String toString(double value, boolean forcePlus) {
    if (value == 0) {
      return "0";
    }
    return super.toString(value, forcePlus);
  }

  private void checkContains(final String text, final boolean expected) {
    UISpecAssert.assertEquals(expected, new Assertion() {
      public void check() throws Exception {
        String labelText = getLabelText();
        if (!labelText.contains(text)) {
          Assert.fail("Actual content: " + labelText);
        }
      }
    });
  }

  public void checkEmpty() {
    UISpecAssert.assertThat(new Assertion() {
      public void check() throws Exception {
        String text = getLabelText();
        if (!Strings.isNullOrEmpty(text)) {
          Assert.fail("Actual content: " + text);
        }
      }
    });
  }
}
