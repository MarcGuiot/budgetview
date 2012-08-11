package org.designup.picsou.gui.printing.report.overview;

import org.designup.picsou.gui.description.stringifiers.MonthListStringifier;
import org.designup.picsou.gui.description.stringifiers.MonthRangeFormatter;
import org.designup.picsou.gui.printing.PrintMetrics;
import org.designup.picsou.gui.printing.PrintStyle;
import org.designup.picsou.gui.printing.report.ReportPage;
import org.designup.picsou.gui.series.analysis.SeriesChartsPanel;
import org.designup.picsou.gui.series.analysis.histobuilders.range.FixedHistoChartRange;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.SortedSet;

public class BudgetOverviewPage extends ReportPage {

  private SortedSet<Integer> selectedMonths;
  private ClosedMonthRange monthRange;
  private GlobRepository repository;
  private Directory directory;
  private JPanel panel;

  public BudgetOverviewPage(SortedSet<Integer> selectedMonths, ClosedMonthRange monthRange, GlobRepository repository, Directory directory) {
    this.selectedMonths = selectedMonths;
    this.monthRange = monthRange;
    this.repository = repository;
    this.directory = directory;
    this.panel = createPanel();
  }

  public String getTitle() {
    String range = MonthListStringifier.toString(selectedMonths, MonthRangeFormatter.STANDARD);
    return Lang.get("print.overview", range.toLowerCase());
  }

  private JPanel createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/print/budgetOverviewPage.splits",
                                                      repository, directory);

    SeriesChartsPanel chartsPanel = new SeriesChartsPanel(new FixedHistoChartRange(monthRange, repository),
                                                          repository, directory,
                                                          directory.get(SelectionService.class));
    chartsPanel.registerCharts(builder);
    chartsPanel.reset();

    chartsPanel.monthSelected(selectedMonths.last(), selectedMonths);

    return builder.load();
  }

  protected int printContent(Graphics2D g2, PrintMetrics metrics, PrintStyle style) {
    Rectangle contentArea = metrics.getContentArea();
    panel.setBounds(0, 0, contentArea.width, contentArea.height);
    panel.setSize(contentArea.width, contentArea.height);
    panel.doLayout();

    g2.translate(contentArea.x, contentArea.y);
    panel.printAll(g2);

    return PAGE_EXISTS;
  }

  public JPanel getPanel() {
    return panel;
  }
}
