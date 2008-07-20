package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.components.jide.JideFastGradientPainter;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorLocator;

import javax.swing.*;
import java.awt.*;

public class JGradientPanel extends JPanel implements ColorChangeListener {
  private Color topColor;
  private Color bottomColor;
  private PicsouColors topColorKey;
  private PicsouColors bottomColorKey;

  public JGradientPanel(ColorService colorService, PicsouColors topColorKey, PicsouColors bottomColorKey) {
    this.topColorKey = topColorKey;
    this.bottomColorKey = bottomColorKey;
    colorService.addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    topColor = colorLocator.get(topColorKey);
    bottomColor = colorLocator.get(bottomColorKey);
  }

  public void update(Graphics graphics) {
    graphics.clearRect(0, 0, getWidth(), getHeight());
    paintComponents(graphics);
  }

  protected void paintComponent(Graphics graphics) {
    setOpaque(false);
    Graphics2D g2 = (Graphics2D)graphics.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (topColor.equals(bottomColor)) {
      g2.setPaint(topColor);
      g2.fillRect(0, 0, getWidth(), getHeight());
    }
    else {
      Shape rect = new Rectangle(0, 0, getWidth(), getHeight());
      JideFastGradientPainter.drawGradient(g2, rect, topColor, bottomColor, true);
    }

    g2.dispose();
  }
}
