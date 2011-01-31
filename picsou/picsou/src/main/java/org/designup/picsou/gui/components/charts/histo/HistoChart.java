package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

public class HistoChart extends JPanel {

  private HistoChartColors colors;
  private HistoPainter painter = HistoPainter.NULL;
  private java.util.List<HistoChartListener> listeners;
  private java.util.List<HistoChartListener> doubleClickListeners;
  private HistoChartMetrics metrics;
  private Integer currentRolloverIndex;
  private Font selectedLabelFont;
  private Font sectionLabelFont;
  private Integer columnSelectionMinIndex;
  private Integer columnSelectionMaxIndex;
  private boolean drawLabels;
  private boolean drawSections;
  private boolean drawInnerLabels;
  private boolean clickable;
  private boolean snapToScale;

  public static final BasicStroke SCALE_STROKE = new BasicStroke(1);
  public static final BasicStroke SCALE_ORIGIN_STROKE = new BasicStroke(1);

  public HistoChart(boolean drawLabels, boolean drawSections, boolean drawInnerLabels, boolean clickable, Directory directory) {
    this.drawLabels = drawLabels;
    this.drawSections = drawSections;
    this.drawInnerLabels = drawInnerLabels;
    this.clickable = clickable;
    this.colors = new HistoChartColors(directory);
    setFont(getFont().deriveFont(9f));
    this.selectedLabelFont = getFont().deriveFont(Font.BOLD);
    this.sectionLabelFont = getFont().deriveFont(11f).deriveFont(Font.BOLD);
    registerMouseActions();
  }

  public void addListener(HistoChartListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<HistoChartListener>();
    }
    this.listeners.add(listener);
  }

  public void addDoubleClickListener(HistoChartListener listener) {
    if (doubleClickListeners == null) {
      doubleClickListeners = new ArrayList<HistoChartListener>();
    }
    this.doubleClickListeners.add(listener);
  }

  public void setSnapToScale(boolean value) {
    this.snapToScale = value;
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
                                      drawSections && dataset.containsSections(),
                                      drawInnerLabels,
                                      snapToScale);
    }

    paintBg(g2);
    paintLabels(g2, dataset);
    paintScale(g2, panelWidth, dataset);
    paintSections(g2, dataset);
    paintSelectionBorder(g2, dataset);
    paintBorder(g2);

    g2.setFont(getFont());
    painter.paint(g2, metrics, currentRolloverIndex);
  }

  private void paintBg(Graphics2D g2) {
    g2.setColor(colors.getChartBgColor());
    g2.fillRect(metrics.chartX(), metrics.columnTop(), metrics.chartWidth(), metrics.chartHeight());
  }

  private void paintBorder(Graphics2D g2) {
    g2.setColor(colors.getChartBorderColor());
    g2.drawRect(metrics.chartX(), metrics.columnTop(), metrics.chartWidth(), metrics.chartHeight());
  }

  private void paintLabels(Graphics2D g2, HistoDataset dataset) {
    g2.setStroke(new BasicStroke(1));
    for (int i = 0; i < dataset.size(); i++) {
      if (dataset.isSelected(i)) {

        int columnHeight = metrics.columnHeight();
        int left = metrics.left(i);

        g2.setColor(colors.getSelectedColumnColor());
        g2.fillRect(left, metrics.columnTop(), metrics.columnWidth(), columnHeight);

        g2.setColor(colors.getSelectedLabelBackgroundColor());
        g2.fillRect(left, metrics.labelTop(), metrics.columnWidth(), metrics.labelZoneHeightWithMargin());
      }

      if (drawLabels) {
        String label = dataset.getLabel(i);
        g2.setFont(getLabelFont(dataset, i));
        g2.setColor(getLabelColor(i));
        g2.drawString(label, metrics.labelX(label, i), metrics.labelY());
      }
    }
  }

  private void paintSelectionBorder(Graphics2D g2, HistoDataset dataset) {
    g2.setStroke(new BasicStroke(1));
    for (int i = 0; i < dataset.size(); i++) {
      if (dataset.isSelected(i)) {

        int columnHeight = metrics.columnHeight();
        int left = metrics.left(i);

        g2.setColor(colors.getSelectedColumnBorder());
        g2.drawRect(left, metrics.columnTop(), metrics.columnWidth(), columnHeight + metrics.labelZoneHeightWithMargin());
      }
    }
  }

  private void paintSections(Graphics2D g2, HistoDataset dataset) {
    if (!drawLabels) {
      return;
    }
    g2.setStroke(new BasicStroke(1));
    boolean firstBlock = true;
    g2.setFont(sectionLabelFont);
    for (HistoChartMetrics.Section section : metrics.getSections(dataset)) {
      if (!firstBlock) {
        g2.setColor(colors.getSectionLineColor());
        g2.drawLine(section.blockX, section.lineY, section.blockX, section.lineHeight);
      }

      g2.setColor(colors.getLabelColor());
      g2.drawString(section.text, section.textX, section.textY);

      firstBlock = false;
    }
  }

  private void paintScale(Graphics2D g2, int panelWidth, HistoDataset dataset) {
    double[] scaleValues = metrics.scaleValues();
    double min = dataset.getMaxNegativeValue();
    double max = dataset.getMaxPositiveValue();
    for (double scaleValue : scaleValues) {
      if ((scaleValue == 0) && (scaleValue != min) && (scaleValue != max)) {
        g2.setStroke(SCALE_ORIGIN_STROKE);
        g2.setColor(colors.getScaleOriginLineColor());
      }
      else {
        g2.setStroke(SCALE_STROKE);
        g2.setColor(colors.getScaleLineColor());
      }
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
    return clickable && (currentRolloverIndex != null) && (listeners != null) && (painter != null)
           && painter.getDataset().size() > 0;
  }

  public void mouseMoved(int x, boolean dragging) {
    if (metrics == null) {
      return;
    }

    Integer columnIndex = metrics.getColumnAt(x);
    if (Utils.equal(columnIndex, currentRolloverIndex)) {
      return;
    }

    if (clickable) {
      currentRolloverIndex = columnIndex;
      setCursor((currentRolloverIndex != null) ?
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      setToolTipText(painter.getDataset().getTooltip(currentRolloverIndex));
    }

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
    for (HistoChartListener listener : listeners) {
      listener.columnsClicked(ids);
    }
  }

  private void registerMouseActions() {
    addMouseListener(new MouseAdapter() {

      public void mousePressed(MouseEvent e) {
        if (isEnabled()) {
          HistoChart.this.startClick();
        }
      }

      public void mouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
        }
        super.mouseReleased(mouseEvent);
      }

      public void mouseEntered(MouseEvent e) {
        if (isEnabled()) {
          currentRolloverIndex = null;
        }
      }

      public void mouseExited(MouseEvent e) {
        if (isEnabled()) {
          currentRolloverIndex = null;
          repaint();
        }
      }
    });

    addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {
        if (isEnabled()) {
          HistoChart.this.mouseMoved(e.getX(), true);
        }
      }

      public void mouseMoved(MouseEvent e) {
        if (isEnabled()) {
          HistoChart.this.mouseMoved(e.getX(), false);
        }
      }
    });
    addMouseWheelListener(new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        for (HistoChartListener listener : listeners) {
          listener.scroll(e.getWheelRotation());
        }
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
