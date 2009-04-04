package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.converters.ButtonPanelCellConverter;
import org.designup.picsou.gui.projects.NextProjectsView;
import org.uispec4j.Panel;
import org.uispec4j.Table;
import org.uispec4j.Window;

public class NextProjectsChecker extends GuiChecker {
  private Window mainWindow;
  private Table table;

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

  public SeriesEditionDialogChecker editRow(int row) {
    Table table = getTable();
    table.click(row, 0);
    org.uispec4j.Button projectNameButton = table.editCell(row, NextProjectsView.NAME_COLUMN_INDEX).getButton();
    return SeriesEditionDialogChecker.open(projectNameButton, false);
  }

  private Table getTable() {
    if (table == null) {
      table = mainWindow.getTable("nextProjects");
      table.setCellValueConverter(NextProjectsView.NAME_COLUMN_INDEX, new ButtonPanelCellConverter());
    }
    return table;
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
