package org.designup.picsou.gui.components;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.gui.utils.PicsouColors;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class PicsouTableHeaderPainter implements CellPainter, ColorChangeListener {
  private Color headerLightColor;
  private Color headerMediumColor;
  private Color headerDarkColor;
  private Color headerBorderColor;
  private PicsouColors tableHeaderDark;
  private PicsouColors tableHeaderMedium;
  private PicsouColors tableHeaderLight;
  private PicsouColors tableHeaderBorder;

  public PicsouTableHeaderPainter(Directory directory,
                                  PicsouColors tableHeaderDark,
                                  PicsouColors tableHeaderMedium,
                                  PicsouColors tableHeaderLight,
                                  PicsouColors tableHeaderBorder) {
    this.tableHeaderDark = tableHeaderDark;
    this.tableHeaderMedium = tableHeaderMedium;
    this.tableHeaderLight = tableHeaderLight;
    this.tableHeaderBorder = tableHeaderBorder;
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    headerLightColor = colorLocator.get(tableHeaderLight);
    headerMediumColor = colorLocator.get(tableHeaderMedium);
    headerDarkColor = colorLocator.get(tableHeaderDark);
    headerBorderColor = colorLocator.get(tableHeaderBorder);
  }

  public void paint(Graphics g, Glob glob,
                    int row, int column,
                    boolean isSelected, boolean hasFocus,
                    int width, int height) {

    int adjustedHeight = height - 1;

    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int middleY = adjustedHeight / 2;

    g2.setPaint(new GradientPaint(0, 0, headerLightColor, 0, middleY, headerMediumColor));
    g2.fillRect(0, 0, width, middleY);
    g2.setPaint(new GradientPaint(0, middleY, headerDarkColor, 0, adjustedHeight, headerLightColor));
    g2.fillRect(0, middleY, width, adjustedHeight);

    Rectangle2D rect = new Rectangle2D.Float(0, 0, width, adjustedHeight);
    g2.setColor(headerBorderColor);
    g2.setStroke(new BasicStroke(1.0f));
    g2.draw(rect);
  }
}
