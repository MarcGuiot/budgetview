package org.designup.picsou.gui.components.ui;

import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class NotificationFlagUI extends BasicButtonUI {

  private static final int ARROW_HEIGHT = 6;
  private static final int VERTICAL_PADDING = 2;
  private static final int HORIZONTAL_PADDING = 5;
  private static final int MIN_WIDTH = 40;

  private Color balloonColor = Color.YELLOW;
  private Color balloonShadowColor = Color.YELLOW.darker().darker();
  private Color balloonRolloverColor = Color.YELLOW.brighter().brighter();
  private Color balloonPressedColor = Color.WHITE;

  public void installUI(JComponent component) {
    super.installUI(component);

    JButton button = (JButton)component;
    button.setOpaque(false);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setRolloverEnabled(true);
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  protected void installListeners(final AbstractButton button) {
    super.installListeners(button);
    button.addPropertyChangeListener(AbstractButton.TEXT_CHANGED_PROPERTY, new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        updateSize(button);
      }
    });
    updateSize(button);
  }

  private void updateSize(AbstractButton button) {
    FontMetrics fontMetrics = button.getFontMetrics(button.getFont());
    int width = Math.max(fontMetrics.stringWidth(button.getText()) + 2 * HORIZONTAL_PADDING, MIN_WIDTH);
    int height = fontMetrics.getHeight() + ARROW_HEIGHT + 2 * VERTICAL_PADDING;
    Dimension size = new Dimension(width, height);
    button.setSize(size);
    button.setPreferredSize(size);
    button.setMaximumSize(size);
    button.setMinimumSize(size);
  }

  public void paint(Graphics g, JComponent c) {

    JButton button = (JButton)c;
    Graphics2D g2 = (Graphics2D)g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = button.getWidth() - 1;
    int height = button.getHeight() - 1;
    int balloonWidth = width - 3;
    int balloonHeight = height - ARROW_HEIGHT - 3;
    int arrowTop = balloonHeight;
    int arrowBottom = arrowTop + ARROW_HEIGHT;
    int arrowLeft = (int)(balloonWidth * 0.7f);
    int arrowRight = (int)(balloonWidth * 0.85f);

    drawBalloon(g2, 2, 2, balloonShadowColor,
                balloonWidth, balloonHeight, arrowTop, arrowBottom, arrowLeft, arrowRight);
    drawBalloon(g2, 0, 0, getBalloonColor(button),
                balloonWidth, balloonHeight, arrowTop, arrowBottom, arrowLeft, arrowRight);

    String text = button.getText();
    FontMetrics fm = g2.getFontMetrics();
    int textX = width / 2 - fm.stringWidth(text) / 2;
    int textY = (balloonHeight + fm.getHeight()) / 2 - fm.getDescent();
    g2.setColor(button.getForeground());
    g2.drawString(text, textX, textY);
    g2.dispose();
  }

  private Color getBalloonColor(JButton button) {
    ButtonModel model = button.getModel();
    if (model.isPressed()) {
      return balloonPressedColor;
    }
    if (model.isRollover() || model.isArmed()) {
      return balloonRolloverColor;
    }
    return balloonColor;
  }

  private void drawBalloon(Graphics2D g2, int x, int y, Color color, int balloonWidth, int balloonHeight, int arrowTop, int arrowBottom, int arrowLeft, int arrowRight) {
    Polygon polygon = new Polygon();
    polygon.addPoint(x + arrowLeft, y + arrowTop);
    polygon.addPoint(x + arrowRight, y + arrowTop);
    polygon.addPoint(x + arrowLeft, y + arrowBottom);

    g2.setColor(color);
    g2.fillPolygon(polygon);
    g2.fillRoundRect(x, y, balloonWidth, balloonHeight, HORIZONTAL_PADDING, HORIZONTAL_PADDING);
  }

  public void setBalloonColor(Color balloonColor) {
    this.balloonColor = balloonColor;
  }

  public void setBalloonPressedColor(Color balloonPressedColor) {
    this.balloonPressedColor = balloonPressedColor;
  }

  public void setBalloonRolloverColor(Color balloonRolloverColor) {
    this.balloonRolloverColor = balloonRolloverColor;
  }

  public void setBalloonShadowColor(Color balloonShadowColor) {
    this.balloonShadowColor = balloonShadowColor;
  }

  public static void main(String[] args) {
    JButton button = new JButton();
    button.setText("6");
    button.setUI(new NotificationFlagUI());

    GuiUtils.showCentered(GridBagBuilder.init()
                            .add(button, 0, 0, 1, 1, 1, 1, Fill.NONE, Anchor.CENTER)
                            .getPanel());
  }
}
