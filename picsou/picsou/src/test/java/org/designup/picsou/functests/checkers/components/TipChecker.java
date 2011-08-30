package org.designup.picsou.functests.checkers.components;

import junit.framework.Assert;
import net.java.balloontip.BalloonTip;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;

public class TipChecker {
  private BalloonTip tip;

  public TipChecker(BalloonTip tip) {
    this.tip = tip;
  }

  public void checkText(final String text) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Assert.assertEquals(text, tip.getText());
      }
    });
  }

  public void checkVisible() {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Assert.assertTrue("Tip not visible - text: " + tip.getText(), tip.isVisible());
      }
    });
  }

  public void checkHidden() {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Assert.assertFalse("Tip not visible - text: " + tip.getText(), tip.isVisible());
      }
    });
  }
}
