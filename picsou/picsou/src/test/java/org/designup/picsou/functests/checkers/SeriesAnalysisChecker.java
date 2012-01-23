package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.designup.picsou.functests.checkers.components.HistoChartChecker;
import org.designup.picsou.functests.checkers.components.StackChecker;
import org.designup.picsou.functests.checkers.components.TableChecker;
import org.designup.picsou.gui.series.analysis.SeriesEvolutionTableView;
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

public class SeriesAnalysisChecker extends ExpandableTableChecker {

  public HistoChartChecker histoChart;
  public StackChecker balanceChart;
  public StackChecker seriesChart;

  private Table table;

  private static final String PANEL_NAME = "seriesAnalysisView";
  private int COUNT_COLUMN = 10;
  private Panel panel;

  public SeriesAnalysisChecker(Window mainWindow) {
    super(mainWindow);
    this.histoChart = new HistoChartChecker(mainWindow, "seriesAnalysisView", "histoChart");
    this.balanceChart = new StackChecker(mainWindow, PANEL_NAME, "balanceChart");
    this.seriesChart = new StackChecker(mainWindow, PANEL_NAME, "seriesChart");
  }

  public void checkBreadcrumb(String text) {
    assertThat(getPanel().getTextBox("breadcrumb").textEquals(text));
  }

  public void clickBreadcrumb(String link) {
    getPanel().getTextBox("breadcrumb").clickOnHyperlink(link);
  }

  public SeriesTableChecker initContent() {
    return new SeriesTableChecker();
  }

  public SeriesAnalysisChecker checkRowLabels(String... labels) {
    assertThat(getTable().columnEquals(1, labels));
    return this;
  }

  public void checkColumnNames(String... names) {
    assertThat(getTable().getHeader().contentEquals(COUNT_COLUMN, Utils.join("", names)));
  }

  public void checkRow(String label, String... values) {
    Table table = getTable();
    int index = getRow(label, table);
    assertThat(table.rowEquals(index, 0, COUNT_COLUMN, Utils.join(new String[]{"", label}, values)));
  }

  public SeriesAnalysisChecker select(String... labels) {
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

  public void checkSelected(String... labels) {
    Table table = getTable();
    SortedSet<Integer> indices = new TreeSet<Integer>();
    for (String label : labels) {
      for (int index : table.getRowIndices(SeriesEvolutionTableView.LABEL_COLUMN_INDEX, label)) {
        indices.add(index);
      }
    }
    assertThat(table.rowsAreSelected(Utils.toArray(indices)));
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
      views.selectAnalysis();
      table = mainWindow.getTable("seriesEvolutionTable");
      table.setCellValueConverter(0, new BlankColumnConverter());
      ColumnConverter converter = new ColumnConverter();
      for (int i = 1; i < 3 + table.getColumnCount() - 1; i++) {
        table.setCellValueConverter(i, converter);
      }
    }
    return table;
  }

  protected int getLabelColumnIndex() {
    return 0;
  }

  protected Panel getPanel() {
    if (panel == null) {
      views.selectAnalysis();
      panel = mainWindow.getPanel(PANEL_NAME);
    }
    return panel;
  }

  public SeriesEditionDialogChecker editSeries(String rowLabel) {
    Table table = getTable();
    int row = getRow(rowLabel.toUpperCase(), table);
    if (row == -1) {
      row = table.getRowIndex(SeriesEvolutionTableView.LABEL_COLUMN_INDEX, rowLabel);
    }
    return SeriesEditionDialogChecker.open(table.editCell(row, SeriesEvolutionTableView.LABEL_COLUMN_INDEX).getButton().triggerClick());
  }

  public SeriesAmountEditionDialogChecker editSeries(String rowLabel, String columnLabel) {
    Table table = getTable();
    int row = getRow(rowLabel.toUpperCase(), table);
    if (row == -1) {
      row = table.getRowIndex(SeriesEvolutionTableView.LABEL_COLUMN_INDEX, rowLabel);
    }
    int column = table.getHeader().findColumnIndex(columnLabel);
    table.selectRow(row);
    return SeriesAmountEditionDialogChecker.open(table.editCell(row, column).getButton().triggerClick());
  }

  public void checkTableIsEmpty(String... labels) {
    SeriesAnalysisChecker.SeriesTableChecker checker = initContent();
    String[] values = new String[COUNT_COLUMN];
    Arrays.fill(values, "");
    for (String label : labels) {
      checker.add(label, values);
    }
    checker.check();
  }

  public void checkSeriesNotShown(String seriesName) {
    assertFalse(getTable().containsRow(SeriesEvolutionTableView.LABEL_COLUMN_INDEX, seriesName));
  }

  public SeriesAnalysisChecker checkForeground(String rowLabel, String columnLabel, String expectedColor) {
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

  public void checkClipboardExport(String expectedClipboardContent) throws Exception {

    Table table = getTable();
    table.selectAllRows();
    KeyUtils.pressKey(table, org.uispec4j.Key.plaformSpecificCtrl(org.uispec4j.Key.C));
    Assert.assertEquals(expectedClipboardContent, Clipboard.getContentAsText());

    Clipboard.putText("something to clean up the clipboard before starting again");

    PopupMenuInterceptor
      .run(getTable().triggerRightClick(0, 0))
      .getSubMenu("Copy")
      .click();
    Assert.assertEquals(expectedClipboardContent, Clipboard.getContentAsText());
  }

  public SeriesAnalysisChecker checkHistoChartLabel(String text) {
    TextBox textBox = getPanel().getTextBox("histoChartLabel");
    Assert.assertEquals(text, org.uispec4j.utils.Utils.cleanupHtml(textBox.getText()));
    return this;
  }

  public SeriesAnalysisChecker checkBalanceChartLabel(String text) {
    TextBox textBox = getPanel().getTextBox("balanceChartLabel");
    Assert.assertEquals(text, org.uispec4j.utils.Utils.cleanupHtml(textBox.getText()));
    return this;
  }

  public SeriesAnalysisChecker checkSeriesChartLabel(String text) {
    TextBox textBox = getPanel().getTextBox("seriesChartLabel");
    Assert.assertEquals(text, org.uispec4j.utils.Utils.cleanupHtml(textBox.getText()));
    return this;
  }

  public SeriesAnalysisChecker selectNextMonth() {
    getPanel().getButton("nextMonth").click();
    return this;
  }

  public SeriesAnalysisChecker checkNextMonthSelectionDisabled() {
    assertFalse(getPanel().getButton("nextMonth").isEnabled());
    return this;
  }

  public SeriesAnalysisChecker selectPreviousMonth() {
    getPanel().getButton("previousMonth").click();
    return this;
  }

  public SeriesAnalysisChecker checkPreviousMonthSelectionDisabled() {
    assertFalse(getPanel().getButton("previousMonth").isEnabled());
    return this;
  }

  public SeriesAnalysisChecker checkTableHidden() {
    checkComponentVisible(getPanel(), JPanel.class, "tablePanel", false);
    return this;
  }

  public SeriesAnalysisChecker checkTableShown() {
    checkComponentVisible(getPanel(), JPanel.class, "tablePanel", true);
    return this;
  }

  public SeriesAnalysisChecker toggleTable() {
    getPanel().getButton("toggleTable").click();
    return this;
  }

  public SeriesAnalysisChecker checkToggleLabel(String expectedLabel) {
    assertThat(getPanel().getButton("toggleTable").textEquals(expectedLabel));
    return this;
  }

  public SeriesAnalysisChecker checkLegendShown(String lineText, String fillText) {
    Panel legendPanel = getPanel().getPanel("histoChartLegend");
    assertThat(legendPanel.isVisible());
    assertThat(legendPanel.getTextBox("lineLabelText").textEquals(lineText));
    assertThat(legendPanel.getTextBox("fillLabelText").textEquals(fillText));
    return this;
  }

  public SeriesAnalysisChecker checkLegendHidden() {
    Panel legendPanel = getPanel().getPanel("histoChartLegend");
    assertFalse(legendPanel.isVisible());
    return this;
  }

  public class SeriesTableChecker extends TableChecker {

    public SeriesTableChecker add(String label, String... monthValues) {
      super.add(Utils.join(new String[]{"", label}, monthValues));
      return this;
    }

    protected Table getTable() {
      return SeriesAnalysisChecker.this.getTable();
    }

    public void check() {
      Object[][] expectedContent = content.toArray(new Object[content.size()][]);
      org.uispec4j.assertion.UISpecAssert.assertTrue(getTable().blockEquals(0, 0, COUNT_COLUMN, content.size(), expectedContent));
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
}
