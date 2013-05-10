package org.designup.picsou.gui.components.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.security.InvalidParameterException;

public class FlatScrollbarUI extends BasicScrollBarUI {

  private static final int SCROLLBAR_WIDTH = 12;
  private static final int THUMB_BORDER_SIZE = 2;
  private static final int THUMB_SIZE = 8;
  private static final Color THUMB_COLOR = Color.BLACK;

  private Color color = Color.GRAY.brighter();
  private Color rolloverColor = Color.GRAY.darker();
  private Color background = Color.WHITE;

  public void installUI(JComponent c) {
    super.installUI(c);
    JScrollBar scrollBar = (JScrollBar)c;
    switch (scrollBar.getOrientation()) {
      case JScrollBar.VERTICAL:
        scrollBar.setPreferredSize(new Dimension(SCROLLBAR_WIDTH, Integer.MAX_VALUE));
        break;
      case JScrollBar.HORIZONTAL:
        scrollBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, SCROLLBAR_WIDTH));
        break;
      default:
        throw new InvalidParameterException("Invalid orientation");
    }
  }

  public void setColor(Color color) {
    this.color = color;
  }

  public void setRolloverColor(Color color) {
    this.rolloverColor = color;
  }

  public void setBackground(Color color) {
    this.background = color;
  }

  protected JButton createDecreaseButton(int orientation) {
    return new MyScrollBarButton();
  }

  protected JButton createIncreaseButton(int orientation) {
    return new MyScrollBarButton();
  }

  protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    g.setColor(background);
    g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
  }

  protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
    int orientation = scrollbar.getOrientation();
    int arc = THUMB_SIZE;
    int x = thumbBounds.x + THUMB_BORDER_SIZE;
    int y = thumbBounds.y + THUMB_BORDER_SIZE;

    int width = orientation == JScrollBar.VERTICAL ?
                THUMB_SIZE : thumbBounds.width - (THUMB_BORDER_SIZE * 2);
    width = Math.max(width, THUMB_SIZE);

    int height = orientation == JScrollBar.VERTICAL ?
                 thumbBounds.height - (THUMB_BORDER_SIZE * 2) : THUMB_SIZE;
    height = Math.max(height, THUMB_SIZE);

    Graphics2D graphics2D = (Graphics2D) g.create();
    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
    graphics2D.setColor(isThumbRollover() ? rolloverColor : color);
    graphics2D.fillRoundRect(x, y, width, height, arc, arc);
    graphics2D.dispose();
  }

  private static class MyScrollBarButton extends JButton {
    private MyScrollBarButton() {
      setOpaque(false);
      setFocusable(false);
      setFocusPainted(false);
      setBorderPainted(false);
      setBorder(BorderFactory.createEmptyBorder());
    }
  }}
