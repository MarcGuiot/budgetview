package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Table;
import org.uispec4j.Window;

public class NextProjectsChecker extends GuiChecker {
  private Window mainWindow;

  public NextProjectsChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public void checkEmpty() {
  }

  public SeriesEditionDialogChecker createProject() {
    return SeriesEditionDialogChecker.open(getPanel().getButton("createProject"), false);
  }

  public SeriesEditionDialogChecker editProjects() {
    return SeriesEditionDialogChecker.open(getPanel().getButton("editAllProjects"), false);
  }

  private Table getTable() {
    return mainWindow.getTable("nextProjects");
  }

  private Panel getPanel() {
    return mainWindow.getPanel("nextProjectsPanel");
  }

  public NextProjectsTableChecker initContent() {
    return new NextProjectsTableChecker();
  }

  public class NextProjectsTableChecker extends TableChecker {

    public NextProjectsTableChecker add(String month, String project, double amount) {
      super.add(month, project, NextProjectsChecker.this.toString(amount), "", "", "");
      return this;
    }

    public NextProjectsTableChecker add(String month, String project, Double amount, Double mainPosition, Double savingsPosition, double totalPosition) {
      super.add(month, project,
                NextProjectsChecker.this.toString(amount),
                NextProjectsChecker.this.toString(mainPosition),
                NextProjectsChecker.this.toString(savingsPosition),
                NextProjectsChecker.this.toString(totalPosition));
      return this;
    }

    protected Table getTable() {
      return NextProjectsChecker.this.getTable();
    }
  }

}
