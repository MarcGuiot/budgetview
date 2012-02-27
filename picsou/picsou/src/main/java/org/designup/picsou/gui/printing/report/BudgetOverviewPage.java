package org.designup.picsou.gui.printing.report;

import org.designup.picsou.gui.printing.PrintColors;
import org.designup.picsou.gui.printing.PrintFonts;
import org.designup.picsou.gui.printing.PrintMetrics;
import org.designup.picsou.gui.series.analysis.SeriesChartsPanel;
import org.designup.picsou.gui.series.analysis.histobuilders.range.FixedHistoChartRange;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.model.util.MonthRange;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.SortedSet;
import java.util.TreeSet;

public class BudgetOverviewPage extends ReportPage {

  private Integer currentMonth;
  private MonthRange monthRange;
  private GlobRepository repository;
  private Directory directory;
  private JPanel panel;

  public BudgetOverviewPage(Integer currentMonth, MonthRange monthRange, GlobRepository repository, Directory directory) {
    this.currentMonth = currentMonth;
    this.monthRange = monthRange;
    this.repository = repository;
    this.directory = directory;
    this.panel = createPanel();
  }

  private JPanel createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/print/budgetOverviewPage.splits",
                                                      repository, directory);

    SeriesChartsPanel chartsPanel = new SeriesChartsPanel(new FixedHistoChartRange(monthRange, repository),
                                                          repository, directory,
                                                          directory.get(SelectionService.class));
    chartsPanel.registerCharts(builder);
    chartsPanel.reset();

    SortedSet<Integer> monthIds = new TreeSet<Integer>(monthRange.asList());
    chartsPanel.monthSelected(currentMonth, monthIds);

    return builder.load();
  }

  protected String getTitle() {
    return Lang.get("print.overview");
  }

  protected int printContent(Graphics2D g2, PrintFonts fonts, PrintMetrics metrics, PrintColors colors) {
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
