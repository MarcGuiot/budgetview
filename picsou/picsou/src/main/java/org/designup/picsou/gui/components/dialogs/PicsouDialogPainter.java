package org.designup.picsou.gui.components.dialogs;

import org.designup.picsou.gui.utils.ApplicationColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Java2DUtils;

import javax.swing.*;
import java.awt.*;

public class PicsouDialogPainter implements ColorChangeListener {

  private Color topColor;
  private Color bottomColor;
  private Color borderColor;
  private ColorService colorService;

  public PicsouDialogPainter() {
    colorService = (ColorService)UIManager.get("ColorService");
    colorService.addListener(this);
    topColor = colorService.get(ApplicationColors.DIALOG_BG_TOP);
    bottomColor = colorService.get(ApplicationColors.DIALOG_BG_BOTTOM);
    borderColor = colorService.get(ApplicationColors.DIALOG_BORDER);
  }

  public Color getBorderColor() {
    return borderColor;
  }

  public Color getBottomColor() {
    return bottomColor;
  }

  public Color getTopColor() {
    return topColor;
  }

  public void paint(Graphics g, int width, int height) {
    Graphics2D g2d = (Graphics2D)g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, height, bottomColor);
    g2d.setPaint(gradient);

    g2d.fillRect(0, 0, width, height);

    Java2DUtils.drawBorder(g2d, borderColor, 0, 0, width - 1, height - 1);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    topColor = colorLocator.get(ApplicationColors.DIALOG_BG_TOP);
    bottomColor = colorLocator.get(ApplicationColors.DIALOG_BG_BOTTOM);
    borderColor = colorLocator.get(ApplicationColors.DIALOG_BORDER);
  }

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(this);
  }
}
