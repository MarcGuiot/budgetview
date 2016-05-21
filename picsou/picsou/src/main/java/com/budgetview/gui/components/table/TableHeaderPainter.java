package com.budgetview.gui.components.table;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class TableHeaderPainter implements CellPainter, ColorChangeListener {
  protected Color lightColor;
  protected Color mediumColor;
  protected Color darkColor;
  protected Color borderColor;
  protected Color filteredLightColor;
  protected Color filteredMediumColor;
  protected Color filteredDarkColor;
  protected Color filteredBorderColor;

  private boolean filtered = false;
  private GlobTableView tableView;
  private ColorService colorService;

  protected TableHeaderPainter(GlobTableView tableView, Directory directory) {
    this.colorService = directory.get(ColorService.class);
    this.colorService.addListener(this);
    this.tableView = tableView;
  }

  public void setFiltered(boolean filtered) {
    this.filtered = filtered;
    this.tableView.getComponent().getTableHeader().repaint();
  }

  public void paint(Graphics g, Glob glob,
                    int row, int column,
                    boolean isSelected, boolean hasFocus,
                    int width, int height) {

    int adjustedHeight = height - 1;

    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int middleY = adjustedHeight / 2;

    g2.setPaint(new GradientPaint(0, 0, getLightColor(), 0, middleY, getMediumColor()));
    g2.fillRect(0, 0, width, middleY);
    g2.setPaint(new GradientPaint(0, middleY, getDarkColor(), 0, adjustedHeight, getLightColor()));
    g2.fillRect(0, middleY, width, adjustedHeight);

    Rectangle2D rect = new Rectangle2D.Float(0, 0, width, adjustedHeight);
    g2.setColor(getBorderColor());
    g2.setStroke(new BasicStroke(1.0f));
    g2.draw(rect);
  }

  private Color getBorderColor() {
    return filtered ? filteredBorderColor : borderColor;
  }

  private Color getLightColor() {
    return filtered ? filteredLightColor : lightColor;
  }

  private Color getMediumColor() {
    return filtered ? filteredMediumColor : mediumColor;
  }

  private Color getDarkColor() {
    return filtered ? filteredDarkColor : darkColor;
  }

  public void dispose() {
    colorService.removeListener(this);
  }
}
