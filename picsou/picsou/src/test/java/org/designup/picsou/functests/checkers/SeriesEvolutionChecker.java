package org.designup.picsou.functests.checkers;

import junit.framework.AssertionFailedError;
import junit.framework.Assert;
import org.designup.picsou.gui.series.evolution.SeriesEvolutionView;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.uispec4j.*;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.utils.ColorUtils;
import org.uispec4j.utils.KeyUtils;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class SeriesEvolutionChecker extends GuiChecker {

  public HistoChecker histoChart;

  private Table table;
  private Window mainWindow;

  public SeriesEvolutionChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
    this.histoChart = new HistoChecker(mainWindow);
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

  public void select(String label) {
    Table table = getTable();
    int index = table.getRowIndex(SeriesEvolutionView.LABEL_COLUMN_INDEX, label);
    if (index < 0) {
      Assert.fail("No line found with label '" + label + "' - available names: " + getLineLabels());
    }
    table.selectRow(index);
  }

  private java.util.List<String> getLineLabels() {
    java.util.List<String> labels = new ArrayList<String>();
    for (int row = 0; row < table.getRowCount(); row++) {
      labels.add(table.getContentAt(row, SeriesEvolutionView.LABEL_COLUMN_INDEX).toString());
    }
    return labels;
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
      table.setCellValueConverter(0, new BlankColumnConverter());
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
    int row = table.getRowIndex(SeriesEvolutionView.LABEL_COLUMN_INDEX, rowLabel.toUpperCase());
    if (row == -1) {
      row = table.getRowIndex(SeriesEvolutionView.LABEL_COLUMN_INDEX, rowLabel);
    }
    int column = table.getHeader().findColumnIndex(columnLabel);
    table.selectRow(row);
    return SeriesEditionDialogChecker.open(table.editCell(row, column).getButton());
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

  public void checkSeriesNotShown(String seriesName) {
    UISpecAssert.assertFalse(getTable().containsRow(SeriesEvolutionView.LABEL_COLUMN_INDEX, seriesName));
  }

  public SeriesEvolutionChecker checkValue(String rowLabel, String columnLabel, String displayedValue) {
    Table table = getTable();
    int row = table.getRowIndex(SeriesEvolutionView.LABEL_COLUMN_INDEX, rowLabel.toUpperCase());
    int column = table.getHeader().findColumnIndex(columnLabel);
    assertThat(table.cellEquals(row, column, displayedValue));
    return this;
  }

  public SeriesEvolutionChecker checkForeground(String rowLabel, String columnLabel, String expectedColor) {
    Table table = getTable();
    int row = table.getRowIndex(SeriesEvolutionView.LABEL_COLUMN_INDEX, rowLabel);
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

  public void checkClipboardExport(String expectedClipboardContent) throws Exception {
    Table table = getTable();
    table.selectAllRows();
    KeyUtils.pressKey(table, org.uispec4j.Key.plaformSpecificCtrl(org.uispec4j.Key.C));
    Assert.assertEquals(expectedClipboardContent, Clipboard.getContentAsText());
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

  private class BlankColumnConverter implements TableCellValueConverter {
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
