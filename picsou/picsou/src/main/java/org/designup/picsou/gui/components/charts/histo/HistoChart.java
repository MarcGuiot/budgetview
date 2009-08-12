package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class HistoChart<T extends HistoDataset> extends JPanel {

  private HistoChartColors colors;
  private HistoPainter painter = HistoPainter.NULL;
  private HistoChartListener listener;

  private HistoChartMetrics metrics;
  private Integer currentRollover;

  public HistoChart(Directory directory) {
    this.colors = new HistoChartColors(directory);
    setFont(getFont().deriveFont(9f));
    registerMouseActions();
  }

  public void setListener(HistoChartListener listener) {
    this.listener = listener;
  }

  public void update(HistoPainter painter) {
    this.painter = painter;
    this.currentRollover = null;
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
                                      dataset.getMaxNegativeValue(), dataset.getMaxPositiveValue());
    }

    g2.setColor(colors.getChartBgColor());
    g2.fillRect(metrics.chartX(), 0, metrics.chartWidth(), metrics.chartHeight());

    g2.setColor(colors.getChartBorderColor());
    g2.drawRect(metrics.chartX(), 0, metrics.chartWidth(), metrics.chartHeight());

    g2.setStroke(new BasicStroke(1));
    for (int i = 0; i < dataset.size(); i++) {
      String label = dataset.getLabel(i);
      g2.setColor(getLabelColor(dataset, i));
      g2.drawString(label, metrics.labelX(label, i), metrics.labelY());
    }

    double[] scaleValues = metrics.scaleValues();
    for (double scaleValue : scaleValues) {

      g2.setColor(colors.getScaleLineColor());
      g2.drawLine(metrics.chartX(), metrics.y(scaleValue), panelWidth, metrics.y(scaleValue));

      g2.setColor(colors.getLabelColor());
      String label = Integer.toString((int)scaleValue);
      g2.drawString(label, metrics.scaleX(label), metrics.scaleY(scaleValue));
    }

    painter.paint(g2, metrics, currentRollover);
  }

  private Color getLabelColor(HistoDataset dataset, int columnIndex) {
    boolean isRollover = (currentRollover != null) && (currentRollover == columnIndex);
    if (isRollover) {
      return colors.getRolloverLabelColor();
    }
    else if (dataset.isSelected(columnIndex)) {
      return colors.getSelectedLabelColor();
    }
    else {
      return colors.getLabelColor();
    }
  }

  public void click() {
    if ((currentRollover != null) && (listener != null) && (painter != null)) {
      listener.columnClicked(painter.getDataset().getId(currentRollover));
    }
  }

  public void mouseMoved(int x, int y) {
    if (metrics == null) {
      return;
    }

    Integer index = metrics.getColumnAt(x);
    if (Utils.equal(index, currentRollover)) {
      return;
    }

    currentRollover = index;
    setCursor(currentRollover != null ?
              Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    repaint();
  }

  private void registerMouseActions() {
    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        HistoChart.this.click();
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
        HistoChart.this.mouseMoved(e.getX(), e.getY());
      }

      public void mouseMoved(MouseEvent e) {
        HistoChart.this.mouseMoved(e.getX(), e.getY());
      }
    });
  }
}
