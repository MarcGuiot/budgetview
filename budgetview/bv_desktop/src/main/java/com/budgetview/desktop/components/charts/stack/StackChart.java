package com.budgetview.desktop.components.charts.stack;

import com.budgetview.desktop.components.charts.stack.utils.StackChartAdapter;
import com.budgetview.desktop.utils.Gui;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Key;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

public class StackChart extends JPanel {

  public static final int MAX_LABEL_LENGTH = 20;
  private StackChartDataset leftDataset;
  private StackChartDataset rightDataset;
  private StackChartColors colors;

  private StackSelectionManager selectionManager = new StackSelectionManager();
  private BasicStackClickMap clickAreas = new BasicStackClickMap();

  private boolean rebuildClickAreas;

  private Font labelFont;
  private Font selectedLabelFont;
  private Font barTextFont;

  private static final BasicStroke SELECTION_STROKE = new BasicStroke(2);
  private static final BasicStroke BORDER_STROKE = new BasicStroke(1);
  private static final BasicStroke FLOOR_STROKE = new BasicStroke(1.5f);

  public StackChart() {
    setMinimumSize(new Dimension(190, 40));
    setPreferredSize(new Dimension(190, 1000));
    labelFont = getFont().deriveFont(10f);
    selectedLabelFont = getFont().deriveFont(Font.BOLD);
    barTextFont = getFont().deriveFont(9f);
    registerMouseActions();
  }

  public void addListener(StackChartListener listener) {
    selectionManager.addListener(listener);
  }

  public void update(StackChartDataset leftDataset,
                     StackChartDataset rightDataset,
                     StackChartColors colors) {
    this.leftDataset = leftDataset;
    this.rightDataset = rightDataset;
    this.colors = colors;
    clearClickAreas();
    repaint();
  }

  public void update(StackChartDataset dataset,
                     StackChartColors colors) {
    update(dataset, null, colors);
  }

  public void clear() {
    update(null, null, null);
  }

  private void clearClickAreas() {
    clickAreas.clear();
    selectionManager.clear();
    rebuildClickAreas = true;
  }

  public void setBounds(int x, int y, int width, int height) {
    clearClickAreas();
    super.setBounds(x, y, width, height);
  }

  public void setBounds(Rectangle r) {
    clearClickAreas();
    super.setBounds(r);
  }

  public StackChartDataset getLeftDataset() {
    return leftDataset;
  }

  public StackChartDataset getRightDataset() {
    return rightDataset;
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = getWidth() - 1;
    int height = getHeight() - 1;

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, width, height);
    }

    if ((leftDataset != null) && (rightDataset != null)) {
      StackChartMetrics metrics =
        new StackChartMetrics(height, width,
                              g2.getFontMetrics(labelFont),
                              g2.getFontMetrics(selectedLabelFont),
                              g2.getFontMetrics(barTextFont),
                              Math.max(leftDataset.getTotal(), rightDataset.getTotal()));

      paintBlocks(g2, metrics, leftDataset,
                  metrics.leftLayout(),
                  colors, colors.getLeftBarColor());
      paintBlocks(g2, metrics, rightDataset,
                  metrics.rightLayout(),
                  colors, colors.getRightBarColor());
    }
    else if (leftDataset != null) {
      StackChartMetrics metrics =
        new StackChartMetrics(height, width,
                              g2.getFontMetrics(labelFont),
                              g2.getFontMetrics(selectedLabelFont),
                              g2.getFontMetrics(barTextFont),
                              leftDataset.getTotal());
      paintBlocks(g2, metrics,
                  leftDataset,
                  metrics.centerLayout(Strings.cut(leftDataset.getLongestLabel(), MAX_LABEL_LENGTH)),
                  colors, colors.getLeftBarColor());
    }

    if (colors != null) {
      g2.setStroke(BORDER_STROKE);
      g2.setColor(colors.getBorderColor());
      g2.drawRect(0, 0, width, height);

      g2.setStroke(FLOOR_STROKE);
      g2.setColor(colors.getFloorColor());
      g2.drawLine(0, height, width, height);
    }

    rebuildClickAreas = false;
  }

  private void paintBlocks(Graphics2D g2,
                           StackChartMetrics metrics,
                           StackChartDataset dataset,
                           StackChartLayout layout,
                           StackChartColors colors,
                           Color barColor) {

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    Color blockColor = barColor;
    for (StackChartBlock block : blocks) {

      g2.setColor(isRollover(block) ? Colors.brighten(blockColor, 0.1f) : blockColor);
      g2.fillRect(layout.barX(), block.blockY, metrics.barWidth(), block.blockHeight);

      if (isRollover(block)) {
        setToolTipText(block.tooltipText);
      }

      if (block.selected) {
        g2.setColor(colors.getSelectionBorderColor());
        g2.setStroke(SELECTION_STROKE);
        g2.drawRect(layout.barX(), block.blockY, metrics.barWidth() - 1, block.blockHeight);
      }

      g2.setColor(Colors.getLabelColor(blockColor, colors.getBarTextColor(), Color.DARK_GRAY));
      g2.setFont(barTextFont);
      g2.drawString(block.barText, layout.barTextX(block.barText), block.barTextY);

      g2.setColor(colors.getLabelColor(isRollover(block)));
      g2.setFont(block.selected ? selectedLabelFont : labelFont);
      g2.drawString(block.label, layout.labelTextX(block.label, block.selected), block.labelTextY);

      if (rebuildClickAreas) {
        Rectangle rectangle = new Rectangle(layout.blockX(), block.blockY, layout.blockWidth(), block.blockHeight);
        clickAreas.put(rectangle, block.key, block.label);
      }
      blockColor = Colors.brighten(blockColor, 0.25f);
    }
  }

  private boolean isRollover(StackChartBlock block) {
    return selectionManager.isRollover(block.key);
  }

  public void mouseMoved(int x, int y, boolean expandSelection, boolean rightClick) {
    Key rollover = getSelection(x, y);
    selectionManager.updateRollover(rollover, expandSelection, rightClick);
  }

  private Key getSelection(int x, int y) {
    return clickAreas.getKeyAt(x, y);
  }

  private void registerMouseActions() {
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        boolean addModifier = Gui.isAddModifier(e.getModifiers());
        boolean rightClick = GuiUtils.isRightClick(e);
        selectionManager.startClick(addModifier && !rightClick, rightClick);
      }

      public void mouseEntered(MouseEvent e) {
        selectionManager.clear();
      }

      public void mouseExited(MouseEvent e) {
        selectionManager.clear();
        repaint();
      }
    });

    addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {
        StackChart.this.mouseMoved(e.getX(), e.getY(), true, GuiUtils.isRightClick(e));
      }

      public void mouseMoved(MouseEvent e) {
        StackChart.this.mouseMoved(e.getX(), e.getY(), false, GuiUtils.isRightClick(e));
      }
    });

    selectionManager.addListener(new StackChartAdapter() {
      public void rolloverUpdated(Key key) {
        setCursor(key != null ?
                  Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        repaint();
      }
    });
  }

  // For test purposes
  public Rectangle getArea(String label) {
    return clickAreas.getArea(label);
  }

  // For test purposes
  public List<String> getAreas() {
    return clickAreas.getAllLabels();
  }
}
