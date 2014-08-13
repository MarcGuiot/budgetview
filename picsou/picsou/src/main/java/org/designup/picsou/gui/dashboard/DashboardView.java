package org.designup.picsou.gui.dashboard;

import org.designup.picsou.gui.View;
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

    UncategorizedSummaryView uncategorized = new UncategorizedSummaryView(repository, directory);
    uncategorized.registerComponents(builder);

    parentBuilder.add("dashboardView", builder);
  }
}
