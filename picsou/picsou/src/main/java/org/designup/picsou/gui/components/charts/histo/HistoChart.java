package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.SortedSet;
import java.util.TreeSet;

public class HistoChart extends JPanel {

  private HistoChartColors colors;
  private HistoPainter painter = HistoPainter.NULL;
  private HistoChartListener listener;
  private HistoChartMetrics metrics;
  private Integer currentRolloverIndex;
  private Font selectedLabelFont;
  private boolean drawLabels;
  private Integer columnSelectionMinIndex;
  private Integer columnSelectionMaxIndex;
  private boolean clickable;

  public HistoChart(boolean drawLabels, boolean clickable, Directory directory) {
    this.drawLabels = drawLabels;
    this.clickable = clickable;
    this.colors = new HistoChartColors(directory);
    setFont(getFont().deriveFont(9f));
    this.selectedLabelFont = getFont().deriveFont(Font.BOLD);
    registerMouseActions();
  }

  public void setListener(HistoChartListener listener) {
    this.listener = listener;
  }

  public void update(HistoPainter painter) {
    this.painter = painter;
    this.currentRolloverIndex = null;
    this.metrics = null;
    repaint();
  }

  public void clear() {
    update(HistoPainter.NULL);
  }

  public void setBounds(int x, int y, int width, int height) {
    metrics = null;
    super.setBounds(x, y, width, height);
  }

  public void setBounds(Rectangle r) {
    metrics = null;
    super.setBounds(r);
  }

  public HistoDataset getCurrentDataset() {
    return painter.getDataset();
  }

  public int getX(int columnIndex) {
    if (metrics == null) {
      return -1;
    }
    return (metrics.left(columnIndex) + metrics.right(columnIndex)) / 2;
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int panelWidth = getWidth() - 1;
    int panelHeight = getHeight() - 1;

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, panelWidth, panelHeight);
    }

    HistoDataset dataset = painter.getDataset();

    if (metrics == null) {
      metrics = new HistoChartMetrics(panelWidth, panelHeight,
                                      getFontMetrics(getFont()),
                                      dataset.size(),
                                      dataset.getMaxNegativeValue(), dataset.getMaxPositiveValue(),
                                      drawLabels,
                                      dataset.containsSections());
    }

    paintBg(g2);
    paintLabels(g2, dataset);
    paintScale(g2, panelWidth);
    paintSections(g2, panelHeight, dataset);
    paintBorder(g2);

    painter.paint(g2, metrics, currentRolloverIndex);
  }

  private void paintBg(Graphics2D g2) {
    g2.setColor(colors.getChartBgColor());
    g2.fillRect(metrics.chartX(), 0, metrics.chartWidth(), metrics.chartHeight());
  }

  private void paintBorder(Graphics2D g2) {
    g2.setColor(colors.getChartBorderColor());
    g2.drawRect(metrics.chartX(), 0, metrics.chartWidth(), metrics.chartHeight());
  }

  private void paintLabels(Graphics2D g2, HistoDataset dataset) {
    g2.setStroke(new BasicStroke(1));
    for (int i = 0; i < dataset.size(); i++) {
      if (drawLabels) {
        String label = dataset.getLabel(i);
        g2.setFont(getLabelFont(dataset, i));
        g2.setColor(getLabelColor(i));
        g2.drawString(label, metrics.labelX(label, i), metrics.labelY());
      }

      if (dataset.isSelected(i)) {
        g2.setColor(colors.getSelectedColumnColor());
        g2.fillRect(metrics.left(i), 0, metrics.columnWidth(), metrics.columnHeight());
      }
    }
  }

  private void paintSections(Graphics2D g2, int panelHeight, HistoDataset dataset) {
    if (!drawLabels) {
      return;
    }
    g2.setStroke(new BasicStroke(1));
    boolean firstBlock = true;
    for (HistoChartMetrics.Section section : metrics.getSections(dataset)) {
      if (!firstBlock) {
        g2.setColor(colors.getSectionLineColor());
        g2.drawLine(section.blockX, 0, section.blockX, panelHeight);
      }

      g2.setColor(colors.getLabelColor());
      g2.drawString(section.text, section.textX, section.textY);

      firstBlock = false;
    }
  }

  private void paintScale(Graphics2D g2, int panelWidth) {
    double[] scaleValues = metrics.scaleValues();
    for (double scaleValue : scaleValues) {

      g2.setColor(colors.getScaleLineColor());
      g2.drawLine(metrics.chartX(), metrics.y(scaleValue), panelWidth, metrics.y(scaleValue));

      if (drawLabels) {
        g2.setColor(colors.getLabelColor());
        String label = Integer.toString((int)scaleValue);
        g2.drawString(label, metrics.scaleX(label), metrics.scaleY(scaleValue));
      }
    }
  }

  public void startClick() {
    if (!clickable()) {
      return;
    }

    columnSelectionMinIndex = null;
    columnSelectionMaxIndex = null;

    HistoDataset dataset = painter.getDataset();
    if (currentRolloverIndex < dataset.size()) {
      addColumnIndexToSelection(currentRolloverIndex);
    }
    else {
      notifyColumnSelection();
    }
  }

  private boolean clickable() {
    return clickable && (currentRolloverIndex != null) && (listener != null) && (painter != null);
  }

  public void mouseMoved(int x, boolean dragging) {
    if (metrics == null) {
      return;
    }

    Integer columnIndex = metrics.getColumnAt(x);
    if (Utils.equal(columnIndex, currentRolloverIndex)) {
      return;
    }

    currentRolloverIndex = columnIndex;
    setCursor(clickable && (currentRolloverIndex != null) ?
              Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    setToolTipText(painter.getDataset().getTooltip(currentRolloverIndex));

    if (dragging) {
      addColumnIndexToSelection(currentRolloverIndex);
    }

    repaint();
  }

  private void addColumnIndexToSelection(Integer index) {
    boolean selectionChanged = false;
    if ((columnSelectionMinIndex == null) || (index < columnSelectionMinIndex)) {
      columnSelectionMinIndex = index;
      selectionChanged = true;
    }
    if ((columnSelectionMaxIndex == null) || (index > columnSelectionMaxIndex)) {
      columnSelectionMaxIndex = index;
      selectionChanged = true;
    }
    if (selectionChanged) {
      notifyColumnSelection();
    }
  }

  private void notifyColumnSelection() {
    if ((columnSelectionMinIndex == null) || (columnSelectionMaxIndex == null)) {
      return;
    }

    SortedSet<Integer> ids = new TreeSet<Integer>();
    HistoDataset currentDataset = painter.getDataset();
    for (Integer columnIndex : Utils.range(columnSelectionMinIndex, columnSelectionMaxIndex)) {
      int id = currentDataset.getId(columnIndex);
      if (id >= 0) {
        ids.add(id);
      }
    }
    listener.columnsClicked(ids);
  }

  private void registerMouseActions() {
    addMouseListener(new MouseAdapter() {

      public void mousePressed(MouseEvent e) {
        HistoChart.this.startClick();
      }

      public void mouseEntered(MouseEvent e) {
        currentRolloverIndex = null;
      }

      public void mouseExited(MouseEvent e) {
        currentRolloverIndex = null;
        repaint();
      }
    });

    addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {
        HistoChart.this.mouseMoved(e.getX(), true);
      }

      public void mouseMoved(MouseEvent e) {
        HistoChart.this.mouseMoved(e.getX(), false);
      }
    });
  }

  private Font getLabelFont(HistoDataset dataset, int columnIndex) {
    if (dataset.isSelected(columnIndex)) {
      return selectedLabelFont;
    }
    else {
      return getFont();
    }
  }

  private Color getLabelColor(int columnIndex) {
    boolean isRollover = (currentRolloverIndex != null) && (currentRolloverIndex == columnIndex);
    if (isRollover) {
      return colors.getRolloverLabelColor();
    }
    else {
      return colors.getLabelColor();
    }
  }
}
