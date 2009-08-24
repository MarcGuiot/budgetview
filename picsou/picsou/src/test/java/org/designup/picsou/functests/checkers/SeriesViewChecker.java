package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.series.view.SeriesView;
import org.uispec4j.Table;
import org.uispec4j.Window;
import org.uispec4j.Panel;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.assertion.UISpecAssert;

public class SeriesViewChecker extends ExpandableTableChecker {

  public SeriesViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void checkContains(String... items) {
    assertThat(getTable().columnEquals(SeriesView.LABEL_COLUMN_INDEX, items));
  }

  public void select(String label) {
    Table table = getTable();
    int row = table.getRowIndex(SeriesView.LABEL_COLUMN_INDEX, label);
    table.selectRow(row);
  }

  protected Table getTable() {
    return window.getTable("seriesView");
  }

  protected Panel getPanel() {
    return window.getPanel("seriesViewPanel");
  }

  public void checkSelection(String label) {
    Table table = getTable();
    int row = table.getRowIndex(SeriesView.LABEL_COLUMN_INDEX, label);
    if (row == -1) {
      Assert.fail(label + " not found");
    }
    if (!table.rowIsSelected(row).isTrue()) {
      int columnCount = table.getColumnCount();
      String selectedRaw = "[";
      for (int i = 0; i < columnCount; i++) {
        if (table.rowIsSelected(i).isTrue()) {
          selectedRaw += table.getContentAt(i, SeriesView.LABEL_COLUMN_INDEX).toString();
          selectedRaw += ", ";
        }
      }
      selectedRaw += "]";
      Assert.fail(label + " not selected but " + selectedRaw + " are selected");
    }
  }

  public void checkVisible(boolean visible) {
    UISpecAssert.assertEquals(visible, getTable().isVisible());
  }

  protected int getLabelColumnIndex() {
    return SeriesView.LABEL_COLUMN_INDEX;
  }
}
