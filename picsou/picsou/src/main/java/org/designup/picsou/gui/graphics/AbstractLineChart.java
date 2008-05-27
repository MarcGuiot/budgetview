package org.designup.picsou.gui.graphics;

import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.ChangeSetListener;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorUpdater;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;

public abstract class AbstractLineChart extends View implements GlobSelectionListener, ChangeSetListener {
  protected ChartPanel panel;
  protected XYPlot plot;
  protected JFreeChart chart;
  protected XYSeriesCollection dataset = new XYSeriesCollection();

  public AbstractLineChart(GlobRepository repository, Directory directory) {
    super(repository, directory);
    repository.addChangeListener(this);
  }

  public void globsReset(GlobRepository globRepository, java.util.List<GlobType> changedTypes) {
    updateChart();
  }

  protected abstract void updateChart();

  protected abstract void configureChart();

  public ChartPanel createPanel() {
    ChartPanel panel = new ChartPanel(createChart());
    panel.setFont(Gui.getDefaultFont());
    panel.setDomainZoomable(false);
    panel.setDisplayToolTips(true);
    panel.setInitialDelay(100);
    panel.setFillZoomRectangle(false);
    panel.setMouseZoomable(false);
    panel.setPopupMenu(null);
    configurePanel(panel);
    return panel;
  }

  protected abstract void configurePanel(ChartPanel panel);

  protected JFreeChart createChart() {
    chart = ChartFactory.createXYLineChart(
      null,  // chart title
      null,  // domain axis label
      null,  // range axis label
      dataset,
      PlotOrientation.VERTICAL,
      false, // include legend
      true,  // tooltips
      false  // urls
    );

    plot = (XYPlot) chart.getPlot();
    plot.setBackgroundPaint(Color.white);
    plot.setRangeGridlinePaint(Color.lightGray);

    plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

    plot.setDomainCrosshairVisible(false);
    plot.setRangeCrosshairVisible(false);

    chart.setBackgroundPaint(null);

    configureChart();

    return chart;
  }

  protected void setRowColor(String row, PicsouColors color, final XYLineAndShapeRenderer renderer, XYDataset dataset) {
    final int rowIndex = dataset.indexOf(row);
    if (rowIndex >= 0) {
      colorService.install(color.toString(), new ColorUpdater() {
        public void updateColor(Color color) {
          renderer.setSeriesPaint(rowIndex, color);
        }
      });
    }
  }

  protected void setShapeColor(String row, PicsouColors color, final XYLineAndShapeRenderer renderer, XYDataset dataset) {
    final int rowIndex = dataset.indexOf(row);
    if (rowIndex >= 0) {
      colorService.install(color.toString(), new ColorUpdater() {
        public void updateColor(Color color) {
          renderer.setSeriesFillPaint(rowIndex, color);
        }
      });
    }
  }

  protected ChartPanel getPanel() {
    if (panel == null) {
      panel = createPanel();
    }
    return panel;
  }
}
