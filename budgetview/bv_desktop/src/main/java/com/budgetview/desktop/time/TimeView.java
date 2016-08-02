package com.budgetview.desktop.time;

import com.budgetview.desktop.View;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class TimeView extends View {
  private TimeViewPanel viewPanel;

  public TimeView(GlobRepository globRepository, Directory directory) {
    super(globRepository, directory);
    viewPanel = new TimeViewPanel(globRepository, directory);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("timeView", viewPanel);
  }

  public void selectCurrentMonth() {
    viewPanel.selectMonth(directory.get(TimeService.class).getCurrentMonthId());
  }

  public void centerToSelected() {
    viewPanel.centerToSelected();
  }
}
