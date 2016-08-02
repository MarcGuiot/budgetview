package com.budgetview.desktop.components.charts.histo.button;

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
  private String disabledBgTopKey;
  private String disabledBgBottomKey;
  private String disabledLabelKey;
  private String disabledBorderKey;

  private Color bgTopColor;
  private Color bgBottomColor;
  private Color labelColor;
  private Color labelShadowColor;
  private Color borderColor;
  private Color rolloverBgTopColor;
  private Color rolloverBgBottomColor;
  private Color rolloverLabelColor;
  private Color rolloverBorderColor;
  private Color disabledBgTopColor;
  private Color disabledBgBottomColor;
  private Color disabledLabelColor;
  private Color disabledBorderColor;

  public HistoButtonColors(String bgTopKey,
                           String bgBottomKey,
                           String labelKey,
                           String labelShadowKey,
                           String borderKey,
                           String rolloverBgTopKey,
                           String rolloverBgBottomKey,
                           String rolloverLabelKey,
                           String rolloverBorderKey,
                           String disabledBgTopKey,
                           String disabledBgBottomKey,
                           String disabledLabelKey,
                           String disabledBorderKey,
                           Directory directory) {
    this.bgTopKey = bgTopKey;
    this.bgBottomKey = bgBottomKey;
    this.labelKey = labelKey;
    this.labelShadowKey = labelShadowKey;
    this.borderKey = borderKey;
    this.rolloverBgTopKey = rolloverBgTopKey;
    this.rolloverBgBottomKey = rolloverBgBottomKey;
    this.rolloverLabelKey = rolloverLabelKey;
    this.rolloverBorderKey = rolloverBorderKey;
    this.disabledBgTopKey = disabledBgTopKey;
    this.disabledBgBottomKey = disabledBgBottomKey;
    this.disabledLabelKey = disabledLabelKey;
    this.disabledBorderKey = disabledBorderKey;
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    bgTopColor = colorLocator.get(bgTopKey);
    bgBottomColor = colorLocator.get(bgBottomKey);
    labelColor = colorLocator.get(labelKey);
    labelShadowColor = colorLocator.get(labelShadowKey);
    borderColor = colorLocator.get(borderKey);
    rolloverBgTopColor = colorLocator.get(rolloverBgTopKey);
    rolloverBgBottomColor = colorLocator.get(rolloverBgBottomKey);
    rolloverLabelColor = colorLocator.get(rolloverLabelKey);
    rolloverBorderColor = colorLocator.get(rolloverBorderKey);
    disabledBgTopColor = colorLocator.get(disabledBgTopKey);
    disabledBgBottomColor = colorLocator.get(disabledBgBottomKey);
    disabledLabelColor = colorLocator.get(disabledLabelKey);
    disabledBorderColor = colorLocator.get(disabledBorderKey);
  }

  public Color getLabelColor(boolean enabled, boolean selected, boolean rollover) {
    if (rollover || selected) {
      return rolloverLabelColor;
    }
    else if (enabled) {
      return labelColor;
    }
    else {
      return disabledLabelColor;
    }
  }

  public Color getLabelShadowColor() {
    return labelShadowColor;
  }

  public Color getBorderColor(boolean enabled, boolean selected, boolean rollover) {
    if (rollover || selected) {
      return rolloverBorderColor;
    }
    else if (enabled) {
      return borderColor;
    }
    else {
      return disabledBorderColor;
    }
  }

  public Color getBgTopColor(boolean enabled, boolean selected, boolean rollover) {
    if (enabled) {
      return bgTopColor;
    }
    else {
      return disabledBgTopColor;
    }
  }

  public Color getBgBottomColor(boolean enabled, boolean selected, boolean rollover) {
    if (enabled) {
      return bgBottomColor;
    }
    else {
      return disabledBgBottomColor;
    }
  }
}
