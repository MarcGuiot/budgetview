package org.designup.picsou.gui.components;

import org.crossbowlabs.splits.color.ColorChangeListener;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.color.ColorSource;
import org.designup.picsou.gui.utils.PicsouColors;

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

  public void colorsChanged(ColorSource colorSource) {
    topColor = colorSource.get(topColorKey);
    bottomColor = colorSource.get(bottomColorKey);
  }

  public void update(Graphics graphics) {
    graphics.clearRect(0, 0, getWidth(), getHeight());
    paintComponents(graphics);
  }

  protected void paintComponent(Graphics graphics) {
    setOpaque(false);
    Graphics2D g2 = (Graphics2D) graphics.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
    g2.setPaint(gradient);

    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
  }
}
