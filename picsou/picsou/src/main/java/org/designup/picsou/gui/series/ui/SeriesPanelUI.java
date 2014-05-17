package org.designup.picsou.gui.series.ui;

import org.designup.picsou.gui.budget.BudgetAreaSeriesView;
import org.designup.picsou.gui.budget.components.BudgetAreaSeriesLayout;
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
    boolean isGroupItemSequence = false;
    Component lastActiveToggle = null;
    Integer topY = null;
    Component[] components = panel.getComponents();
    for (int i = 0; i < components.length; i++) {
      Component component = components[i];
      if ("groupToggle".equals(component.getName())) {
        JButton groupToggle = (JButton)component;
        boolean isGroupItem = isGroupItem(groupToggle);
        if (isGroupItem) {
          isGroupItemSequence = true;
          lastActiveToggle = groupToggle;
          if (topY == null) {
            Rectangle bounds = component.getBounds();
            topY = bounds.y;
          }
          if (components.length - i < 7) {
            drawBackground(g2, lastActiveToggle, c.getWidth(), topY, component.getY() - topY + BudgetAreaSeriesLayout.ROW_HEIGHT);
            return;
          }
        }
        else if (isGroupItemSequence) {
          drawBackground(g2, lastActiveToggle, c.getWidth(), topY, component.getY() - topY - 2);
          isGroupItemSequence = false;
          topY = null;
        }
      }
    }
  }

  private void drawBackground(Graphics2D g2, Component lastToggle, int width, Integer topY, int height) {
    g2.setColor(groupBackground);
    g2.fillRoundRect(0, topY, width, height, 5, 5);

    GeneralPath shape = new GeneralPath();
    shape.moveTo(0, 10);
    shape.lineTo(5, 0);
    shape.lineTo(10, 10);
    shape.closePath();

    Java2DUtils.resize(shape, 10, 6);

    Rectangle shapeBounds = shape.getBounds();
    shape.transform(getTranslateInstance((double)(lastToggle.getX() + lastToggle.getWidth() / 2 - shapeBounds.x),
                                         (double)(topY - shapeBounds.height - shapeBounds.y)));
    g2.fill(shape);
  }

  public String[] getGroupItemLabels(JPanel panel) {
    java.util.List<String> result = new ArrayList<String>();
    Component[] components = panel.getComponents();
    for (int i = 0; i < components.length; i++) {
      Component component = components[i];
      if ("seriesName".equals(component.getName())) {
        JButton button = (JButton)component;
        JButton toggle = (JButton)components[i + 1];
        if (isGroupItem(toggle)) {
          result.add(button.getText());
        }
      }
    }
    return result.toArray(new String[result.size()]);
  }

  public boolean isGroupItem(JButton toggle) {
    return Boolean.TRUE.equals(toggle.getClientProperty(BudgetAreaSeriesView.IS_GROUP_ELEMENT_PROPERTY));
  }

}
