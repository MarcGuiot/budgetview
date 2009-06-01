package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Key;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.utils.KeyUtils;
import org.globsframework.utils.Dates;

import javax.swing.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public abstract class GuiChecker {
  private static DecimalFormat format = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));

  protected String toString(Double value) {
    if (value == null) {
      return "";
    }
    return format.format(value);
  }

  protected String toString(double value) {
    return format.format(value);
  }

  protected String toString(double value, boolean forcePlus) {
    String prefix = (forcePlus && value > 0) ? "+" : "";
    return prefix + format.format(value);
  }

  protected <T extends JComponent> void checkComponentVisible(Panel panel,
                                                              Class<T> swingComponentClass,
                                                              String componentName,
                                                              final boolean visible) {
    final JComponent component = panel.findSwingComponent(swingComponentClass, componentName);
    UISpecAssert.assertThat(visible ? "is not visible" : "is visible", new Assertion() {
      public void check() throws Exception {
        Assert.assertEquals(visible, component != null && component.isVisible());
      }
    });
  }

  protected void pressEsc(final Window dialog) {
    final JDialog jDialog = (JDialog)dialog.getAwtComponent();
    KeyUtils.pressKey(jDialog.getRootPane(), Key.ESCAPE);
  }

  protected int parseMonthId(String date) {
    return Month.getMonthId(Dates.parseMonth(date));
  }
}
