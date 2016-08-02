package com.budgetview.desktop.components;

import java.awt.*;

public class TextDisplayHolder implements TextDisplay {
  private String text;
  private Color foreground;
  private boolean visible;
  private String tooltipText;

  public TextDisplayHolder() {
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public void setToolTipText(String tooltipText) {
    this.tooltipText = tooltipText;
  }

  public String getTooltipText() {
    return tooltipText;
  }

  public void setForeground(Color color) {
    this.foreground = color;
  }

  public Color getForeground() {
    return foreground;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }
}
