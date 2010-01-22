package org.globsframework.gui.splits.components;

import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class HyperlinkButtonUI extends BasicButtonUI {
  private int textWidth;
  private int fontHeight;
  private int descent;
  private boolean underline = true;
  private boolean autoHideEnabled = true;
  private boolean useNormalColorWhenDisabled = false;

  private Color rolloverColor = Color.BLUE.brighter();
  private Color disabledColor = Color.GRAY;
  private PropertyChangeListener autoHideListener;
  private PropertyChangeListener fontMetricsUpdater;

  public HyperlinkButtonUI() {
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    AbstractButton button = (AbstractButton)c;
    button.setRolloverEnabled(true);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.setOpaque(false);
    initFontMetrics(button);
    updateVisibility(button);
  }

  protected void installListeners(final AbstractButton button) {
    super.installListeners(button);
    autoHideListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        updateVisibility(button);
      }
    };
    button.addPropertyChangeListener("enabled", autoHideListener);

    fontMetricsUpdater = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        initFontMetrics(button);
      }
    };
    button.addPropertyChangeListener("font", fontMetricsUpdater);
    button.addPropertyChangeListener("text", fontMetricsUpdater);
    button.addPropertyChangeListener("icon", fontMetricsUpdater);
  }

  private void updateVisibility(AbstractButton button) {
    button.setVisible(button.isEnabled() || !autoHideEnabled);
  }

  protected void uninstallListeners(AbstractButton button) {
    super.uninstallListeners(button);
    button.removePropertyChangeListener(autoHideListener);
    button.removePropertyChangeListener(fontMetricsUpdater);
  }

  public void setAutoHideEnabled(boolean autoHideIfDisabled) {
    this.autoHideEnabled = autoHideIfDisabled;
  }

  public void setUseNormalColorWhenDisabled(boolean value) {
    this.useNormalColorWhenDisabled = value;
  }

  public void setUnderline(boolean underline) {
    this.underline = underline;
  }

  public void setRolloverColor(Color rolloverColor) {
    this.rolloverColor = rolloverColor;
  }

  public void setDisabledColor(Color disabledColor) {
    this.disabledColor = disabledColor;
  }

  public void paint(Graphics g, JComponent c) {

    AbstractButton button = (AbstractButton)c;

    Graphics2D d = (Graphics2D)g;
    d.setFont(button.getFont());
    d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (button.isOpaque()) {
      d.setColor(button.getParent().getBackground());
      d.clearRect(0, 0, button.getWidth(), button.getHeight());
    }

    String text = button.getText();
    if (Strings.isNullOrEmpty(text)) {
      return;
    }

    Icon icon = button.getIcon();
    if (icon != null) {
      icon.paintIcon(button, g, 0, button.getHeight() / 2 - icon.getIconHeight() / 2);
    }

    int textX = getLabelX(button);
    int textY = (button.getHeight() + fontHeight) / 2 - descent;
    if (button.getModel().isRollover() || button.getModel().isArmed()) {
      d.setColor(rolloverColor);
    }
    else if (!button.isEnabled() && !useNormalColorWhenDisabled) {
      d.setColor(disabledColor);
    }
    else {
      d.setColor(button.getForeground());
    }

    d.drawString(text, textX, textY);

    if (button.isEnabled() && (underline || button.getModel().isRollover())) {
      d.drawLine(textX, textY + 1, textX + textWidth, textY + 1);
    }
  }

  private int getLabelX(AbstractButton button) {
    int iconOffset = button.getIcon() != null ? button.getIcon().getIconWidth() + button.getIconTextGap() : 0;

    int alignment = button.getHorizontalAlignment();
    switch (alignment) {
      case SwingConstants.LEFT:
        return iconOffset;
      case SwingConstants.CENTER:
        return iconOffset + (button.getWidth() - iconOffset - textWidth) / 2;
      case SwingConstants.RIGHT:
        return (button.getWidth() - textWidth);
    }
    throw new InvalidParameter("Unsupported horizontalAlignement value: " + alignment);
  }

  private void initFontMetrics(AbstractButton button) {
    FontMetrics fontMetrics = button.getFontMetrics(button.getFont());
    textWidth = fontMetrics.stringWidth(button.getText() == null ? "" : button.getText());
    fontHeight = fontMetrics.getHeight();
    descent = fontMetrics.getDescent();

    int width = computeWidth(button);
    int height = computeHeight(button);

    button.setSize(width, height);
    Dimension dimension = new Dimension(width, height);
    button.setPreferredSize(dimension);
    button.setMaximumSize(dimension);
    button.setMinimumSize(dimension);
  }

  private int computeWidth(AbstractButton button) {
    if (button.getIcon() == null) {
      return textWidth;
    }
    return textWidth + button.getIconTextGap() + button.getIcon().getIconWidth();
  }

  private int computeHeight(AbstractButton button) {
    if (button.getIcon() == null) {
      return fontHeight;
    }
    return Math.max(button.getIcon().getIconHeight(), fontHeight);
  }
}