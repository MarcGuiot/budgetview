package org.designup.picsou.gui.components.charts;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.BudgetArea;
import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.utils.Lang;

public class GaugeUpdater {
  public static void updateGauge(double futureRemaining, double futureOverrun,
                                 double pastRemaining,
                                 double pastOverrun, double gaugeTarget, double gaugeActual,
                                 boolean active,
                                 final Gauge gauge, BudgetArea budgetArea, boolean isRemainingUnset) {
    String tooltips = computeTooltips(futureRemaining, futureOverrun,
                                      pastRemaining, pastOverrun,
                                      gaugeTarget, gauge.shouldInvertAll(),
                                      budgetArea);
    gauge.getModel().setValues(gaugeActual, gaugeTarget, futureOverrun + pastOverrun, futureRemaining + pastRemaining, tooltips, isRemainingUnset);
    gauge.setActive(active);
  }

  public static String computeTooltips(double futureRemaining, double futureOverrun,
                                       double pastRemaining, double pastOverrun, double gaugeTarget,
                                       final boolean shouldInvert, BudgetArea budgetArea) {
    if (gaugeTarget < 0) {
      double diff = (futureOverrun + pastOverrun) - pastRemaining;
      if (diff > 0) {
        futureOverrun = 0;
        pastOverrun = 0;
        pastRemaining = -diff;
      }
      else {
        futureOverrun = 0;
        pastRemaining = 0;
        pastOverrun = diff;
      }
    }
    else if (gaugeTarget > 0){
      double diff = (futureOverrun + pastOverrun) - pastRemaining;
      if (diff < 0) {
        futureOverrun = 0;
        pastOverrun = 0;
        pastRemaining = -diff;
      }
      else {
        pastRemaining = 0;
        futureOverrun = 0;
        pastOverrun = diff;
      }
    }

    if (shouldInvert) {
      futureOverrun = -1.0 * futureOverrun;
      futureRemaining = -1.0 * futureRemaining;
      pastOverrun = -1.0 * pastOverrun;
      pastRemaining = -1.0 * pastRemaining;
      gaugeTarget = -1.0 * gaugeTarget;
    }

    String prefix = gaugeTarget > 0 ? "positive" : gaugeTarget < 0 ? "negative" : "zero";
    String tooltips = "";

    futureOverrun += pastOverrun;
    pastOverrun = 0;

    if (gaugeTarget > 0 && pastOverrun > 0 && futureOverrun > 0) {
      futureOverrun += pastOverrun;
      pastOverrun = 0;
    }

    if (gaugeTarget < 0 && pastOverrun < 0 && futureOverrun < 0 ) {
      futureOverrun += pastOverrun;
      pastOverrun = 0;
    }

    if (Amounts.isNotZero(pastRemaining)) {
      tooltips += toString(budgetArea,
                           prefix,
                           pastRemaining > 0 ? ".positive" : ".negative",
                           ".past.remaining",
                           pastRemaining);
    }

    if (Amounts.isNotZero(pastOverrun)) {
      tooltips += toString(budgetArea,
                           prefix,
                           pastOverrun > 0 ? ".positive" : ".negative",
                           ".past.overrun",
                           pastOverrun);
    }

    if (Amounts.isNotZero(futureRemaining)) {
      tooltips += toString(budgetArea,
                           prefix,
                           futureRemaining > 0 ? ".positive" : ".negative",
                           ".future.remaining",
                           futureRemaining);
    }


    if (Amounts.isNotZero(futureOverrun)) {
      tooltips += toString(budgetArea,
                           prefix,
                           futureOverrun > 0 ? ".positive" : ".negative",
                           ".future.overrun",
                           futureOverrun);
    }

    return tooltips;
  }

  public static String toString(BudgetArea budgetArea, String prefix, String suffix, String key, double value) {
    return "<p>" +
           Lang.getWithDefault("gauge." + prefix + key + suffix + "." + budgetArea.getName(),
                               "gauge." + prefix + key + suffix,
                               Formatting.DECIMAL_FORMAT.format(Math.abs(value)))
           + "</p>";
  }
}
