package com.budgetview.desktop.components.ui;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CustomButtonUI extends BasicButtonUI {

  private static final int HORIZONTAL_PADDING = 15;
  private static final int VERTICAL_PADDING = 3;

  private int padding = 0;

  private Color borderColor = Color.GRAY.brighter();
  private Color topColor = Color.WHITE;
  private Color bottomColor = Color.WHITE;
  private Color rolloverTextColor = Color.RED;
  private Color pressedTextColor = Color.WHITE;
  private Color disabledTextColor = Color.GRAY;

  private PropertyChangeListener fontMetricsUpdater;
  private int textWidth;
  private int fontHeight;
  private int descent;

  protected void installDefaults(final AbstractButton button) {
    super.installDefaults(button);
    button.setRolloverEnabled(true);
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  protected void installListeners(final AbstractButton button) {
    super.installListeners(button);
    fontMetricsUpdater = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        initFontMetrics(button);
      }
    };
    button.addPropertyChangeListener(fontMetricsUpdater);
  }

  private void initFontMetrics(AbstractButton button) {
    FontMetrics fontMetrics = button.getFontMetrics(button.getFont());
    textWidth = fontMetrics.stringWidth(button.getText() == null ? "" : button.getText());
    fontHeight = fontMetrics.getHeight();
    descent = fontMetrics.getDescent();

    int width = textWidth + HORIZONTAL_PADDING;
    int height = fontHeight + descent + VERTICAL_PADDING;

    button.setSize(width, height);
    Dimension dimension = new Dimension(width, height);
    button.setPreferredSize(dimension);
    button.setMaximumSize(dimension);
    button.setMinimumSize(dimension);
  }

  protected void uninstallListeners(AbstractButton button) {
    super.uninstallListeners(button);
    button.removePropertyChangeListener(fontMetricsUpdater);
  }

  public void paint(Graphics g, JComponent c) {
    JButton button = (JButton)c;
    button.setOpaque(false);

    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    Dimension size = button.getSize();

    int width = size.width;
    int height = size.height;
    if (GuiUtils.isMacOSX()) {
      height -= 2;
    }

    int x = 0;
    int y = 0;

    int rectWidth = width - 1 - (padding * 2);
    int rectHeight = height - 1 - (padding * 2);
    int cornerRadius = rectHeight;

    Shape clipShape = new RoundRectangle2D.Float(x, y, rectWidth + 1, rectHeight + 1, cornerRadius, cornerRadius);
    BufferedImage clipImage = createClipImage(g2d, clipShape, rectWidth + 1, rectHeight + 1);
    Graphics2D g2 = clipImage.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2.setPaint(new GradientPaint(0, 0, topColor, 0, rectHeight, bottomColor));
    g2.fill(clipShape);

    g2.setColor(borderColor);
    g2.setStroke(new BasicStroke(2));
    g2.draw(clipShape);

    ButtonModel model = button.getModel();
    if (model.isRollover()) {
      g2.setColor(rolloverTextColor);
    }
    else {
      g2.setColor(button.getForeground());
    }
    if (!model.isEnabled()) {
      g2.setColor(disabledTextColor);
    }

    String text = button.getText();
    FontMetrics fm = button.getFontMetrics(button.getFont());
    int textX = rectWidth / 2 - fm.stringWidth(text) / 2;
    int textY = (button.getHeight() + fm.getHeight()) / 2 - fm.getDescent();
    g2.setFont(button.getFont());
    g2.drawString(text, textX, textY);

    g2.dispose();

    g.drawImage(clipImage, 0, 0, null);
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public void setTopColor(Color topColor) {
    this.topColor = topColor;
  }

  public void setBottomColor(Color bottomColor) {
    this.bottomColor = bottomColor;
  }

  public void setPressedTextColor(Color pressedTextColor) {
    this.pressedTextColor = pressedTextColor;
  }

  public void setRolloverTextColor(Color rolloverTextColor) {
    this.rolloverTextColor = rolloverTextColor;
  }

  public void setDisabledTextColor(Color disabledTextColor) {
    this.disabledTextColor = disabledTextColor;
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
