package com.budgetview.desktop.components;

import junit.framework.Assert;
import junit.framework.TestCase;
import com.budgetview.desktop.components.charts.DeltaGauge;

import static com.budgetview.desktop.components.charts.DeltaGauge.Evolution.*;

public class DeltaGaugeTest extends TestCase {
  private DeltaGauge gauge;

  public void setUp() throws Exception {
    this.gauge = new DeltaGauge();
  }

  public void testNoRatio() throws Exception {
    checkNoRatio(null, null);
    checkNoRatio(100.0, null);
    checkNoRatio(-100.0, null);
  }

  public void testNoPreviousValue() throws Exception {
    checkRatio(null, 0.0, 0.0, FLAT);
    checkRatio(null, +1.0, +1.0, BETTER);
    checkRatio(null, -1.0, +1.0, WORSE);

    checkRatio(0.0, 0.0, 0.0, FLAT);
    checkRatio(0.0, +1.0, +1.0, BETTER);
    checkRatio(0.0, -1.0, +1.0, WORSE);
  }

  public void testSignChange() throws Exception {
    checkRatio(+100.0, -1.0, -1.0, WORSE);
    checkRatio(-100.0, +1.0, +1.0, BETTER);
    checkRatio(+1.0, 0.0, -1.0, WORSE);
    checkRatio(-1.0, 0.0, -1.0, BETTER);
  }

  public void testPositiveVariation() throws Exception {
    checkRatio(+100.0, +150.0, +0.5, BETTER);
    checkRatio(+100.0, +100.0, +0.0, FLAT);
    checkRatio(+100.0, +200.0, +1.0, BETTER);
    checkRatio(+100.0, +50.0, -0.5, WORSE);
    checkRatio(+100.0, +75.0, -0.25, WORSE);
  }

  public void testNegativeVariation() throws Exception {
    checkRatio(-100.0, -100.0, +0.0, FLAT);
    checkRatio(-100.0, -150.0, +0.5, WORSE);
    checkRatio(-100.0, -200.0, +1.0, WORSE);
    checkRatio(-100.0, -50.0, -0.5, BETTER);
    checkRatio(-100.0, -75.0, -0.25, BETTER);
  }

  private void checkNoRatio(Double previousRatio, Double newValue) {
    gauge.setValues(previousRatio, newValue);
    assertFalse(gauge.isActive());
    assertEquals(0.0, gauge.getRatio());
  }

  private void checkRatio(Double previousValue, Double newValue, double expectedRatio, DeltaGauge.Evolution evolution) {
    gauge.setValues(previousValue, newValue);
    assertTrue(gauge.isActive());
    if ((Math.abs(expectedRatio - gauge.getRatio()) > 0.01)) {
      Assert.fail("Expected [" + previousValue + " / " + newValue + "] ==> " + expectedRatio + " but was: " + gauge.getRatio());
    }
    if (evolution != gauge.getEvolution()) {
      Assert.fail("Expected [" + previousValue + " / " + newValue + "] ==> " + expectedRatio + " / " +
                  evolution + " but was: " + gauge.getEvolution());
    }
  }
}
