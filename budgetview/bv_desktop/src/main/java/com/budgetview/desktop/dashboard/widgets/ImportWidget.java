package com.budgetview.desktop.dashboard.widgets;

import com.budgetview.desktop.actions.ImportFileAction;
import com.budgetview.desktop.dashboard.DashboardWidget;
import com.budgetview.desktop.model.DashboardStat;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class ImportWidget extends DashboardWidget {
  public ImportWidget(GlobRepository repository, Directory directory) {
    super(repository, directory);
    widgetButton.addActionListener(ImportFileAction.initForMenu("", repository, directory));
  }

  protected void doUpdate(Glob dashboardStat, GlobRepository repository) {
    int days = dashboardStat.get(DashboardStat.DAYS_SINCE_LAST_IMPORT, 0);
    widgetButton.setText(Integer.toString(days));
    setWidgetStyle(days == 0 ? "textOK" : "textNOK");
    legend.setText(Lang.get("importWidget.legend"));
  }
}
