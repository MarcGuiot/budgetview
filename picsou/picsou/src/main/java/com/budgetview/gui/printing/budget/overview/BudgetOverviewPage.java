package com.budgetview.gui.printing.budget.overview;

import com.budgetview.gui.printing.PrintStyle;
import com.budgetview.gui.description.stringifiers.MonthListStringifier;
import com.budgetview.gui.description.stringifiers.MonthRangeFormatter;
import com.budgetview.gui.printing.PrintMetrics;
import com.budgetview.gui.printing.PrintablePage;
import com.budgetview.gui.analysis.SeriesChartsPanel;
import com.budgetview.gui.analysis.histobuilders.range.FixedHistoChartRange;
import com.budgetview.model.util.ClosedMonthRange;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.SortedSet;

public class BudgetOverviewPage extends PrintablePage {

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
