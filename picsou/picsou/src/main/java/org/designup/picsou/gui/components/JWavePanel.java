package org.designup.picsou.gui.components;

import org.crossbowlabs.splits.color.ColorChangeListener;
import org.crossbowlabs.splits.color.ColorSource;
import org.crossbowlabs.splits.color.ColorService;
import org.designup.picsou.gui.utils.PicsouColors;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

public class JWavePanel extends JPanel implements ColorChangeListener {

  private Color topColor;
  private Color bottomColor;
  private Color waveColor;
  private static float WAVE_ALPHA = 0.2f;

  public JWavePanel(ColorService colorService) {
    setOpaque(false);
    colorService.addListener(this);
  }

  public void colorsChanged(ColorSource colorSource) {
    topColor = colorSource.get(PicsouColors.WAVE_PANEL_TOP);
    bottomColor = colorSource.get(PicsouColors.WAVE_PANEL_BOTTOM);
    waveColor = colorSource.get(PicsouColors.WAVE_PANEL_WAVE);
  }
  
  protected void paintComponent(Graphics g) {

    Dimension d = getSize();
    int w = d.width;
    int h = d.height;

    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, h, bottomColor);
    g2.setPaint(gradient);
    g2.fillRect(0, 0, w, h);

    GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
    path.moveTo(0.0f, 0.0f);
    path.lineTo(1, 0.0f);
    path.curveTo(0.2f, 0.1f, 0.3f, 0.8f, 0, 1);
    path.closePath();

    g2.setColor(waveColor);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, WAVE_ALPHA));

    AffineTransform at = new AffineTransform();
    at.setToScale(w * 0.8, h * 0.6);
    g2.transform(at);
    g2.fill(path);

    at.setToScale(0.4, 1.4);
    g2.transform(at);
    g2.fill(path);

  }

  public static void main(String[] args) {
    JPanel panel = new JWavePanel();
    JFrame frame = new JFrame();
    frame.setContentPane(panel);
    frame.setSize(new Dimension(300, 300));
    frame.setVisible(true);
  }
}
