package org.designup.picsou.functests.utils;

import junit.framework.Assert;
import net.java.balloontip.BalloonTip;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Strings;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.finder.ComponentFinder;
import org.uispec4j.finder.ComponentMatcher;
import org.uispec4j.utils.Utils;

import java.awt.*;

import static org.uispec4j.finder.ComponentMatchers.*;

public class BalloonTipTesting {

  public static void checkBalloonTipVisible(Panel enclosingWindow,
                                            UIComponent targetUIComponent,
                                            String text,
                                            String message) {
    checkBalloonTipVisible(enclosingWindow.getAwtComponent(),
                           targetUIComponent.getAwtComponent(),
                           text,
                           message);
  }

  public static void checkBalloonTipVisible(final Component enclosingWindow,
                                            final Component targetUIComponent,
                                            String text,
                                            String message) {
    UISpecAssert.assertThat(message, new Assertion() {
      public void check() {
        final BalloonTip balloon = getBalloonTip(enclosingWindow, targetUIComponent);
        Assert.assertTrue(balloon != null && balloon.isVisible());
      }
    });
    final BalloonTip balloon = getBalloonTip(enclosingWindow, targetUIComponent);
    Assert.assertEquals(text, Utils.cleanupHtml(balloon.getText()));
  }

  private static BalloonTip getBalloonTip(Component enclosingPanel, Component targetUIComponent) {
    final Component targetComponent = targetUIComponent;
    org.uispec4j.Window enclosingWindow = getEnclosingWindow(enclosingPanel);
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

  private static org.uispec4j.Window getEnclosingWindow(Component enclosingPanel) {
    if (enclosingPanel instanceof java.awt.Window) {
      return new org.uispec4j.Window((java.awt.Window)enclosingPanel);

    }
    return new org.uispec4j.Window(GuiUtils.getEnclosingFrame(enclosingPanel));
  }

  public static void checkNoBalloonTipVisible(Panel panel) {
    checkNoBalloonTipVisible(panel.getAwtContainer());
  }

  public static void checkNoBalloonTipVisible(Container panel) {
    ComponentFinder finder = new ComponentFinder(panel, new Panel(panel));
    final Component[] actual = finder.getComponents(and(fromClass(BalloonTip.class),
                                                        visible(true)));
    if (actual.length > 0) {
      StringBuilder builder = new StringBuilder("Visible tips:\n");
      for (Component component : actual) {
        BalloonTip tip = (BalloonTip)component;
        builder.append(Utils.cleanupHtml(tip.getText()));
        String componentName = tip.getAttachedComponent().getName();
        if (Strings.isNotEmpty(componentName)) {
          builder.append(" (component: ").append(componentName).append(")");
        }
        builder.append("\n");
      }
      Assert.fail(builder.toString());
    }
  }
}
