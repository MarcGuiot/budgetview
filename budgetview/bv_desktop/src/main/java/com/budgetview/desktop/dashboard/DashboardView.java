package com.budgetview.desktop.dashboard;

import com.budgetview.desktop.View;
import com.budgetview.desktop.accounts.utils.AccountFilter;
import com.budgetview.desktop.components.filtering.FilterManager;
import com.budgetview.desktop.components.filtering.Filterable;
import com.budgetview.desktop.components.filtering.components.FilterMessagePanel;
import com.budgetview.desktop.dashboard.widgets.*;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class DashboardView extends View {
  private FilterManager filterManager;

  public DashboardView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/dashboard/dashboardView.splits",
                                                      repository, directory);

    filterManager = new FilterManager(Filterable.NO_OP);
    AccountFilter.initForPeriodStat(filterManager, repository, directory);

    FilterMessagePanel accountFilterMessage = new FilterMessagePanel(filterManager, repository, directory);
    builder.add("accountFilterMessage", accountFilterMessage.getPanel());

    ImportWidget importWidget = new ImportWidget(repository, directory);
    importWidget.register(builder, "importWidget", "importLegend");

    UncategorizedWidget uncategorized = new UncategorizedWidget(repository, directory);
    uncategorized.register(builder, "uncategorizedWidget", "uncategorizedLegend");

    WeatherWidget weather = new WeatherWidget(repository, directory);
    weather.register(builder, "weatherWidget", "weatherLegend");

    RemainderWidget remainder = new RemainderWidget(repository, directory);
    remainder.register(builder, "remainderWidget", "remainderLegend");

    AllAccountsWidget allAccounts = new AllAccountsWidget(repository, directory);
    allAccounts.register(builder, "allAccountsWidget", "allAccountsLegend");

    MainAccountsWidget mainAccounts = new MainAccountsWidget(repository, directory);
    mainAccounts.register(builder, "mainAccountsWidget", "mainAccountsLegend");

    parentBuilder.add("dashboardView", builder);
  }

  public void reset() {
    filterManager.reset();
  }

}
