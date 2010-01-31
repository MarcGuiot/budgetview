package org.designup.picsou.model.util;

import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

public class Amounts {

  public static boolean sameSign(double value1, double value2) {
    return Math.signum(value1) == Math.signum(value2) || Amounts.isNearZero(value1) || Amounts.isNearZero(value2);
  }

  public static boolean isNullOrZero(Double value) {
    return value == null || isNearZero(value);
  }

  public static boolean isNearZero(double value) {
    return value > -1E-6 && value < 1E-6;
  }

  public static double zeroIfNull(Double value) {
    return Utils.zeroIfNull(value);
  }

  public static boolean isNotZero(Double value) {
    return !isNullOrZero(value);
  }

  public static double normalize(double value) {
    return isNearZero(value) ? 0 : value;
  }

  static public double extractAmount(String amount) {
    double coef = 100.0;
    amount = amount.trim();
    int len = amount.length();
    int commaSep = amount.lastIndexOf(",");
    if (commaSep == len - 2) {
      coef = 10.;
    }
    else if (commaSep == len - 3) {
      coef = 100.;
    }
    else {
      int dotSep = amount.lastIndexOf(".");
      if (dotSep == len - 2) {
        coef = 10.;
      }
      else if (dotSep == len - 3) {
        coef = 100.;
      }
      else {
        coef = 1.;
      }
    }

    String tmp = amount.replaceAll(",", "").replaceAll("\\.", "").replaceAll(" ", "");
    if (Strings.isNullOrEmpty(tmp)) {
      return 0.0;
    }
    return Double.parseDouble(tmp) / coef;
  }

  public static boolean equal(Double val1, Double val2) {
    return Math.abs(val1 - val2) < 0.0001;
  }

  public static double diff(Double val1, Double val2) {
    if ((val1 == null) && (val2 == null)) {
      return 0.0;
    }
    if (val1 == null) {
      return -val2;
    }
    if (val2 == null) {
      return val1;
    }
    return val1 - val2;
  }

  public static double max(Double val1, Double planned, boolean isIncome) {
    if (val1 == null) {
      return planned;
    }
    if (planned == null) {
      return val1;
    }
    if (isIncome) {
      return Math.max(val1, planned);
    }
    else {
      if (planned > 0) {
        return Math.max(val1, planned);
      }
      if (planned < 0) {
        return Math.min(val1, planned);
      }
    }
    return val1;
  }

  public static void updateGauge(double futurePositiveRemaining, double futurePositiveOverrun,
                                 double futureNegativeRemaining, double futureNegativeOverrun,
                                 double pastRemaining,
                                 double pastOverrun, double gaugeTarget, double gaugeActual,
                                 final Gauge gauge, BudgetArea budgetArea) {
    String tooltips = computeTooltips(futurePositiveRemaining, futurePositiveOverrun,
                                      futureNegativeRemaining, futureNegativeOverrun,
                                      pastRemaining, pastOverrun, gaugeTarget, gauge.shouldInvertAll(), budgetArea);
    gauge.setValues(gaugeActual, gaugeTarget, futureNegativeOverrun + futurePositiveOverrun + pastOverrun,
                    futureNegativeRemaining + futurePositiveRemaining +  pastRemaining,
                    "<html>" + tooltips + "</html>");
  }

  private static String computeTooltips(double futurePositiveRemaining, double futurePositiveOverrun,
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
    String enterPara = "<p>";
    String leavePara = "</p>";

    if (gaugeTarget > 0 && pastOverrun > 0 && futurePositiveOverrun > 0 && Amounts.isNearZero(futureNegativeOverrun)) {
      futurePositiveOverrun += pastOverrun;
      pastOverrun = 0;
    }

    if (gaugeTarget < 0 && pastOverrun < 0 && futureNegativeOverrun < 0 && Amounts.isNearZero(futurePositiveOverrun)) {
      futureNegativeOverrun += pastOverrun;
      pastOverrun = 0;
    }

    if (isNotZero(pastRemaining)) {
      String sufixe = pastRemaining > 0 ? ".positive" : ".negative";
      tooltips += enterPara +
                  Lang.getWithDefault("gauge." + prefix + ".past.remaining" + sufixe + "." + budgetArea.getName(),
                                      "gauge." + prefix + ".past.remaining" + sufixe,
                                      Formatting.DECIMAL_FORMAT.format(Math.abs(pastRemaining))) + leavePara;
    }

    if (isNotZero(pastOverrun)) {
      String sufixe = pastOverrun > 0 ? ".positive" : ".negative";
      tooltips += enterPara +
                  Lang.getWithDefault("gauge." + prefix + ".past.overrun" + sufixe + "." + budgetArea.getName(),
                                      "gauge." + prefix + ".past.overrun" + sufixe,
                                      Formatting.DECIMAL_FORMAT.format(Math.abs(pastOverrun))) + leavePara;
    }

    if (isNotZero(futurePositiveRemaining)) {
      String sufixe = ".positive";
      tooltips += enterPara +
                  Lang.getWithDefault("gauge." + prefix + ".future.remaining" + sufixe + "." + budgetArea.getName(),
                                      "gauge." + prefix + ".future.remaining" + sufixe,
                                      Formatting.DECIMAL_FORMAT.format(Math.abs(futurePositiveRemaining))) + leavePara;
    }

    if (isNotZero(futureNegativeRemaining)) {
      String sufixe = ".negative";
      tooltips += enterPara +
                  Lang.getWithDefault("gauge." + prefix + ".future.remaining" + sufixe + "." + budgetArea.getName(),
                                      "gauge." + prefix + ".future.remaining" + sufixe,
                                      Formatting.DECIMAL_FORMAT.format(Math.abs(futureNegativeRemaining))) + leavePara;
    }

    if (isNotZero(futurePositiveOverrun)) {
      String sufixe = ".positive";
      tooltips += enterPara +
                  Lang.getWithDefault("gauge." + prefix + ".future.overrun" + sufixe + "." + budgetArea.getName(),
                                      "gauge." + prefix + ".future.overrun" + sufixe,
                                      Formatting.DECIMAL_FORMAT.format(Math.abs(futurePositiveOverrun))) + leavePara;
    }

    if (isNotZero(futureNegativeOverrun)) {
      String sufixe = ".negative";
      tooltips += enterPara +
                  Lang.getWithDefault("gauge." + prefix + ".future.overrun" + sufixe + "." + budgetArea.getName(),
                                      "gauge." + prefix + ".future.overrun" + sufixe,
                                      Formatting.DECIMAL_FORMAT.format(Math.abs(futureNegativeOverrun))) + leavePara;
    }
    return tooltips;
  }

  public static double upperOrder(double value) {
    int power = (int)Math.log10(value);
    return Math.pow(10, power + 1);
  }
}
