package org.designup.picsou.gui.projects.components;

import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.model.Month;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ProjectButtonUI extends BasicButtonUI {

  protected void installDefaults(AbstractButton b) {
    super.installDefaults(b);
    b.setOpaque(false);
    b.setBorder(null);
  }

  public void paint(Graphics g, JComponent c) {
    ProjectButton block = (ProjectButton)c;

    BufferedImage image = ((Graphics2D)g).getDeviceConfiguration().createCompatibleImage(c.getWidth(), c.getHeight(), Transparency.TRANSLUCENT);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    doPaint(g2, block);

    ((Graphics2D)g).drawImage(image, null, null);
  }

  private void doPaint(Graphics2D g2, ProjectButton button) {
    int width = button.getWidth() - 1;
    int height = button.getHeight() - 1;

    g2.setColor(button.getBackground());
    g2.fillRect(0, 0, width, height);

    g2.setColor(button.getBorderColor());
    g2.drawRect(0, 0, width, height);

    Icon icon = button.getIcon();
    int iconHeight = 75;
    if (icon != null) {
      icon.paintIcon(button, g2,
                     width / 2 - icon.getIconWidth() / 2,
                     5 + iconHeight / 2 - icon.getIconHeight() / 2);
    }

    int gaugeTop = 10 + iconHeight;
    Gauge gauge = button.getGauge();
    Graphics gaugeGraphics = g2.create(width / 2 - gauge.getWidth() / 2,
                                       gaugeTop,
                                       gauge.getWidth(),
                                       gauge.getHeight());
    gauge.paint(gaugeGraphics);
    gaugeGraphics.dispose();

    int labelTop = gaugeTop + gauge.getHeight() + 10;
    FontMetrics metrics = button.getFontMetrics(button.getFont());
    g2.setFont(button.getFont());
    g2.setColor(button.getForeground());
    String planned = button.getPlanned();
    g2.drawString(planned,
                  width - 5 - metrics.stringWidth(planned),
                  labelTop);
    g2.drawString(button.getMonth(), 5, labelTop);
  }
}
