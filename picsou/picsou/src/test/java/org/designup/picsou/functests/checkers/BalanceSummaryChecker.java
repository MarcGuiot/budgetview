package org.designup.picsou.functests.checkers;

import org.uispec4j.TextBox;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class BalanceSummaryChecker extends DataChecker {
  private Window window;

  public BalanceSummaryChecker(Window window) {
    this.window = window;
  }

  public MonthDetail initDetails() {
    return new MonthDetail(window);
  }

  public class MonthDetail {
    private Window panel;

    public MonthDetail(Window panel) {
      this.panel = panel;
    }

    public MonthDetail balance(double amount) {
      return check(amount, "detailBalance");
    }

    private MonthDetail check(double amount, String name) {
      TextBox textBox = panel.getTextBox(name);
      assertThat(textBox.textEquals(BalanceSummaryChecker.this.toString(amount)));
      return this;
    }

    public MonthDetail income(double amount) {
      return check(amount, "detailIncome");
    }

    public MonthDetail fixe(double amount) {
      return check(amount, "detailFixe");
    }

    public MonthDetail saving(double amount) {
      return check(amount, "detailSaving");
    }

    public MonthDetail total(double amount) {
      return check(amount, "detailTotal");
    }
  }
}