package org.designup.picsou.gui.dashboard;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.dashboard.widgets.*;
import org.designup.picsou.gui.license.registered.PremiumRegisteredPanel;
import org.designup.picsou.gui.license.promotion.PremiumPromotionPanel;
import org.designup.picsou.gui.license.promotion.PremiumTrialPanel;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class DashboardView extends View {
  public DashboardView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/dashboard/dashboardView.splits",
                                                      repository, directory);

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

    PremiumPromotionPanel premiumPromotion = new PremiumPromotionPanel(repository, directory);
    premiumPromotion.registerComponents(builder);

    PremiumTrialPanel premiumRegistered= new PremiumTrialPanel(repository, directory);
    premiumRegistered.registerComponents(builder);

    PremiumRegisteredPanel premiumTrial = new PremiumRegisteredPanel(repository, directory);
    premiumTrial.registerComponents(builder);

    parentBuilder.add("dashboardView", builder);
  }
}
