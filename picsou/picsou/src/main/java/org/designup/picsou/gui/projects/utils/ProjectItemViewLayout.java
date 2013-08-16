package org.designup.picsou.gui.projects.utils;

import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;

public class ProjectItemViewLayout implements LayoutManager {
  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container target) {
    Metrics metrics = new Metrics(target);
    return new Dimension(Integer.MAX_VALUE, metrics.getHeight());
  }

  public Dimension minimumLayoutSize(Container target) {
    Metrics metrics = new Metrics(target);
    return new Dimension(100, metrics.getHeight());
  }

  public void layoutContainer(Container parent) {
    Metrics metrics = initMetrics(parent);
    metrics.layoutComponents(parent);
  }

  private Metrics initMetrics(Container parent) {
    return new Metrics(parent);
  }

  private static class Metrics {

    private static final int VERTICAL_MARGIN = 5;
    private static final int IMAGE_LEFT_MARGIN = 20;
    private static final int IMAGE_RIGHT_MARGIN = 10;
    private static final int SPACE = 5;
    private static int GAUGE_WIDTH = 40;
    private static int GAUGE_HEIGHT = 15;

    private final int top;
    private final int left;
    private final int right;

    private Dimension itemButtonSize;
    private int itemButtonLeft;
    private int itemButtonMaxWidth;
    private int itemButtonTop;
    private Dimension monthLabelSize;
    private int monthLabelLeft;
    private int monthLabelTop;
    private Dimension actualLabelSize;
    private int actualLabelWidth;
    private int actualLabelLeft;
    private int actualLabelTop;
    private Dimension gaugeSize;
    private int gaugeLeft;
    private int gaugeTop;
    private Dimension plannedLabelSize;
    private int plannedLabelLeft;
    private int plannedLabelTop;
    private Dimension modifyButtonSize;
    private int modifyButtonLeft;
    private int modifyButtonTop;
    private Dimension imageLabelSize;
    private int imageRightMargin;
    private int imageLabelLeft;
    private int imageLabelTop;
    private int imageVerticalMargin = 0;
    private Dimension linkSize;
    private int linkLeft;
    private int linkTop = 0;
    private int linkVerticalMargin = 0;
    private Dimension descriptionSize;
    private int descriptionLeft;
    private int descriptionTop;
    private int firstRowBottom;
    private final int insetsHeight;

    public Metrics(Container target) {
      Insets insets = target.getInsets();
      top = insets.top;
      left = insets.left;
      right = target.getWidth() - insets.right;
      insetsHeight = insets.top + insets.bottom;
      init(target);
    }

    private void init(Container parent) {
      for (Component component : parent.getComponents()) {
        if (component.getName() == null) {
          throw new InvalidParameter("Unexpected component with no name: " + component);
        }
        if (component.getName().equals("itemButton")) {
          FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
          itemButtonSize = component.getPreferredSize();
          itemButtonSize.height = fontMetrics.getAscent() + fontMetrics.getDescent();
          firstRowBottom = top + fontMetrics.getAscent();
        }
        else if (component.getName().equals("monthLabel")) {
          monthLabelSize = GuiUtils.maxSize(component, "Aaaaa 2013");
        }
        else if (component.getName().equals("actualLabel")) {
          actualLabelSize = GuiUtils.maxSize(component, "000.000.00");
          actualLabelWidth = component.getPreferredSize().width;
        }
        else if (component.getName().equals("itemGauge")) {
          gaugeSize = new Dimension(GAUGE_WIDTH, GAUGE_HEIGHT);
        }
        else if (component.getName().equals("plannedLabel")) {
          plannedLabelSize = GuiUtils.maxSize(component, "000.000.00");
        }
        else if (component.getName().equals("modify")) {
          modifyButtonSize = GuiUtils.maxSize(component, "Modify");
        }
        else if (component.getName().equals("imageLabel")) {
          JLabel label = (JLabel)component;
          if (component.isVisible() && label.getIcon() != null) {
            imageLabelSize = component.getPreferredSize();
            imageRightMargin = IMAGE_RIGHT_MARGIN;
            imageVerticalMargin = VERTICAL_MARGIN;
          }
          else {
            imageLabelSize = new Dimension(0, 0);
          }
        }
        else if (component.getName().equals("link")) {
          JButton button = (JButton)component;
          if (Strings.isNotEmpty(button.getText()) && component.isVisible()) {
            linkSize = component.getPreferredSize();
            linkVerticalMargin = VERTICAL_MARGIN;
          }
          else {
            linkSize = new Dimension(0, 0);
            linkVerticalMargin = 0;
          }
        }
        else if (component.getName().equals("description")) {
          if (component.isVisible()) {
            descriptionSize = component.getPreferredSize();
          }
          else {
            descriptionSize = new Dimension(0, 0);
          }
        }
      }

      itemButtonLeft = left;
      itemButtonTop = firstRowBottom - itemButtonSize.height;
      modifyButtonLeft = right - modifyButtonSize.width;
      modifyButtonTop = firstRowBottom - modifyButtonSize.height;
      plannedLabelLeft = modifyButtonLeft - SPACE - plannedLabelSize.width;
      plannedLabelTop = firstRowBottom - plannedLabelSize.height;
      gaugeLeft = plannedLabelLeft - SPACE - gaugeSize.width;
      gaugeTop = firstRowBottom - gaugeSize.height;
      actualLabelLeft = gaugeLeft - SPACE - actualLabelWidth;
      actualLabelTop = firstRowBottom - actualLabelSize.height;
      monthLabelLeft = gaugeLeft - SPACE - actualLabelSize.width - SPACE - monthLabelSize.width;
      monthLabelTop = firstRowBottom - monthLabelSize.height;
      itemButtonMaxWidth = monthLabelLeft - itemButtonLeft;
      imageLabelLeft = left + IMAGE_LEFT_MARGIN;
      imageLabelTop = firstRowBottom + imageVerticalMargin;
      linkLeft = imageLabelLeft + imageLabelSize.width + imageRightMargin;
      linkTop = firstRowBottom + linkVerticalMargin;
      descriptionLeft = linkLeft;
      descriptionTop = linkTop + linkSize.height + linkVerticalMargin;
    }

    public int getHeight() {
      return insetsHeight
             + itemButtonSize.height
             + Math.max(imageVerticalMargin + imageLabelSize.height,
                        linkVerticalMargin + linkSize.height + linkVerticalMargin + descriptionSize.height);
    }

    public void layoutComponents(Container parent) {
      for (Component component : parent.getComponents()) {
        if (component.getName().equals("itemButton")) {
          component.setBounds(itemButtonLeft, itemButtonTop,
                              itemButtonMaxWidth, itemButtonSize.height);
        }
        else if (component.getName().equals("monthLabel")) {
          component.setBounds(monthLabelLeft, monthLabelTop,
                              monthLabelSize.width, monthLabelSize.height);
        }
        else if (component.getName().equals("actualLabel")) {
          component.setBounds(actualLabelLeft, actualLabelTop,
                              actualLabelSize.width, actualLabelSize.height);
        }
        else if (component.getName().equals("itemGauge")) {
          component.setBounds(gaugeLeft, gaugeTop,
                              gaugeSize.width, gaugeSize.height);
        }
        else if (component.getName().equals("plannedLabel")) {
          component.setBounds(plannedLabelLeft, plannedLabelTop,
                              plannedLabelSize.width, plannedLabelSize.height);
        }
        else if (component.getName().equals("modify")) {
          component.setBounds(modifyButtonLeft, modifyButtonTop,
                              modifyButtonSize.width, modifyButtonSize.height);
        }
        else if (component.getName().equals("imageLabel")) {
          component.setBounds(imageLabelLeft, imageLabelTop,
                              imageLabelSize.width, imageLabelSize.height);
        }
        else if (component.getName().equals("link")) {
          component.setBounds(linkLeft, linkTop,
                              linkSize.width, linkSize.height);
        }
        else if (component.getName().equals("description")) {
          component.setBounds(descriptionLeft, descriptionTop,
                              descriptionSize.width, descriptionSize.height);
        }
        else {
          throw new InvalidParameter("Unexpected component found: " + component);
        }
      }
    }
  }
}
