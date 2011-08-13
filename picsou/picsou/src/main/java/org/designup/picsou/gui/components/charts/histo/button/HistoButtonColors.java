package org.designup.picsou.gui.components.charts.histo.button;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class HistoButtonColors implements ColorChangeListener {
  
  private String bgTopKey;
  private String bgBottomKey;
  private String labelKey;
  private String labelShadowKey;
  private String borderKey;
  private String rolloverBgTopKey;
  private String rolloverBgBottomKey;
  private String rolloverLabelKey;
  private String rolloverBorderKey;

  private Color bgTopColor;
  private Color bgBottomColor;
  private Color labelColor;
  private Color labelShadowColor;
  private Color borderColor;
  private Color rolloverBgTopColor;
  private Color rolloverBgBottomColor;
  private Color rolloverLabelColor;
  private Color rolloverBorderColor;

  public HistoButtonColors(String bgTopKey, 
                           String bgBottomKey, 
                           String labelKey, 
                           String labelShadowKey,
                           String borderKey,
                           String rolloverBgToKey,
                           String rolloverBgBottomKey,
                           String rolloverLabelKey, 
                           String rolloverBorderKey,
                           Directory directory) {
    this.bgTopKey = bgTopKey;
    this.bgBottomKey = bgBottomKey;
    this.labelKey = labelKey;
    this.labelShadowKey = labelShadowKey;
    this.borderKey = borderKey;
    this.rolloverBgTopKey = rolloverBgToKey;
    this.rolloverBgBottomKey = rolloverBgBottomKey;
    this.rolloverLabelKey = rolloverLabelKey;
    this.rolloverBorderKey = rolloverBorderKey;
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    bgTopColor = colorLocator.get(bgTopKey);
    bgBottomColor = colorLocator.get(bgBottomKey);
    labelColor  = colorLocator.get(labelKey);
    labelShadowColor  = colorLocator.get(labelShadowKey);
    borderColor  = colorLocator.get(borderKey);
    rolloverBgTopColor  = colorLocator.get(rolloverBgTopKey);
    rolloverBgBottomColor = colorLocator.get(rolloverBgBottomKey);
    rolloverLabelColor  = colorLocator.get(rolloverLabelKey);
    rolloverBorderColor  = colorLocator.get(rolloverBorderKey);
  }

  public Color getLabelColor(boolean rollover) {
    return rollover ? rolloverLabelColor : labelColor;
  }

  public Color getLabelShadowColor() {
    return labelShadowColor;
  }

  public Color getBorderColor(boolean rollover) {
    return rollover ? rolloverBorderColor : borderColor;
  }

  public Color getBgTopColor(boolean rollover) {
    return rollover ? rolloverBgTopColor : bgTopColor;
  }

  public Color getBgBottomColor(boolean rollover) {
    return rollover ? rolloverBgBottomColor : bgBottomColor;
  }
}
