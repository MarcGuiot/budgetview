package org.designup.picsou.functests.utils;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import net.java.balloontip.BalloonTip;
import org.designup.picsou.functests.checkers.components.TipChecker;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Strings;
import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentFinder;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static org.uispec4j.finder.ComponentMatchers.*;

public class BalloonTipTesting {

  public static void checkBalloonTipVisible(Panel enclosingWindow,
                                            UIComponent targetUIComponent,
                                            String text) {
    checkBalloonTipVisible(enclosingWindow.getAwtComponent(),
                           targetUIComponent.getAwtComponent(),
                           text);
  }

  public static void checkBalloonTipVisible(final Component enclosingWindow,
                                            final Component targetUIComponent,
                                            final String text) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        final BalloonTip balloon = getBalloonTip(enclosingWindow, targetUIComponent);
        Assert.assertEquals(text, Utils.cleanupHtml(getText(balloon)));
        if (!targetUIComponent.isVisible()) {
          Assert.fail("Component is not visible");
        }
      }
    });
  }

  private static String getText(BalloonTip balloon) {
    return ((JLabel) balloon.getContents()).getText();
  }

  private static BalloonTip getBalloonTip(Component enclosingSwingPanel,
                                          Component targetSwingComponent) {

    org.uispec4j.Window window = getEnclosingWindow(enclosingSwingPanel);
    ComponentFinder finder = new ComponentFinder(window.getAwtContainer(), window);
    Component[] tips = finder.getComponents(ComponentMatchers.fromClass(BalloonTip.class));
    for (Component component : tips) {
      BalloonTip tip = (BalloonTip) component;
      if (tip.getAttachedComponent() == targetSwingComponent) {
        return tip;
      }
    }

    if (tips.length > 0) {
      throw new AssertionFailedError("No tip found for component " + targetSwingComponent + "\n" +
                                     "Shown tips:\n" + toString(tips));
    }
    throw new AssertionFailedError("No tips found in window: \n" + window.getAwtComponent());
  }

  private static String toString(Component[] tips) {
    StringBuilder builder = new StringBuilder();
    for (Component component : tips) {
      BalloonTip tip = (BalloonTip) component;
      if (!tip.isVisible()) {
        continue;
      }

      if (builder.length() > 0) {
        builder.append("\n");
      }
      builder.append(" - ").append(Utils.cleanupHtml(getText(tip))).append(" <== ").append(tip.getAttachedComponent());
    }
    return builder.toString();
  }

  private static org.uispec4j.Window getEnclosingWindow(Component enclosingPanel) {
    if (enclosingPanel instanceof java.awt.Window) {
      return new org.uispec4j.Window((java.awt.Window) enclosingPanel);

    }
    return new org.uispec4j.Window(GuiUtils.getEnclosingFrame(enclosingPanel));
  }

  public static void checkNoBalloonTipVisible(Panel panel) {
    checkNoBalloonTipVisible(panel.getAwtContainer());
  }

  public static void checkNoBalloonTipVisible(Container panel) {
    final Component[] actual = getBalloonTipComponents(panel);
    if (actual.length > 0) {
      StringBuilder builder = new StringBuilder("Visible tips:\n");
      for (Component component : actual) {
        BalloonTip tip = (BalloonTip) component;
        builder.append(Utils.cleanupHtml(getText(tip)));
        String componentName = tip.getAttachedComponent().getName();
        if (Strings.isNotEmpty(componentName)) {
          builder.append(" (component: ").append(componentName).append(")");
        }
        builder.append("\n");
      }
      Assert.fail(builder.toString());
    }
  }

  private static Component[] getBalloonTipComponents(Container panel) {
    ComponentFinder finder = new ComponentFinder(panel, new Panel(panel));
    return finder.getComponents(and(fromClass(BalloonTip.class)));
  }

  public static void checkSingleBalloonVisible(Panel enclosingPanel) {
    Component[] tips = getBalloonTipComponents(enclosingPanel.getAwtComponent());
    if (tips.length == 0) {
      Assert.fail("No tooltip shown");
    }
    else if (tips.length > 1) {
      java.util.List<String> labels = new ArrayList<String>();
      for (Component component : tips) {
        BalloonTip tip = (BalloonTip) component;
        labels.add(((JLabel) tip.getContents()).getText());
      }
      Assert.fail(tips.length + " tips shown : " + labels);
    }
  }

  public static TipChecker getTip(Panel enclosingWindow,
                                  UIComponent targetUIComponent) {
    BalloonTip tip = getBalloonTip(enclosingWindow.getAwtComponent(),
                                   targetUIComponent.getAwtComponent());
    return new TipChecker(tip);
  }
}
