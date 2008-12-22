package org.designup.picsou.gui.utils;

import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;

import java.awt.*;

public class AmountColors implements ColorChangeListener {
  private Color balanceZero;
  private Color balancePlus4;
  private Color balancePlus3;
  private Color balancePlus2;
  private Color balancePlus1;
  private Color balancePlus05;
  private Color balanceMinus4;
  private Color balanceMinus3;
  private Color balanceMinus2;
  private Color balanceMinus1;
  private Color balanceMinus05;
  private Color normalText;

  public AmountColors(Directory directory) {
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    balanceZero = colorLocator.get(PicsouColors.PERIOD_BALANCE_ZERO);
    balancePlus4 = colorLocator.get(PicsouColors.PERIOD_BALANCE_PLUS_4);
    balancePlus3 = colorLocator.get(PicsouColors.PERIOD_BALANCE_PLUS_3);
    balancePlus2 = colorLocator.get(PicsouColors.PERIOD_BALANCE_PLUS_2);
    balancePlus1 = colorLocator.get(PicsouColors.PERIOD_BALANCE_PLUS_1);
    balancePlus05 = colorLocator.get(PicsouColors.PERIOD_BALANCE_PLUS_05);
    balanceMinus4 = colorLocator.get(PicsouColors.PERIOD_BALANCE_MINUS_4);
    balanceMinus3 = colorLocator.get(PicsouColors.PERIOD_BALANCE_MINUS_3);
    balanceMinus2 = colorLocator.get(PicsouColors.PERIOD_BALANCE_MINUS_2);
    balanceMinus1 = colorLocator.get(PicsouColors.PERIOD_BALANCE_MINUS_1);
    balanceMinus05 = colorLocator.get(PicsouColors.PERIOD_BALANCE_MINUS_05);
    normalText = colorLocator.get("block.total");
  }

  public Color getIndicatorColor(double diff) {
    Color color = balanceZero;
    if (diff > 400) {
      color = balancePlus4;
    }
    else if (diff > 300) {
      color = balancePlus3;
    }
    else if (diff > 200) {
      color = balancePlus2;
    }
    else if (diff > 100) {
      color = balancePlus1;
    }
    else if (diff > 50) {
      color = balancePlus05;
    }
    else if (diff < -400) {
      color = balanceMinus4;
    }
    else if (diff < -200) {
      color = balanceMinus3;
    }
    else if (diff < -100) {
      color = balanceMinus2;
    }
    else if (diff < -50) {
      color = balanceMinus1;
    }
    else if (diff < 0) {
      color = balanceMinus05;
    }
    return color;
  }

  public Color getTextColor(double value) {
    return getTextColor(value, normalText);
  }

  public Color getTextColor(double value, Color normalColor) {
    Color color = normalColor;
    if (value < -400) {
      color = balanceMinus4;
    }
    else if (value < -200) {
      color = balanceMinus3;
    }
    else if (value < -100) {
      color = balanceMinus2;
    }
    else if (value < -50) {
      color = balanceMinus1;
    }
    else if (value < 0) {
      color = balanceMinus05;
    }
    return color;
  }

}
