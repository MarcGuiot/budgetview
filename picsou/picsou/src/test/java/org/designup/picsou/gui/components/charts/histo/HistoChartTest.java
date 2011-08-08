package org.designup.picsou.gui.components.charts.histo;

import junit.framework.TestCase;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineDataset;
import org.designup.picsou.gui.components.charts.histo.line.HistoLinePainter;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.uispec4j.Mouse;
import org.uispec4j.interception.toolkit.Empty;

public class HistoChartTest extends TestCase {
  private HistoChart chart;
  private DummyHistoChartListener listener;
  private HistoLineColors colors;

  protected void setUp() throws Exception {

    Directory directory = new DefaultDirectory();
    directory.add(new ColorService());


    chart = new HistoChart(new HistoChartConfig(true, true, false, true, false), directory);

    colors = new HistoLineColors(
      "histo.expenses.line",
      "histo.expenses.line",
      "histo.expenses.fill",
      "histo.expenses.fill",
      "histo.vertical.divider",
      directory
    );

    listener = new DummyHistoChartListener();
    chart.addListener(listener);
  }

  public void testSelectByDragging() throws Exception {

    HistoLineDataset dataset = new HistoLineDataset("seriesAnalysis.chart.histo.series.tooltip");
    dataset.add(201005, 5, "5", "", "", false, false, false);
    dataset.add(201006, 6, "6", "", "", false, false, false);
    dataset.add(201007, 7, "7", "", "", false, false, false);
    dataset.add(201008, 8, "8", "", "", false, false, false);
    dataset.add(201009, 9, "9", "", "", false, false, false);
    init(dataset);

    Mouse.enter(chart, 60, 10);
    Mouse.move(chart, 60, 50);
    Mouse.pressed(chart, 60, 50);
    listener.check("<select ids='[201006]'/>");

    Mouse.drag(chart, 80, 50);
    listener.check("<select ids='[201006, 201007, 201008]'/>");

    Mouse.drag(chart, 60, 50);
    listener.checkEmpty();

    Mouse.drag(chart, 40, 50);
    listener.check("<select ids='[201005, 201006, 201007, 201008]'/>");

    Mouse.released(chart, 110, 100);
    listener.checkEmpty();

    Mouse.move(chart, 60, 50);
    listener.checkEmpty();

    Mouse.exit(chart, 110, 100);
    listener.checkEmpty();
  }

  public void testWithDiscontinuousMonths() throws Exception {
    HistoLineDataset dataset = new HistoLineDataset("seriesAnalysis.chart.histo.series.tooltip");
    dataset.add(201005, 5, "5", "", "", false, false, false);
    dataset.add(201007, 7, "7", "", "", false, false, false);
    dataset.add(201009, 9, "9", "", "", false, false, false);
    init(dataset);

    Mouse.enter(chart, 30, 10);
    Mouse.move(chart, 30, 50);
    Mouse.pressed(chart, 30, 50);
    listener.check("<select ids='[201005]'/>");

    Mouse.drag(chart, 100, 50);
    listener.check("<select ids='[201005, 201007, 201009]'/>");

    Mouse.drag(chart, 60, 50);
    listener.checkEmpty();

    Mouse.exit(chart, 110, 100);
    listener.checkEmpty();
  }

  private void init(HistoLineDataset dataset) {
    HistoLinePainter painter = new HistoLinePainter(dataset, colors);
    chart.update(painter);
    chart.setSize(100, 115);
    chart.paint(Empty.NULL_GRAPHICS_2D);
  }
}
