package com.budgetview.gui.components.ui;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class FlatIconButtonUI extends BasicButtonUI {

  private static final int CORNER_RADIUS = 10;

  private int padding = 0;

  private Color bgColor = Color.WHITE;
  private Color rolloverBgColor = Color.RED;
  private Color disabledBgColor = Color.GRAY;

  protected void installDefaults(final AbstractButton button) {
    super.installDefaults(button);
    button.setRolloverEnabled(true);
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  protected void installListeners(final AbstractButton button) {
    super.installListeners(button);
    button.addPropertyChangeListener(AbstractButton.ICON_CHANGED_PROPERTY, new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        updateSize(button);
      }
    });
    updateSize(button);
  }

  private void updateSize(AbstractButton button) {
    Icon icon = button.getIcon();
    if (icon != null) {
      Dimension size = new Dimension(icon.getIconWidth() + 2 * padding,
                                     icon.getIconHeight() + 2 * padding);
      button.setSize(size);
      button.setPreferredSize(size);
      button.setMaximumSize(size);
      button.setMinimumSize(size);
    }
  }

  public void paint(Graphics g, JComponent c) {
    JButton button = (JButton) c;
    button.setOpaque(false);

    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Dimension size = button.getSize();

    int width = size.width;
    int height = size.height;
    if (GuiUtils.isMacOSX()) {
      height -= 2;
    }

    int x = 0;
    int y = 0;

    int rectWidth = width - 1;
    int rectHeight = height - 1;

    Shape clipShape = new RoundRectangle2D.Float(x, y, rectWidth + 1, rectHeight + 1, CORNER_RADIUS, CORNER_RADIUS);
    BufferedImage clipImage = createClipImage(g2d, clipShape, rectWidth + 1, rectHeight + 1);
    Graphics2D g2 = clipImage.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    ButtonModel model = button.getModel();
    if (!model.isEnabled()) {
      g2.setColor(disabledBgColor);
    }
    else if (model.isRollover()) {
      g2.setColor(rolloverBgColor);
    }
    else {
      g2.setColor(bgColor);
    }
    g2.fill(clipShape);

    Icon icon = button.getIcon();
    if (icon != null) {
      icon.paintIcon(button, g2, rectWidth / 2 - icon.getIconWidth() / 2, rectHeight / 2 - icon.getIconHeight() / 2);
    }

    g2.dispose();

    g.drawImage(clipImage, 0, 0, null);
  }

  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
  }

  public void setRolloverBgColor(Color rolloverBgColor) {
    this.rolloverBgColor = rolloverBgColor;
  }

  public void setDisabledBgColor(Color disabledBgColor) {
    this.disabledBgColor = disabledBgColor;
  }

  public void setPadding(int padding) {
    this.padding = padding;
  }

  private BufferedImage createClipImage(Graphics2D g, Shape shape, int width, int height) {
    GraphicsConfiguration gc = g.getDeviceConfiguration();
    BufferedImage img = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
    Graphics2D g2 = img.createGraphics();

    g2.setComposite(AlphaComposite.Clear);
    g2.fillRect(0, 0, width, height);

    g2.setComposite(AlphaComposite.Src);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(Color.WHITE);
    g2.fill(shape);
    g2.dispose();

    return img;
  }
}
