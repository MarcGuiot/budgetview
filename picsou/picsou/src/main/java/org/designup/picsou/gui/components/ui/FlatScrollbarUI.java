package org.designup.picsou.gui.components.ui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.security.InvalidParameterException;

public class FlatScrollbarUI extends BasicScrollBarUI {

  private static final int SCROLLBAR_WIDTH = 12;
  private static final int THUMB_PADDING = 2;
  private static final int THUMB_WIDTH = 8;

  private static final int MIN_HORIZONTAL_LENGTH = 25;
  private static final int MIN_VERTICAL_LENGTH = 20;

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

  protected void installDefaults() {
    super.installDefaults();
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

  public void paint(Graphics g, JComponent c) {

    Rectangle trackBounds = getTrackBounds();
    g.setClip(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

    Rectangle thumbBounds = getThumbBounds();
    int orientation = scrollbar.getOrientation();
    int arc = THUMB_WIDTH;
    int x = thumbBounds.x + THUMB_PADDING;
    int y = thumbBounds.y + THUMB_PADDING;

    int width = orientation == JScrollBar.VERTICAL ?
                THUMB_WIDTH : Math.max(thumbBounds.width - (THUMB_PADDING * 2), MIN_HORIZONTAL_LENGTH);
    width = Math.max(width, THUMB_WIDTH);

    int height = orientation == JScrollBar.VERTICAL ?
                 Math.max(thumbBounds.height - (THUMB_PADDING * 2), MIN_VERTICAL_LENGTH) : THUMB_WIDTH;
    height = Math.max(height, THUMB_WIDTH);

    Graphics2D graphics2D = (Graphics2D) g.create();

    graphics2D.setColor(background);
    graphics2D.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);

    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    graphics2D.setColor(isThumbRollover() ? rolloverColor : color);
    graphics2D.fillRoundRect(x, y, width, height, arc, arc);
    graphics2D.dispose();
  }

  protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
  }

  protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
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
