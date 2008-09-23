package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.expansion.TableExpansionColumn;
import org.designup.picsou.gui.series.view.SeriesView;
import org.uispec4j.Button;
import org.uispec4j.Table;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import javax.swing.*;

public class SeriesViewChecker extends DataChecker {
  private Window mainWindow;

  public SeriesViewChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public void checkContains(String... items) {
    UISpecAssert.assertThat(getTable().columnEquals(SeriesView.LABEL_COLUMN_INDEX, items));
  }

  public void select(String label) {
    Table table = getTable();
    int row = table.getRowIndex(SeriesView.LABEL_COLUMN_INDEX, label);
    table.selectRow(row);
  }

  public void checkExpansionEnabled(String label, boolean enabled) {
    Table table = getTable();
    int row = table.getRowIndex(SeriesView.LABEL_COLUMN_INDEX, label);
    JButton button = (JButton)getTable().getSwingRendererComponentAt(row, 0);
    Assert.assertEquals(enabled,
                        (button.getIcon() != null) &&
                        (button.getIcon() != TableExpansionColumn.DISABLED_ICON));
  }

  public void toggle(String label) {
    Table table = getTable();
    int row = table.getRowIndex(SeriesView.LABEL_COLUMN_INDEX, label);
    table.selectRow(row);
    JButton button = (JButton)getTable().getSwingEditorComponentAt(row, 0);
    new Button(button).click();
  }

  private Table getTable() {
    return mainWindow.getTable("seriesView");
  }
}
