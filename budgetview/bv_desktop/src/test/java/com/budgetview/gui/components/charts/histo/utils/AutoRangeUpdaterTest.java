package com.budgetview.gui.components.charts.histo.utils;

import junit.framework.TestCase;

public class AutoRangeUpdaterTest extends TestCase {
  public void test() throws Exception {
    checkWidth(0).returns(1, 3);
    checkWidth(100).returns(1, 3);
    checkWidth(200).returns(1, 3);
    checkWidth(300).returns(1, 4);
    checkWidth(400).returns(2, 5);
    checkWidth(600).returns(3, 8);
    checkWidth(800).returns(3, 13);
    checkWidth(1200).returns(3, 22);
  }

  private Checker checkWidth(int width) {
    return new Checker(AutoRangeUpdater.getConfig(width));
  }

  private class Checker {
    private AutoRangeUpdater.Config config;

    public Checker(AutoRangeUpdater.Config config) {
      this.config = config;
    }

    public void returns(int monthsBack, int monthsForward) {
      assertEquals(toString(monthsBack, monthsForward),
                   toString(config.monthsBack, config.monthsForward));
    }

    private String toString(int monthsBack, int monthsForward) {
      return "[" + monthsBack + ", " + monthsForward + "]";
    }
  }
}