package org.globsframework.gui.splits.painters;

import javax.swing.*;
import java.awt.*;

public class PaintablePanel extends JPanel implements Paintable {
  private Painter painter = Painter.NULL;

  public PaintablePanel() {
    setOpaque(false);
    this.painter = new Painter() {
      public void paint(Graphics g, int width, int height) {
        g.setColor(PaintablePanel.this.getBackground());
        g.fillRect(0, 0, width, height);
      }
    };
  }

  public void setPainter(Painter painter) {
    this.painter = painter;
  }

  public void paintComponent(Graphics g) {
    painter.paint(g, getWidth(), getHeight());
    super.paintComponent(g);
  }
}
