package org.designup.picsou.gui.components.charts.stack;

import org.globsframework.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

public class StackChart extends JPanel {

  private StackChartDataset leftDataset;
  private StackChartDataset rightDataset;
  private StackChartColors colors;

  private Map<Rectangle, StackChartSelection> clickAreas = new HashMap<Rectangle, StackChartSelection>();
  private StackChartSelection currentRollover;
  private boolean rebuildClickAreas;

  private Font labelFont;
  private Font barTextFont;

  private static final BasicStroke SELECTION_STROKE = new BasicStroke(2);
  private static final BasicStroke BORDER_STROKE = new BasicStroke(1);
  private static final BasicStroke FLOOR_STROKE = new BasicStroke(1.5f);

  public StackChart() {
    setMinimumSize(new Dimension(160, 40));
    setPreferredSize(new Dimension(160, 1000));
    labelFont = getFont().deriveFont(10f);
    barTextFont = getFont().deriveFont(9f);
    registerMouseActions();
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
    currentRollover = null;
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
                              g2.getFontMetrics(labelFont), g2.getFontMetrics(barTextFont),
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
                              g2.getFontMetrics(labelFont), g2.getFontMetrics(barTextFont),
                              leftDataset.getTotal());
      paintBlocks(g2, metrics,
                  leftDataset,
                  metrics.centerLayout(leftDataset.getLongestLabel()),
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

    float alpha = 1.0f;
    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    for (StackChartBlock block : blocks) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
      g2.setColor(barColor);
      g2.fillRect(layout.barX(), block.blockY, metrics.barWidth(), block.blockHeight);

      g2.setComposite(AlphaComposite.Src);

      if (block.selected) {
        g2.setColor(colors.getSelectionBorderColor());
        g2.setStroke(SELECTION_STROKE);
        g2.drawRect(layout.barX(), block.blockY, metrics.barWidth(), block.blockHeight);
      }

      g2.setColor(colors.getBarTextColor());
      g2.setFont(barTextFont);
      g2.drawString(block.barText, layout.barTextX(block.barText), block.barTextY);

      g2.setColor(isRollover(block) ? colors.getRolloverTextColor() : colors.getLabelColor());
      g2.setFont(labelFont);
      g2.drawString(block.label, layout.labelTextX(block.label), block.labelTextY);

      if (rebuildClickAreas) {
        Rectangle rectangle = new Rectangle(layout.blockX(), block.blockY, layout.blockWidth(), block.blockHeight);
        StackChartSelection selection = new StackChartSelection(block.dataset, block.datasetIndex);
        clickAreas.put(rectangle, selection);
      }

      alpha *= 0.7f;
    }


  }

  private boolean isRollover(StackChartBlock block) {
    return (currentRollover != null)
           && currentRollover.dataset.equals(block.dataset)
           && (currentRollover.datasetIndex == block.datasetIndex);
  }

  public void click() {
    if (currentRollover != null) {
      currentRollover.getAction().actionPerformed(new ActionEvent(this, 0, "click"));
    }
  }

  public void mouseMoved(int x, int y) {
    StackChartSelection rollover = getSelection(x, y);
    if (Utils.equal(rollover, currentRollover)) {
      return;
    }

    currentRollover = rollover;
    setCursor(currentRollover != null ?
              Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    repaint();
  }

  private StackChartSelection getSelection(int x, int y) {
    for (Map.Entry<Rectangle, StackChartSelection> entry : clickAreas.entrySet()) {
      if (entry.getKey().contains(x, y)) {
        return entry.getValue();
      }
    }
    return null;
  }

  private void registerMouseActions() {
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        StackChart.this.click();
      }

      public void mouseEntered(MouseEvent e) {
        currentRollover = null;
      }

      public void mouseExited(MouseEvent e) {
        currentRollover = null;
        repaint();
      }
    });

    addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {
        StackChart.this.mouseMoved(e.getX(), e.getY());
      }

      public void mouseMoved(MouseEvent e) {
        StackChart.this.mouseMoved(e.getX(), e.getY());
      }
    });
  }


}
