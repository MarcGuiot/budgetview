package org.designup.picsou.gui.utils;

import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;

import java.awt.*;

public class AmountColors implements ColorChangeListener {
  public Color balanceZero;
  public Color balancePlus4;
  public Color balancePlus3;
  public Color balancePlus2;
  public Color balancePlus1;
  public Color balancePlus05;
  public Color balanceMinus4;
  public Color balanceMinus3;
  public Color balanceMinus2;
  public Color balanceMinus1;
  public Color balanceMinus05;

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
  }

  public Color get(double diff) {
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
}
