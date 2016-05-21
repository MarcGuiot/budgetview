package com.budgetview.gui.components.charts.histo;

import com.budgetview.gui.components.charts.histo.utils.AwtTextMetrics;
import com.budgetview.shared.gui.TextMetrics;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.shared.gui.histochart.HistoChartMetrics;
import com.budgetview.shared.gui.histochart.HistoDataset;
import com.budgetview.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Key;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Set;

public class HistoChart extends JPanel implements Disposable {

  private HistoChartColors colors;
  private HistoPainter painter = HistoPainter.NULL;
  private HistoChartMetrics metrics;
  private Insets insets = new Insets(0, 0, 0, 0);
  private Font selectedLabelFont;
  private Font sectionLabelFont;
  private boolean snapToScale;
  private HistoChartConfig config;
  private Font defaultFont;
  private HistoSelectionManager selectionManager;
  private DisposableGroup disposables = new DisposableGroup();

  public static final BasicStroke SCALE_STROKE = new BasicStroke(1);
  public static final BasicStroke SCALE_ORIGIN_STROKE = new BasicStroke(1.2f);

  public HistoChart(HistoChartConfig config, HistoChartColors colors) {
    this.config = config;
    this.selectionManager = new HistoSelectionManager();
    this.colors = colors;
    this.defaultFont = getFont().deriveFont(9f);
    setFont(defaultFont);
    this.selectedLabelFont = getFont().deriveFont(Font.BOLD);
    this.sectionLabelFont = getFont().deriveFont(11f).deriveFont(Font.BOLD);
    registerMouseActions();
    selectionManager.addListener(new HistoChartListenerAdapter() {
      public void rolloverUpdated(HistoRollover rollover) {
        if (rollover.getColumnIndex() != null) {
          setToolTipText(painter.getDataset().getTooltip(rollover.getColumnIndex(), rollover.getObjectKeys()));
        }
        setCursor(Cursor.getPredefinedCursor(rollover.isActive() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
      }
    });

    disposables.add(colors);
    disposables.add(selectionManager);
  }

  public int getMaxLabelSize(int columnCount) {
    int usableColumnWidth = HistoChartMetrics.usableColumnWidth(getWidth(), columnCount);
    int mCharWidth = getFontMetrics(getFont()).stringWidth("m");
    return usableColumnWidth / mCharWidth;
  }

  public void addListener(HistoChartListener listener) {
    selectionManager.addListener(listener);
  }

  public void dispose() {
    disposables.dispose();
    selectionManager = null;
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

  public void setBorder(Border border) {
    super.setBorder(border);
    if (border != null && border instanceof EmptyBorder) {
      this.insets = ((EmptyBorder) border).getBorderInsets();
    }
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
    BufferedImage image = ((Graphics2D) g).getDeviceConfiguration().createCompatibleImage(getWidth(), getHeight(), Transparency.TRANSLUCENT);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int panelWidth = getWidth() - 1;
    int panelHeight = getHeight() - 1;

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, panelWidth, getHeight());
    }

    HistoDataset dataset = painter.getDataset();

    if (metrics == null) {
      metrics = new HistoChartMetrics(panelWidth, panelHeight, insets,
                                      getTextMetrics(g, getFont()),
                                      dataset.size(),
                                      dataset.getMaxNegativeValue(), dataset.getMaxPositiveValue(),
                                      config,
                                      dataset.containsSections(),
                                      snapToScale);
    }

    g2.setFont(defaultFont);
    paintBg(g2);
    paintLabels(g2, dataset);
    paintScale(g2);
    paintSelectionBorder(g2, dataset);
    paintBorder(g2);

    g2.setFont(getFont());
    painter.paint(g2, metrics, config, selectionManager.getRollover());

    paintSections(g2, dataset);
    ((Graphics2D) g).drawImage(image, null, null);
  }

  private TextMetrics getTextMetrics(Graphics g, Font font) {
    return new AwtTextMetrics(g, font);
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

      g2.setColor(dataset.isSelected(i) ? colors.getSelectedColumnColor() : colors.getChartBgColor());
      g2.fillRect(left, metrics.columnTop(), metrics.columnWidth(), columnHeight);

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
        g2.drawRect(left, metrics.columnTop(), metrics.columnWidth(), columnHeight);
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

  private void paintScale(Graphics2D g2) {
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
      g2.drawLine(metrics.chartX(), metrics.y(scaleValue), metrics.chartX() + metrics.chartWidth(), metrics.y(scaleValue));

      g2.setColor(colors.getLabelColor());
      String label = Integer.toString((int) scaleValue);
      g2.drawString(label, metrics.scaleX(label), metrics.scaleY(scaleValue));
    }
  }

  public void mouseMoved(int x, int y, boolean dragging, boolean rightClick) {
    if (metrics == null) {
      return;
    }

    Integer columnIndex = metrics.getColumnAt(x);
    Set<Key> objectKeys = painter.getObjectKeysAt(x, y);
    selectionManager.updateRollover(columnIndex, objectKeys, dragging, rightClick, new Point(x, y));

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
          selectionManager.startClick(GuiUtils.isRightClick(e), new Point(e.getX(), e.getY()));
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
          HistoChart.this.mouseMoved(e.getX(), e.getY(), true, GuiUtils.isRightClick(e));
        }
      }

      public void mouseMoved(MouseEvent e) {
        if (isEnabled()) {
          HistoChart.this.mouseMoved(e.getX(), e.getY(), false, GuiUtils.isRightClick(e));
        }
      }
    });

    if (config.useWheelScroll) {
      addMouseWheelListener(new MouseWheelListener() {
        public void mouseWheelMoved(MouseWheelEvent e) {
          selectionManager.notifyScroll(e.getWheelRotation());
        }
      });
    }
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
