package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import net.java.balloontip.BalloonTip;
import org.designup.picsou.model.Month;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Dates;
import org.uispec4j.Key;
import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentMatcher;
import org.uispec4j.utils.KeyUtils;
import org.uispec4j.utils.Utils;

import javax.swing.*;
import java.awt.*;
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
      public void check() {
        Assert.assertEquals(visible, component != null && component.isVisible());
      }
    });
  }

  protected void checkSignpostVisible(Panel enclosingPanel,
                                      UIComponent targetUIComponent,
                                      final String text) {
    final BalloonTip balloon = getBalloonTip(enclosingPanel, targetUIComponent);
    UISpecAssert.assertThat("Signpost is not visible", new Assertion() {
      public void check() {
        Assert.assertTrue(balloon != null && balloon.isVisible());
      }
    });
    Assert.assertEquals(text, Utils.cleanupHtml(balloon.getText()));
  }

  protected void checkSignpostHidden(Panel enclosingPanel,
                                     UIComponent targetUIComponent) {
    final BalloonTip balloon = getBalloonTip(enclosingPanel, targetUIComponent);
    UISpecAssert.assertThat("Signpost is visible", new Assertion() {
      public void check() {
        Assert.assertTrue((balloon == null) || !balloon.isVisible());
      }
    });
  }

  private BalloonTip getBalloonTip(Panel enclosingPanel, UIComponent targetUIComponent) {
    final Component targetComponent = targetUIComponent.getAwtComponent();
    Window enclosingWindow = new Window(GuiUtils.getEnclosingFrame(enclosingPanel.getAwtComponent()));
    return (BalloonTip)enclosingWindow.findSwingComponent(new ComponentMatcher() {
      public boolean matches(Component component) {
        if (!BalloonTip.class.isAssignableFrom(component.getClass())) {
          return false;
        }
        BalloonTip balloon = (BalloonTip)component;
        return balloon.getAttachedComponent() == targetComponent;
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
