package org.designup.picsou.gui.dashboard.widgets;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.dashboard.DashboardWidget;
import org.designup.picsou.gui.model.DashboardStat;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class UncategorizedWidget extends DashboardWidget {

  public UncategorizedWidget(GlobRepository repository, Directory directory) {
    super(repository, directory);
    widgetButton.addActionListener(new GotoUncategorizedAction());
  }

  public void doUpdate(Glob dashboardStat, GlobRepository repository) {
    int count = dashboardStat.get(DashboardStat.UNCATEGORIZED_COUNT, 0);
    if (count == 0) {
      widgetButton.setText(Lang.get("uncategorizedWidget.ok"));
      setWidgetStyle("textOK");
      legend.setText(Lang.get("uncategorizedWidget.legend.ok"));
    }
    else {
      widgetButton.setText(Integer.toString(count));
      setWidgetStyle("textNOK");
      legend.setText(Lang.get("uncategorizedWidget.legend.nok"));
    }
  }

  private class GotoUncategorizedAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      directory.get(NavigationService.class).gotoUncategorized();
    }
  }
}

