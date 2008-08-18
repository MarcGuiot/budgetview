package org.designup.picsou.gui.graphics;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorUpdater;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.xy.ClusteredXYBarRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.Set;

public abstract class AbstractLineChart extends View implements GlobSelectionListener, ChangeSetListener {
  protected ChartPanel panel;
  protected XYPlot plot;
  protected JFreeChart chart;
  protected XYSeriesCollection dataset = new XYSeriesCollection();

  public AbstractLineChart(GlobRepository repository, Directory directory) {
    super(repository, directory);
    repository.addChangeListener(this);
  }

  public void globsReset(GlobRepository globRepository, Set<GlobType> changedTypes) {
    updateChart();
  }

  protected abstract void updateChart();

  protected abstract void configureChart();

  public ChartPanel createPanel() {
    ChartPanel panel = new ChartPanel(createChart());
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
    chart = ChartFactory.createXYBarChart(
      null,  // chart title
      null,  // domain axis label
      false,
      null,  // range axis label
      dataset,
      PlotOrientation.VERTICAL,
      false, // include legend
      true,  // tooltips
      false  // urls
    );

    plot = (XYPlot)chart.getPlot();
    plot.setBackgroundPaint(Color.white);
    plot.setRangeGridlinePaint(Color.lightGray);

    plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

    plot.setDomainCrosshairVisible(false);
    plot.setRangeCrosshairVisible(false);

    chart.setBackgroundPaint(null);

    configureChart();


    ClusteredXYBarRenderer renderer = new ClusteredXYBarRenderer();
    renderer.setMargin(0.3);
    plot.setRenderer(renderer);

    return chart;
  }

  protected void setRowColor(String row, PicsouColors topColor, PicsouColors bottomColor, final AbstractRenderer renderer, XYDataset dataset) {
    final int rowIndex = dataset.indexOf(row);
    if (rowIndex >= 0) {
      FillColorUpdater colorUpdater = new FillColorUpdater(renderer, rowIndex, topColor, bottomColor, colorService);
      colorService.install(topColor.toString(), colorUpdater);
      colorService.install(bottomColor.toString(), colorUpdater);
    }
  }

  protected void setShapeColor(String row, PicsouColors color, final AbstractRenderer renderer, XYDataset dataset) {
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

  private static class FillColorUpdater implements ColorUpdater {
    private final AbstractRenderer renderer;
    private final int rowIndex;
    private PicsouColors topColor;
    private PicsouColors bottomColor;
    private ColorLocator colorLocator;

    public FillColorUpdater(AbstractRenderer renderer, int rowIndex,
                            PicsouColors topColor, PicsouColors bottomColor,
                            ColorLocator colorLocator) {
      this.renderer = renderer;
      this.rowIndex = rowIndex;
      this.topColor = topColor;
      this.bottomColor = bottomColor;
      this.colorLocator = colorLocator;
    }

    public void updateColor(Color color) {
      renderer.setSeriesPaint(rowIndex,
                              new GradientPaint(0.0f, 0.0f, colorLocator.get(topColor),
                                                0.0f, 5000.0f, colorLocator.get(bottomColor)));
    }
  }
}
