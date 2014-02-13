package org.designup.picsou.gui.series.ui;

import org.designup.picsou.gui.budget.BudgetAreaSeriesView;
import org.globsframework.gui.splits.utils.Java2DUtils;

import javax.swing.*;
import javax.swing.plaf.basic.BasicPanelUI;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import static java.awt.geom.AffineTransform.getTranslateInstance;

public class SeriesPanelUI extends BasicPanelUI {

  private Color panelBackground = Color.RED;
  private Color groupBackground = Color.RED;

  public void setPanelBackground(Color panelBackground) {
    this.panelBackground = panelBackground;
  }

  public void setGroupBackground(Color groupBackground) {
    this.groupBackground = groupBackground;
  }

  public void paint(Graphics g, JComponent c) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2.setColor(panelBackground);
    g2.fillRect(0, 0, c.getWidth() - 1, c.getHeight() - 1);
    JPanel panel = (JPanel)c;
    boolean isGroupElement = false;
    Integer topY = null;
    for (Component component : panel.getComponents()) {
      if ("seriesName".equals(component.getName())) {
        JButton button = (JButton)component;
        if (isGroupItem(button)) {
          isGroupElement = true;
          if (topY == null) {
            Rectangle bounds = component.getBounds();
            topY = bounds.y;
          }
        }
        else {
          if (isGroupElement) {
            drawBackground(g2, component, c.getWidth(), topY);
          }
          isGroupElement = false;
          topY = null;
        }
      }
    }
  }

  private void drawBackground(Graphics2D g2, Component component, int width, Integer topY) {
    Rectangle bounds = component.getBounds();
    g2.setColor(groupBackground);
    g2.fillRoundRect(0, topY, width, bounds.y - topY - 2, 5, 5);

    GeneralPath shape = new GeneralPath();
    shape.moveTo(0, 10);
    shape.lineTo(5, 0);
    shape.lineTo(10, 10);
    shape.closePath();

    Java2DUtils.resize(shape, 10, 6);

    Rectangle shapeBounds = shape.getBounds();
    shape.transform(getTranslateInstance((double)(component.getX() + component.getWidth() - shapeBounds.x - 10),
                                         (double)(topY - shapeBounds.height - shapeBounds.y)));
    g2.fill(shape);
  }

  public String[] getGroupItemLabels(JPanel panel) {
    java.util.List<String> result = new ArrayList<String>();
    for (Component component : panel.getComponents()) {
      if ("seriesName".equals(component.getName())) {
        JButton button = (JButton)component;
        if (isGroupItem(button)) {
          result.add(button.getText());
        }
      }
    }
    return result.toArray(new String[result.size()]);
  }

  public boolean isGroupItem(JButton button) {
    return Boolean.TRUE.equals(button.getClientProperty(BudgetAreaSeriesView.IS_GROUP_ELEMENT_PROPERTY));
  }

}
