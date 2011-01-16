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

    chart = new HistoChart(true, true, directory);

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

    HistoLineDataset dataset = new HistoLineDataset("seriesEvolution.chart.histo.series.tooltip");
    dataset.add(201005, 5, "5", "", "", false, false, false);
    dataset.add(201006, 6, "6", "", "", false, false, false);
    dataset.add(201007, 7, "7", "", "", false, false, false);
    dataset.add(201008, 8, "8", "", "", false, false, false);
    dataset.add(201009, 9, "9", "", "", false, false, false);
    init(dataset);

    Mouse.enter(chart, 50, 10);
    Mouse.move(chart, 50, 50);
    Mouse.pressed(chart, 50, 50);
    listener.check("<select ids='[201006]'/>");

    Mouse.drag(chart, 70, 50);
    listener.check("<select ids='[201006, 201007, 201008]'/>");

    Mouse.drag(chart, 50, 50);
    listener.checkEmpty();

    Mouse.drag(chart, 30, 50);
    listener.check("<select ids='[201005, 201006, 201007, 201008]'/>");

    Mouse.released(chart, 100, 100);
    listener.checkEmpty();

    Mouse.move(chart, 50, 50);
    listener.checkEmpty();

    Mouse.exit(chart, 100, 100);
    listener.checkEmpty();
  }

  public void testWithDiscontinuousMonths() throws Exception {
    HistoLineDataset dataset = new HistoLineDataset("seriesEvolution.chart.histo.series.tooltip");
    dataset.add(201005, 5, "5", "", "", false, false, false);
    dataset.add(201007, 7, "7", "", "", false, false, false);
    dataset.add(201009, 9, "9", "", "", false, false, false);
    init(dataset);

    Mouse.enter(chart, 20, 10);
    Mouse.move(chart, 20, 50);
    Mouse.pressed(chart, 20, 50);
    listener.check("<select ids='[201005]'/>");

    Mouse.drag(chart, 90, 50);
    listener.check("<select ids='[201005, 201007, 201009]'/>");

    Mouse.drag(chart, 50, 50);
    listener.checkEmpty();

    Mouse.exit(chart, 100, 100);
    listener.checkEmpty();
  }

  private void init(HistoLineDataset dataset) {
    HistoLinePainter painter = new HistoLinePainter(dataset, colors);
    chart.update(painter);
    chart.setSize(100, 100);
    chart.paint(Empty.NULL_GRAPHICS_2D);
  }
}
