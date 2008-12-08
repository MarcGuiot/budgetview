package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.series.evolution.SeriesEvolutionView;
import org.uispec4j.Panel;
import org.uispec4j.Table;
import org.uispec4j.TableCellValueConverter;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class SeriesEvolutionChecker extends DataChecker {
  private Table table;
  private Window mainWindow;

  public SeriesEvolutionChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  public SeriesTableChecker initContent() {
    return new SeriesTableChecker();
  }

  public SeriesEvolutionChecker checkRowLabels(String... labels) {
    assertThat(getTable().columnEquals(1, labels));
    return this;
  }

  public void checkColumnNames(String... names) {
    assertThat(getTable().getHeader().contentEquals(Utils.join("", names)));
  }

  public void checkRow(String label, String... values) {
    Table table = getTable();
    int index = table.getRowIndex(SeriesEvolutionView.LABEL_COLUMN_INDEX, label);
    assertThat(table.rowEquals(index, Utils.join(new String[]{"", label}, values)));
  }

  public void doubleClickOnRow(String label) {
    Table table = getTable();
    int index = table.getRowIndex(SeriesEvolutionView.LABEL_COLUMN_INDEX, label);
    table.doubleClick(index, 1);
  }

  public void expand() {
    getPanel().getButton("expand").click();
  }

  public void collapse() {
    getPanel().getButton("collapse").click();
  }

  private Table getTable() {
    if (table == null) {
      table = mainWindow.getTable("seriesEvolutionTable");
      table.setCellValueConverter(0, new BlancColumnConverter());
      MonthColumnConverter converter = new MonthColumnConverter();
      for (int i = 2; i < 2 + SeriesEvolutionView.MONTH_COLUMNS_COUNT; i++) {
        table.setCellValueConverter(i, converter);
      }
    }
    return table;
  }

  private Panel getPanel() {
    return mainWindow.getPanel("seriesEvolutionView");
  }

  public SeriesEditionDialogChecker editSeries(String rowLabel, String columnLabel) {
    Table table = getTable();
    int row = table.getRowIndex(SeriesEvolutionView.LABEL_COLUMN_INDEX, rowLabel);
    int column = table.getHeader().findColumnIndex(columnLabel);
    table.selectRow(row);
    Window window = WindowInterceptor.getModalDialog(table.editCell(row, column).getButton().triggerClick());
    return new SeriesEditionDialogChecker(window, false);
  }

  public void checkTableIsEmpty(String... labels) {
    SeriesEvolutionChecker.SeriesTableChecker checker = initContent();
    String[] values = new String[SeriesEvolutionView.MONTH_COLUMNS_COUNT];
    Arrays.fill(values, "");
    for (String label : labels) {
      checker.add(label, values);
    }
    checker.check();
  }

  public class SeriesTableChecker extends TableChecker {

    public SeriesTableChecker add(String label, String... monthValues) {
      super.add(Utils.join(new String[]{"", label}, monthValues));
      return this;
    }

    protected Table getTable() {
      return SeriesEvolutionChecker.this.getTable();
    }
  }

  private class BlancColumnConverter implements TableCellValueConverter {
    public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
      return "";
    }
  }

  private class MonthColumnConverter implements TableCellValueConverter {
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
}