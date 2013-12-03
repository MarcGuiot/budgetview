package org.globsframework.gui.splits.components;

import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;

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
  private Color lineColor = null;

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
    button.setBorder(null);
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
  }

  private void updateVisibility(AbstractButton button) {
    boolean visible = button.isEnabled() || !autoHideEnabled;
    initFontMetrics(button);
    button.setVisible(visible);
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

  public void setLineColor(Color lineColor) {
    this.lineColor = lineColor;
  }

  public void paint(Graphics g, JComponent c) {

    AbstractButton button = (AbstractButton)c;

    Graphics2D graphics = (Graphics2D)g;
    graphics.setFont(button.getFont());
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (button.isOpaque()) {
      Color background = button.getBackground();
      if (background != null) {
        graphics.fillRect(0, 0, button.getWidth(), button.getHeight());
      }
      else {
        graphics.setColor(button.getParent().getBackground());
        graphics.clearRect(0, 0, button.getWidth(), button.getHeight());
      }
    }

    String text = button.getText();
    if (Strings.isNullOrEmpty(text)) {
      return;
    }

    XPositions positions = getXPositions(button);

    Icon icon = getIcon(button);
    if (icon != null) {
      icon.paintIcon(button, g, positions.iconX, button.getHeight() / 2 - icon.getIconHeight() / 2);
    }

    int textX = positions.labelX;
    int textY = (button.getHeight() + fontHeight) / 2 - descent;
    setTextColor(button, graphics);

    graphics.drawString(text, textX, textY);

    if (button.isEnabled() && (underline || button.getModel().isRollover())) {
      setLineColor(button, graphics);
      graphics.drawLine(textX, textY + 1, textX + textWidth, textY + 1);
    }
  }

  private Icon getIcon(AbstractButton button) {
    ButtonModel model = button.getModel();
    if (!model.isEnabled()) {
      Icon disabledIcon = button.getDisabledIcon();
      if (disabledIcon == null) {
        return button.getIcon();
      }
      return disabledIcon;
    }
    else if (model.isRollover()) {
      Icon icon = button.getRolloverIcon();
      if (icon == null) {
        return button.getIcon();
      }
      return icon;
    }
    else {
      return button.getIcon();
    }
  }

  private void setTextColor(AbstractButton button, Graphics2D d) {
    if (button.getModel().isRollover() || button.getModel().isArmed()) {
      d.setColor(rolloverColor);
    }
    else if (!button.isEnabled() && !useNormalColorWhenDisabled) {
      d.setColor(disabledColor);
    }
    else {
      d.setColor(button.getForeground());
    }
  }

  private void setLineColor(AbstractButton button, Graphics2D d) {
    if (button.getModel().isRollover() || button.getModel().isArmed()) {
      d.setColor(rolloverColor);
    }
    else if (lineColor != null) {
      d.setColor(lineColor);
    }
    else {
      d.setColor(button.getForeground());
    }
  }

  private XPositions getXPositions(AbstractButton button) {

    Icon icon = getIcon(button);
    int iconOffset = icon != null ? icon.getIconWidth() + button.getIconTextGap() : 0;
    int textOffset = icon != null ? textWidth + button.getIconTextGap() : textWidth;

    int contentWidth = textWidth + iconOffset;

    int alignment = button.getHorizontalAlignment();
    int textPosition = button.getHorizontalTextPosition();

    switch (alignment) {

      case SwingConstants.LEFT:
        switch (textPosition) {
          case SwingConstants.RIGHT:
          case SwingConstants.TRAILING:
            return new XPositions(0, iconOffset);
          case SwingConstants.LEFT:
            return new XPositions(textOffset, 0);
          default:
            throw new InvalidParameter("Unsupported horizontalTextPosition value: " + textPosition);
        }

      case SwingConstants.CENTER:
        switch (textPosition) {
          case SwingConstants.RIGHT:
          case SwingConstants.TRAILING:
            return new XPositions((button.getWidth() - textWidth - iconOffset) / 2,
                                  (button.getWidth() - textWidth + iconOffset) / 2);
          case SwingConstants.LEFT:
            return new XPositions(button.getWidth() / 2 - contentWidth / 2 + textOffset,
                                  button.getWidth() / 2 - contentWidth / 2);
          default:
            throw new InvalidParameter("Unsupported horizontalTextPosition value: " + textPosition);
        }

      case SwingConstants.RIGHT:
        switch (textPosition) {
          case SwingConstants.RIGHT:
          case SwingConstants.TRAILING:
            return new XPositions(button.getWidth() - textWidth - iconOffset, button.getWidth() - textWidth);
          case SwingConstants.LEFT:
            return new XPositions(button.getWidth() - iconOffset, button.getWidth() - textWidth - iconOffset);
          default:
            throw new InvalidParameter("Unsupported horizontalTextPosition value: " + textPosition);
        }
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
    Icon icon = getIcon(button);
    if (icon == null) {
      return textWidth;
    }
    return textWidth + button.getIconTextGap() + icon.getIconWidth();
  }

  private int computeHeight(AbstractButton button) {
    Icon icon = getIcon(button);
    if (icon == null) {
      return fontHeight;
    }
    return Math.max(icon.getIconHeight(), fontHeight);
  }

  private class XPositions {
    final int iconX;
    final int labelX;

    private XPositions(int iconX, int labelX) {
      this.iconX = iconX;
      this.labelX = labelX;
    }
  }
}
