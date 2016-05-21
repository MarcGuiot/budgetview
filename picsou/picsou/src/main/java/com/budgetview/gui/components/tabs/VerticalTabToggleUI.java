package com.budgetview.gui.components.tabs;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class VerticalTabToggleUI extends BasicToggleButtonUI {

  public static final int MARGIN_LEFT = 5;
  public static final int MARGIN_RIGHT= 10;

  private Color rolloverTextColor = Color.RED;
  private Color disabledTextColor = Color.GRAY;
  private Color selectedTextColor = Color.BLACK;

  private Color bgColor;
  private Color borderColor;
  private Font boldFont;
  private BufferedImage iconImage;

  protected void installDefaults(final AbstractButton button) {
    super.installDefaults(button);
    button.setRolloverEnabled(true);
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    button.setHorizontalTextPosition(SwingConstants.LEFT);
    button.addPropertyChangeListener(AbstractButton.ICON_CHANGED_PROPERTY, new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        iconImage = null;
        recomputeWidth(button);
      }
    });

    button.addPropertyChangeListener(AbstractButton.TEXT_CHANGED_PROPERTY, new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        recomputeWidth(button);
      }
    });

    button.addPropertyChangeListener("font", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        recomputeWidth(button);
      }
    });
  }

  private void recomputeWidth(AbstractButton button) {
    FontMetrics fm = button.getFontMetrics(button.getFont());
    int width = MARGIN_LEFT + fm.stringWidth(button.getText()) + MARGIN_RIGHT;
    if (button.getIcon() != null) {
      width += button.getIcon().getIconWidth() + button.getIconTextGap();
    }
    Dimension size = new Dimension(width, button.getPreferredSize().height);
    button.setMinimumSize(size);
    button.setPreferredSize(size);
  }

  public void paint(Graphics g, JComponent c) {

    JToggleButton button = (JToggleButton)c;
    button.setOpaque(false);

    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    Rectangle bounds = button.getBounds();
    int width = bounds.width;
    int height = bounds.height - 1;

    if (button.getModel().isSelected()) {
      g2.setColor(bgColor);
      g2.fillRect(0, 0, width, height);

      g2.setColor(borderColor);
      g2.drawLine(0, 0, width, 0);
      g2.drawLine(0, 0, 0, height);
      g2.drawLine(0, height, width, height);
    }

    int left = bounds.x + MARGIN_LEFT;
    int textX = left;

    Icon icon = button.getIcon();
    if (icon != null) {
      if (iconImage == null) {
        iconImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
        icon.paintIcon(new JPanel(), iconImage.getGraphics(), 0, 0);
      }

      textX += icon.getIconWidth() + button.getIconTextGap();
      int iconX = left;
      int iconY = (height / 2) - (icon.getIconHeight() / 2);
      g2.drawImage(iconImage, iconX, iconY, null);
    }

    ButtonModel model = button.getModel();
    if (!button.isEnabled()) {
      g.setColor(disabledTextColor);
    }
    else if (model.isRollover()) {
      g.setColor(rolloverTextColor);
    }
    else if (model.isSelected()) {
      g.setColor(selectedTextColor);
    }
    else {
      g.setColor(button.getForeground());
    }

    FontMetrics fm = g.getFontMetrics(button.getFont());
    g.drawString(button.getText(), textX, (height + fm.getHeight()) / 2 - fm.getDescent() + 1);
  }

  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public void setRolloverTextColor(Color rolloverTextColor) {
    this.rolloverTextColor = rolloverTextColor;
  }

  public void setSelectedTextColor(Color selectedTextColor) {
    this.selectedTextColor = selectedTextColor;
  }

  public void setDisabledTextColor(Color disabledTextColor) {
    this.disabledTextColor = disabledTextColor;
  }
}

