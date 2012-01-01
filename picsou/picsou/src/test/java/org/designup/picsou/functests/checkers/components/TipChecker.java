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

  public TipChecker checkText(final String text) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Assert.assertEquals(text, tip.getText());
      }
    });
    return this;
  }

  public TipChecker checkTextContains(final String text) {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        if (!tip.getText().contains(text)) {
          Assert.fail("Text '" + text + "' not found. Actual: \n" + tip.getText());
        }
      }
    });
    return this;
  }

  public TipChecker checkVisible() {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Assert.assertTrue("Tip not visible - text: " + tip.getText(), tip.isVisible());
      }
    });
    return this;
  }

  public void checkHidden() {
    UISpecAssert.assertThat(new Assertion() {
      public void check() {
        Assert.assertFalse("Tip not visible - text: " + tip.getText(), tip.isVisible());
      }
    });
  }
}
