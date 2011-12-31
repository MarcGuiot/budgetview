package org.designup.picsou.gui.components.charts.histo;

import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HistoChart extends JPanel {

  private HistoChartColors colors;
  private HistoPainter painter = HistoPainter.NULL;
  private HistoChartMetrics metrics;
  private Font selectedLabelFont;
  private Font sectionLabelFont;
  private boolean snapToScale;
  private HistoChartConfig config;

  private HistoSelectionManager selectionManager;

  public static final BasicStroke SCALE_STROKE = new BasicStroke(1);
  public static final BasicStroke SCALE_ORIGIN_STROKE = new BasicStroke(1.2f);

  public HistoChart(HistoChartConfig config, Directory directory) {
    this.config = config;
    this.selectionManager = new HistoSelectionManager();
    this.colors = new HistoChartColors(directory);
    setFont(getFont().deriveFont(9f));
    this.selectedLabelFont = getFont().deriveFont(Font.BOLD);
    this.sectionLabelFont = getFont().deriveFont(11f).deriveFont(Font.BOLD);
    registerMouseActions();
    selectionManager.addListener(new HistoChartListenerAdapter() {
      public void rolloverUpdated(HistoRollover rollover) {
        if (rollover.getColumnIndex() != null) {
          setToolTipText(painter.getDataset().getTooltip(rollover.getColumnIndex(), rollover.getObjectKey()));
        }
        setCursor(Cursor.getPredefinedCursor(rollover.isActive() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
      }
    });
  }

  public void addListener(HistoChartListener listener) {
    selectionManager.addListener(listener);
  }

  // For testing
  public HistoSelectionManager getSelectionManager() {
    return selectionManager;
  }

  public void setSnapToScale(boolean value) {
    this.snapToScale = value;
  }

  public void update(HistoPainter painter) {
    this.painter = painter;
    selectionManager.resetRollover(painter.getDataset());
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
                                      config,
                                      dataset.containsSections(),
                                      snapToScale);
    }

    paintBg(g2);
    paintLabels(g2, dataset);
    paintScale(g2, panelWidth);
    paintSelectionBorder(g2, dataset);
    paintBorder(g2);

    g2.setFont(getFont());
    painter.paint(g2, metrics, config, selectionManager.getRollover());

    paintSections(g2, dataset);
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
    g2.setComposite(AlphaComposite.Src);
    for (int i = 0; i < dataset.size(); i++) {

      int columnHeight = metrics.columnHeight();
      int left = metrics.left(i);
      int right = metrics.right(i);

      if (dataset.isSelected(i)) {
        g2.setColor(colors.getSelectedColumnColor());
        g2.fillRect(left, metrics.columnTop(), metrics.columnWidth(), columnHeight);

        g2.setColor(colors.getSelectedLabelBackgroundColor());
        g2.fillRect(left, metrics.labelTop(), metrics.columnWidth(), metrics.labelZoneHeightWithMargin());
      }
      else {
        g2.setColor(colors.getChartBgColor());
        g2.fillRect(left, metrics.columnTop(), metrics.columnWidth(), columnHeight);
        g2.setColor(getBackground());
        g2.fillRect(left, metrics.labelTop(), metrics.columnWidth(), metrics.labelZoneHeightWithMargin());
      }

      if (config.drawColumnDividers) {
        colors.setColumnDividerStyle(g2);
        g2.drawLine(right, metrics.columnTop(), right, metrics.columnTop() + columnHeight);
      }

      if (config.drawLabels) {
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
    if (!config.drawLabels) {
      return;
    }
    g2.setComposite(AlphaComposite.Src);
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

  private void paintScale(Graphics2D g2, int panelWidth) {
    if (!config.drawScale) {
      return;
    }
    double[] scaleValues = metrics.scaleValues();
    for (double scaleValue : scaleValues) {
      if (scaleValue == 0) {
        g2.setStroke(SCALE_ORIGIN_STROKE);
        g2.setColor(colors.getScaleOriginLineColor());
      }
      else {
        g2.setStroke(SCALE_STROKE);
        g2.setColor(colors.getScaleLineColor());
      }
      g2.drawLine(metrics.chartX(), metrics.y(scaleValue), panelWidth, metrics.y(scaleValue));

      g2.setColor(colors.getLabelColor());
      String label = Integer.toString((int)scaleValue);
      g2.drawString(label, metrics.scaleX(label), metrics.scaleY(scaleValue));
    }
  }

  public void mouseMoved(int x, int y, boolean dragging) {
    if (metrics == null) {
      return;
    }

    Integer columnIndex = metrics.getColumnAt(x);
    Key objectKey = painter.getObjectKeyAt(x, y);
    selectionManager.updateRollover(columnIndex, objectKey, dragging);

    repaint();
  }

  private boolean clickable() {
    return config.columnClickEnabled && selectionManager.canSelect() && (painter != null)
           && painter.getDataset().size() > 0;
  }

  private void registerMouseActions() {
    addMouseListener(new MouseAdapter() {

      public void mousePressed(MouseEvent e) {
        if (clickable() && isEnabled() && e.getClickCount() == 1) {
          selectionManager.startClick();
        }
      }

      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          selectionManager.notifyDoubleClick();
        }
      }

      public void mouseEntered(MouseEvent e) {
        if (isEnabled()) {
          selectionManager.resetRollover(painter.getDataset());
        }
      }

      public void mouseExited(MouseEvent e) {
        selectionManager.resetRollover(painter.getDataset());
        repaint();
      }
    });

    addMouseMotionListener(new MouseMotionListener() {
      public void mouseDragged(MouseEvent e) {
        if (isEnabled()) {
          HistoChart.this.mouseMoved(e.getX(), e.getY(), true);
        }
      }

      public void mouseMoved(MouseEvent e) {
        if (isEnabled()) {
          HistoChart.this.mouseMoved(e.getX(), e.getY(), false);
        }
      }
    });

    addMouseWheelListener(new MouseWheelListener() {
      public void mouseWheelMoved(MouseWheelEvent e) {
        selectionManager.notifyScroll(e.getWheelRotation());
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
    if (config.columnClickEnabled && selectionManager.getRollover().isOnColumn(columnIndex)) {
      return colors.getRolloverLabelColor();
    }
    else {
      return colors.getLabelColor();
    }
  }
}
