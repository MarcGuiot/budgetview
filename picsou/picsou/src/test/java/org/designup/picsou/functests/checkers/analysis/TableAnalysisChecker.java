package org.designup.picsou.functests.checkers.analysis;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.functests.checkers.ExpandableTableChecker;
import org.designup.picsou.functests.checkers.SeriesAmountEditionDialogChecker;
import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.checkers.components.PopupButton;
import org.designup.picsou.functests.checkers.components.PopupChecker;
import org.designup.picsou.functests.checkers.components.TableChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.analysis.table.SeriesEvolutionTableView;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.interception.PopupMenuInterceptor;
import org.uispec4j.utils.ColorUtils;
import org.uispec4j.utils.KeyUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.uispec4j.assertion.UISpecAssert.*;

public class TableAnalysisChecker extends ExpandableTableChecker<TableAnalysisChecker> {
  private Table table;
  private int COLUMN_COUNT = 10;
  private Panel panel;

  public TableAnalysisChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void checkColumnNames(String... names) {
    assertThat(getTable().getHeader().contentEquals(COLUMN_COUNT, Utils.join("", names)));
  }

  public void checkRow(String label, String... values) {
    Table table = getTable();
    int index = getRow(label, table);
    if (index < 0) {
      Assert.fail("No row found with label: " + label + "\nActual content:\n" + table);
    }
    assertThat(table.rowEquals(index, 0, COLUMN_COUNT, Utils.join(new String[]{"", label}, values)));
  }

  public TableAnalysisChecker unselectAll() {
    getTable().clearSelection();
    return this;
  }

  public TableAnalysisChecker select(String... labels) {
    if (labels.length != 0) {
      getTable().selectRowsWithText(SeriesEvolutionTableView.LABEL_COLUMN_INDEX, labels);
    }
    else {
      getTable().clearSelection();
    }
    return this;
  }

  public void clearSelection() {
    getTable().clearSelection();
  }

  public TableAnalysisChecker checkSelected(String... labels) {
    Table table = getTable();
    int[] rowIndices = getRows(table, labels);
    assertThat(table.rowsAreSelected(rowIndices));
    return this;
  }

  private int[] getRows(Table table, String[] labels) {
    SortedSet<Integer> indices = new TreeSet<Integer>();
    for (String label : labels) {
      for (int index : table.getRowIndices(SeriesEvolutionTableView.LABEL_COLUMN_INDEX, label)) {
        indices.add(index);
      }
    }
    return Utils.toArray(indices);
  }

  public void checkNoSelection() {
    assertThat(getTable().selectionIsEmpty());
  }

  private int getRow(String label, Table table) {
    return table.getRowIndex(SeriesEvolutionTableView.LABEL_COLUMN_INDEX, label);
  }

  public void doubleClickOnRow(String label) {
    Table table = getTable();
    int index = getRow(label, table);
    table.doubleClick(index, 1);
  }

  protected Table getTable() {
    if (table == null) {
      table = getPanel().getTable("analysisTable");
      table.setCellValueConverter(0, new BlankColumnConverter());
      ColumnConverter converter = new ColumnConverter();
      for (int i = 1; i < 3 + table.getColumnCount() - 1; i++) {
        table.setCellValueConverter(i, converter);
      }
    }
    return table;
  }

  protected Panel getPanel() {
    if (panel == null) {
      views.selectAnalysis();
      this.panel = mainWindow.getPanel("tableAnalysis");
    }
    return panel;
  }

  public SeriesEditionDialogChecker editSeries(String rowLabel) {
    Table table = getTable();
    int row = getRow(rowLabel.toUpperCase(), table);
    if (row == -1) {
      row = table.getRowIndex(getLabelColumnIndex(), rowLabel);
    }
    return SeriesEditionDialogChecker.open(table.editCell(row, getLabelColumnIndex()).getButton().triggerClick());
  }

  public SeriesAmountEditionDialogChecker editSeries(String rowLabel, String columnLabel) {
    Table table = getTable();
    int row = getRow(rowLabel.toUpperCase(), table);
    if (row == -1) {
      row = table.getRowIndex(getLabelColumnIndex(), rowLabel);
    }
    int column = table.getHeader().findColumnIndex(columnLabel);
    table.selectRow(row);
    return SeriesAmountEditionDialogChecker.open(table.editCell(row, column).getButton().triggerClick());
  }

  public SeriesTableChecker initContent() {
    return new SeriesTableChecker(-1);
  }

  public SeriesTableChecker initContent(int maxColumns) {
    return new SeriesTableChecker(maxColumns);
  }

  public TableAnalysisChecker checkRowLabels(String... labels) {
    assertThat(getTable().columnEquals(1, labels));
    return this;
  }

  public void checkEmpty(String... labels) {
    SeriesTableChecker checker = initContent();
    String[] values = new String[COLUMN_COUNT];
    Arrays.fill(values, "");
    for (String label : labels) {
      checker.add(label, values);
    }
    checker.check();
  }

  public void checkNoTableRowWithLabel(String seriesName) {
    Table table = getTable();
    assertFalse("'" + seriesName + "' unexpectedly shown: \n" + table, getTable().containsRow(getLabelColumnIndex(), seriesName));
  }

  public TableAnalysisChecker checkForeground(String rowLabel, String columnLabel, String expectedColor) {
    Table table = getTable();
    int row = getRow(rowLabel, table);
    int column = table.getHeader().findColumnIndex(columnLabel);
    final JComponent component = getTextComponent(row, column);
    ColorUtils.assertSimilar("Error at (" + row + "," + column + ") - value=" + table.getContentAt(row, column),
                             expectedColor, component.getForeground());
    return this;
  }

  private JComponent getTextComponent(int row, int column) {
    final Component renderer = table.getSwingRendererComponentAt(row, column);
    org.uispec4j.Panel panel = new org.uispec4j.Panel((JPanel)renderer);
    JButton button = panel.findSwingComponent(JButton.class);
    if (button != null) {
      return button;
    }

    JLabel label = panel.findSwingComponent(JLabel.class);
    if (label != null) {
      return label;
    }

    throw new AssertionFailedError("unexpected component: " + panel.getDescription());
  }

  public void checkSelectionClipboardExport(int[] indices, String expectedClipboardContent) throws Exception {
    LoggedInFunctionalTestCase.callFailIfClipBoardDisable();

    Clipboard.putText("something to clean up the clipboard before running the test");

    Table table = getTable();
    table.selectRows(indices);
    KeyUtils.pressKey(table, org.uispec4j.Key.plaformSpecificCtrl(org.uispec4j.Key.C));
    checkClipboardContent(expectedClipboardContent);

    Clipboard.putText("something to clean up the clipboard before running the test");

    PopupMenuInterceptor
      .run(getTable().triggerRightClick(indices[0], 0))
      .getSubMenu("Copy")
      .click();
    checkClipboardContent(expectedClipboardContent);
  }

  private void checkClipboardContent(String expectedClipboardContent) throws Exception {
    assertRowsEqual(expectedClipboardContent, Clipboard.getContentAsText());
  }

  private void assertRowsEqual(String expected, String actual) {
    String[] expectedRows = expected.split("\n");
    String[] actualRows = expected.split("\n");
    if (!rowsEqual(expectedRows, actualRows)) {
      Assert.assertEquals(expected, actual);
    }
  }

  private boolean rowsEqual(String[] expectedRows, String[] actualRows) {
    if (expectedRows.length != actualRows.length) {
      return false;
    }
    for (int i = 0; i < expectedRows.length; i++) {
      if (!actualRows[i].startsWith(expectedRows[i])) {
        return false;
      }
    }
    return true;
  }

  public void checkTableClipboardExport(String expectedClipboardContent) throws Exception {
    Clipboard.putText("something to clean up the clipboard before running the test");
    getTablePopup().click(Lang.get("copyTable"));
    checkClipboardContent(expectedClipboardContent);
  }

  private PopupButton getTablePopup() {
    return new PopupButton(getPanel().getButton("tableActionsMenu"));
  }

  public class SeriesTableChecker extends TableChecker {

    private int maxColumns;

    public SeriesTableChecker(int maxColumns) {
      this.maxColumns = maxColumns;
    }

    public SeriesTableChecker add(String label, String... monthValues) {
      super.add(Utils.join(new String[]{"", label}, monthValues));
      return this;
    }

    protected Table getTable() {
      return TableAnalysisChecker.this.getTable();
    }

    public void dumpCode() {
      StringBuilder builder = new StringBuilder();
      Table table = getTable();
      for (int row = 0; row < table.getRowCount(); row++) {
        builder.append("  .add(");
        int columnCount = getColumnCount();
        for (int col = 1; col < columnCount; col++) {
          if (col > 1) {
            builder.append(", ");
          }
          builder.append('"').append(table.getContentAt(row, col)).append('"');
        }
        builder.append(")\n");
      }
      builder.append("  .check();\n");
      Assert.fail("Write this:\n" + builder.toString());
    }

    public void check() {
      Object[][] expectedContent = rows.toArray(new Object[rows.size()][]);
      org.uispec4j.assertion.UISpecAssert.assertTrue(getTable().blockEquals(0, 0, getColumnCount(), rows.size(), expectedContent));
    }

    private int getColumnCount() {
      return maxColumns >= 0 ? maxColumns : COLUMN_COUNT;
    }
  }

  private class BlankColumnConverter implements TableCellValueConverter {
    public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
      return "";
    }
  }

  private class ColumnConverter implements TableCellValueConverter {
    public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
      org.uispec4j.Panel panel = new org.uispec4j.Panel((JPanel)renderedComponent);
      JButton button = panel.findSwingComponent(JButton.class);
      if (button != null) {
        return Strings.toString(button.getText());
      }

      JLabel label = panel.findSwingComponent(JLabel.class);
      if (label != null) {
        return Strings.toString(label.getText());
      }

      return "Nothing found";
    }
  }

  protected int getLabelColumnIndex() {
    return SeriesEvolutionTableView.LABEL_COLUMN_INDEX;
  }

  private PopupChecker openRightClickPopup(final String item, final int column) {
    return new PopupChecker() {
      protected org.uispec4j.MenuItem openMenu() {
        Table table = getTable();
        return PopupMenuInterceptor.run(table.triggerRightClick(getRow(item, table), column));
      }
    };
  }

  private PopupChecker openRightClickPopup(final String[] items, final int columnIndex) {
    return new PopupChecker() {
      protected org.uispec4j.MenuItem openMenu() {
        Table table = getTable();
        int[] rows = getRows(table, items);
        table.selectRows(rows);
        return PopupMenuInterceptor.run(table.triggerRightClick(rows[0], columnIndex));
      }
    };
  }

  public TableAnalysisChecker expandAll() {
    getTablePopup().click(Lang.get("expand"));
    return this;
  }

  public TableAnalysisChecker collapseAll() {
    getTablePopup().click(Lang.get("collapse"));
    return this;
  }

  public void checkRightClickOptions(String item, String... options) {
    openRightClickPopup(item, 1).checkChoices(options);
  }

  public void checkRightClickOptions(String[] items, String... options) {
    openRightClickPopup(items, 1).checkChoices(options);
  }

  public void rightClickAndSelect(String item, String option) {
    openRightClickPopup(item, 1).click(option);
  }

  public void rightClickAndSelect(String[] items, String option) {
    openRightClickPopup(items, 1).click(option);
  }

  public SeriesEditionDialogChecker rightClickAndEditSeries(final String item, final String option) {
    PopupChecker popupChecker = openRightClickPopup(item, 1);
    return SeriesEditionDialogChecker.open(popupChecker.triggerClick(option));
  }
}
