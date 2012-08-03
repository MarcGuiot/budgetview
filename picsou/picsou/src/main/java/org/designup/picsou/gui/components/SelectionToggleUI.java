package org.designup.picsou.gui.components;

import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.splits.utils.Java2DUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class SelectionToggleUI extends BasicToggleButtonUI {
  private static final Color TOP_COLOR = new Color(203, 210, 221);
  private static final Color BOTTOM_COLOR = new Color(143, 159, 183);

  private boolean useBold = true;
  private int padding = 0;
  private int borderWidth = 2;
  private Color borderColor = Color.GRAY.brighter();
  private int cornerRadius = 10;
  private Color topColor;
  private Color bottomColor;
  private Color rolloverTextColor = Color.RED;
  private Color pressedTextColor = Color.WHITE;
  private Color disabledTextColor = Color.GRAY;

  protected void installDefaults(final AbstractButton button) {
    super.installDefaults(button);
    button.setRolloverEnabled(true);
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    if (useBold) {
      final Font defaultFont = button.getFont();
      final Font boldFont = defaultFont.deriveFont(defaultFont.getStyle() ^ Font.BOLD);

      button.getModel().addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          button.setFont(button.isSelected() ? boldFont : defaultFont);
        }
      });
    }
  }

  protected void paintButtonPressed(Graphics g, AbstractButton button) {
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

    Shape clipShape = new RoundRectangle2D.Float(x, y, rectWidth + 1, rectHeight + 1, cornerRadius, cornerRadius);

    BufferedImage clipImage = createClipImage(g2d, clipShape, rectWidth + 1, rectHeight + 1);
    Graphics2D g2 = clipImage.createGraphics();

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setComposite(AlphaComposite.SrcAtop);
    g2.setPaint(new GradientPaint(0, 0, TOP_COLOR, 0, rectHeight, BOTTOM_COLOR.darker()));
    g2.fill(clipShape);

    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.4f));
    g2.setColor(Color.DARK_GRAY);
    g2.setStroke(new BasicStroke(2));
    g2.drawRoundRect(x, y, rectWidth, rectHeight, cornerRadius, cornerRadius);

    g2.dispose();

    g.drawImage(clipImage, 0, 0, null);
  }

  protected void paintText(Graphics g, JComponent component, Rectangle textRect, String text) {
    AbstractButton b = (AbstractButton)component;
    ButtonModel model = b.getModel();
    FontMetrics fm = g.getFontMetrics(g.getFont());
    int mnemonicIndex = b.getDisplayedMnemonicIndex();
    if (model.isRollover()) {
      g.setColor(rolloverTextColor);
    }
    else {
      g.setColor(component.getForeground());
    }
    if (!b.isEnabled()) {
      g.setColor(disabledTextColor);
    }

    if (model.isPressed() || model.isSelected()) {
      Java2DUtils.drawShadowedString((Graphics2D)g, text, pressedTextColor, Color.DARK_GRAY,
                                     textRect.x + getTextShiftOffset(),
                                     textRect.y + (fm.getMaxAscent() - fm.getMaxDescent()) + getTextShiftOffset());
    }
    else {
      g.drawString(text,
                   textRect.x + getTextShiftOffset(),
                   textRect.y + (fm.getMaxAscent() - fm.getMaxDescent()) + getTextShiftOffset());
    }
    if (mnemonicIndex > 0) {
      GuiUtils.drawUnderlineCharAt(g, text, mnemonicIndex,
                                   textRect.x + getTextShiftOffset(),
                                   textRect.y + (fm.getMaxAscent() - fm.getMaxDescent()) + getTextShiftOffset());
    }
  }

  public void setUseBold(boolean useBold) {
    this.useBold = useBold;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public void setBottomColor(Color bottomColor) {
    this.bottomColor = bottomColor;
  }

  public void setTopColor(Color topColor) {
    this.topColor = topColor;
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

  public void setBorderWidth(int borderWidth) {
    this.borderWidth = borderWidth;
  }

  public void setCornerRadius(int cornerRadius) {
    this.cornerRadius = cornerRadius;
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
