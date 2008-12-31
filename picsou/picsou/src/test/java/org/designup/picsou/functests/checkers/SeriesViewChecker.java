package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.expansion.TableExpansionColumn;
import org.designup.picsou.gui.series.view.SeriesView;
import org.uispec4j.Button;
import org.uispec4j.Table;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

import javax.swing.*;

public class SeriesViewChecker extends GuiChecker {
  private Window mainWindow;

  public SeriesViewChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public void checkContains(String... items) {
    assertThat(getTable().columnEquals(SeriesView.LABEL_COLUMN_INDEX, items));
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

  public void checkExpanded(String label, boolean expanded) {
    Table table = getTable();
    int row = table.getRowIndex(SeriesView.LABEL_COLUMN_INDEX, label);
    JButton button = (JButton)table.getSwingRendererComponentAt(row, 0);
    if (expanded) {
      Assert.assertSame(TableExpansionColumn.EXPANDED_ICON, button.getIcon());
    }
    else {
      Assert.assertSame(TableExpansionColumn.COLLAPSED_ICON, button.getIcon());
    }
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
}
