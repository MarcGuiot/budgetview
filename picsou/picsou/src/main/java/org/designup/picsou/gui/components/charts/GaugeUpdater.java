package org.designup.picsou.gui.components.charts;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;

public class GaugeUpdater {
  public static void updateGauge(double futurePositiveRemaining, double futurePositiveOverrun,
                                 double futureNegativeRemaining, double futureNegativeOverrun,
                                 double pastRemaining,
                                 double pastOverrun, double gaugeTarget, double gaugeActual,
                                 final Gauge gauge, BudgetArea budgetArea) {
    String tooltips = computeTooltips(futurePositiveRemaining, futurePositiveOverrun,
                                      futureNegativeRemaining, futureNegativeOverrun,
                                      pastRemaining, pastOverrun,
                                      gaugeTarget, gauge.shouldInvertAll(),
                                      budgetArea);
    gauge.setValues(gaugeActual, gaugeTarget, futureNegativeOverrun + futurePositiveOverrun + pastOverrun,
                    futureNegativeRemaining + futurePositiveRemaining + pastRemaining,
                    "<html>" + tooltips + "</html>");
  }

  public static String computeTooltips(double futurePositiveRemaining, double futurePositiveOverrun,
                                        double futureNegativeRemaining, double futureNegativeOverrun,
                                        double pastRemaining, double pastOverrun, double gaugeTarget,
                                        final boolean shouldInvert, BudgetArea budgetArea) {
    if (shouldInvert) {
      double tmp = futurePositiveOverrun;
      futurePositiveOverrun = -1.0 * futureNegativeOverrun;
      futureNegativeOverrun = -1.0 * tmp;
      tmp = futurePositiveRemaining;
      futurePositiveRemaining = -1.0 * futureNegativeRemaining;
      futureNegativeRemaining = -1.0 * tmp;
      pastOverrun = -1.0 * pastOverrun;
      pastRemaining = -1.0 * pastRemaining;
      gaugeTarget = -1.0 * gaugeTarget;
    }

    String prefix = gaugeTarget > 0 ? "positive" : gaugeTarget < 0 ? "negative" : "zero";
    String tooltips = "";

    if (gaugeTarget > 0 && pastOverrun > 0 && futurePositiveOverrun > 0 && Amounts.isNearZero(futureNegativeOverrun)) {
      futurePositiveOverrun += pastOverrun;
      pastOverrun = 0;
    }

    if (gaugeTarget < 0 && pastOverrun < 0 && futureNegativeOverrun < 0 && Amounts.isNearZero(futurePositiveOverrun)) {
      futureNegativeOverrun += pastOverrun;
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

    if (Amounts.isNotZero(futurePositiveRemaining)) {
      tooltips += toString(budgetArea,
                           prefix,
                           ".positive",
                           ".future.remaining",
                           futurePositiveRemaining);
    }

    if (Amounts.isNotZero(futureNegativeRemaining)) {
      tooltips += toString(budgetArea,
                           prefix,
                           ".negative",
                           ".future.remaining",
                           futureNegativeRemaining);
    }

    if (Amounts.isNotZero(futurePositiveOverrun)) {
      tooltips += toString(budgetArea,
                           prefix,
                           ".positive",
                           ".future.overrun",
                           futurePositiveOverrun);
    }

    if (Amounts.isNotZero(futureNegativeOverrun)) {
      tooltips += toString(budgetArea,
                           prefix,
                           ".negative",
                           ".future.overrun",
                           futureNegativeOverrun);
    }
    return tooltips;
  }

  public static String toString(BudgetArea budgetArea, String prefix, String suffix, String key, double value) {
    return "<p>" +
           Lang.getWithDefault("gauge." + prefix + key + suffix + "." + budgetArea.getName(),
                               "gauge." + prefix + key + suffix,
                               Formatting.DECIMAL_FORMAT.format(Math.abs(value))) +
           "</p>";
  }
}
