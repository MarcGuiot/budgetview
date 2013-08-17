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

    private static final String MAX_AMOUNT_STRING = "-0.000.00";

    private static int GAUGE_WIDTH = 60;
    private static int GAUGE_HEIGHT = 15;

    private final int top;
    private final int left;
    private final int right;

    private Dimension itemButtonSize;
    private int itemButtonLeft;
    private int itemButtonTop;
    private Dimension monthLabelSize;
    private int monthLabelLeft;
    private int monthLabelTop;
    private Dimension actualAmountSize;
    private int actualAmountRealWidth;
    private int actualAmountLeft;
    private int actualAmountTop;
    private Dimension slashLabelSize;
    private int slashLabelLeft;
    private int slashLabelTop;
    private Dimension gaugeSize;
    private int gaugeLeft;
    private int gaugeTop;
    private Dimension plannedAmountSize;
    private int plannedAmountLeft;
    private int plannedAmountTop;
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
          JButton button = (JButton)component;
          FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
          itemButtonSize = new Dimension();
          itemButtonSize.width = button.getIcon().getIconWidth() + button.getIconTextGap() + fontMetrics.stringWidth(button.getText());
          firstRowBottom = top + fontMetrics.getAscent();
          itemButtonSize.height = fontMetrics.getAscent() + fontMetrics.getDescent();
        }
        else if (component.getName().equals("monthLabel")) {
          monthLabelSize = GuiUtils.maxSize(component, "Aaaaa 2013");
        }
        else if (component.getName().equals("actualAmount")) {
          actualAmountSize = GuiUtils.maxSize(component, MAX_AMOUNT_STRING);
          FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
          JButton button = (JButton)component;
          actualAmountRealWidth = fontMetrics.stringWidth(button.getText());
        }
        else if (component.getName().equals("itemGauge")) {
          gaugeSize = new Dimension(GAUGE_WIDTH, GAUGE_HEIGHT);
        }
        else if (component.getName().equals("plannedAmount")) {
          plannedAmountSize = GuiUtils.maxSize(component, MAX_AMOUNT_STRING);
        }
        else if (component.getName().equals("slashLabel")) {
          slashLabelSize = component.getPreferredSize();
        }
        else if (component.getName().equals("modify")) {
          modifyButtonSize = component.getPreferredSize();
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
      plannedAmountLeft = modifyButtonLeft - SPACE - plannedAmountSize.width;
      plannedAmountTop = firstRowBottom - plannedAmountSize.height;
      slashLabelLeft = plannedAmountLeft - SPACE - slashLabelSize.width;
      slashLabelTop = firstRowBottom - slashLabelSize.height;
      actualAmountLeft = slashLabelLeft - SPACE - actualAmountRealWidth;
      actualAmountTop = firstRowBottom - actualAmountSize.height;
      gaugeLeft = slashLabelLeft - SPACE - actualAmountSize.width - SPACE - gaugeSize.width;
      gaugeTop = firstRowBottom - gaugeSize.height;
      monthLabelLeft = gaugeLeft - SPACE - monthLabelSize.width;
      monthLabelTop = firstRowBottom - monthLabelSize.height;
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
                              itemButtonSize.width, itemButtonSize.height);
        }
        else if (component.getName().equals("monthLabel")) {
          component.setBounds(monthLabelLeft, monthLabelTop,
                              monthLabelSize.width, monthLabelSize.height);
        }
        else if (component.getName().equals("actualAmount")) {
          component.setBounds(actualAmountLeft, actualAmountTop,
                              actualAmountSize.width, actualAmountSize.height);
        }
        else if (component.getName().equals("slashLabel")) {
          component.setBounds(slashLabelLeft, slashLabelTop,
                              slashLabelSize.width, slashLabelSize.height);
        }
        else if (component.getName().equals("itemGauge")) {
          component.setBounds(gaugeLeft, gaugeTop,
                              gaugeSize.width, gaugeSize.height);
        }
        else if (component.getName().equals("plannedAmount")) {
          component.setBounds(plannedAmountLeft, plannedAmountTop,
                              plannedAmountSize.width, plannedAmountSize.height);
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
