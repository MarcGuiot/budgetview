package org.uispec4j;

import junit.framework.AssertionFailedError;
import org.uispec4j.utils.AssertionFailureNotDetectedError;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TableContentTest extends TableTestCase {
  public void testAssertContentEquals() throws Exception {
    assertTrue(table.contentEquals(new Object[][]{
      {"a", Boolean.TRUE, "3"},
      {"c", Boolean.FALSE, "4"}
    }));
  }

  public void testAssertContentEqualsFailure() throws Exception {
    try {
      assertTrue(table.contentEquals(new Object[][]{
        {"a", Boolean.TRUE, "3"},
        {"c", Boolean.FALSE, "ERROR!"}
      }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
    }
  }

  public void testAssertEmptyFailure() throws Exception {
    try {
      assertTrue(table.isEmpty());
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Expected: empty table but was:[[a,\ttrue,\t3]\n [c,\tfalse,\t4]]", e.getMessage());
    }
  }

  public void testContentEqualsUsesTheModelWhenAnUnknownRendererComponentIsUsed() throws Exception {
    installJPanelRenderer();
    assertTrue(table.contentEquals(new Object[][]{
      {"a", "true", "3"},
      {"c", "false", "4"}
    }));
    try {
      assertTrue(table.contentEquals(new Object[][]{
        {"a", Boolean.TRUE, "3"},
        {"c", Boolean.FALSE, "4"}
      }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
    }
  }

  public void testContentEqualsWithUnknownRendererComponentAndNullModelValue() throws Exception {
    installJPanelRenderer();
    jTable.getModel().setValueAt(null, 0, 1);
    assertTrue(table.contentEquals(new Object[][]{
      {"a", "", "3"},
      {"c", "false", "4"}
    }));
    try {
      assertTrue(table.contentEquals(new Object[][]{
        {"a", "", "3"},
        {"c", Boolean.FALSE, "4"}
      }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
    }
  }

  public void testContentEqualsWhenColumnMoved() throws Exception {
    jTable.moveColumn(0, 1);
    assertTrue(table.contentEquals(new Object[][]{
      {Boolean.TRUE, "a", "3"},
      {Boolean.FALSE, "c", "4"}
    }));
    try {
      assertTrue(table.contentEquals(new Object[][]{
        {"a", Boolean.TRUE, "3"},
        {"c", Boolean.FALSE, "4"}
      }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
    }
  }

  public void testContentEqualsAfterSettingACustomCellValueConverterAsDefault() throws Exception {
    table.setDefaultCellValueConverter(new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        if ((row == 1) && (column == 1)) {
          return "toto";
        }
        return modelObject.toString();
      }
    });
    assertTrue(table.contentEquals(new Object[][]{
      {"a", "true", "3"},
      {"c", "toto", "4"}
    }));
    try {
      assertTrue(table.contentEquals(new Object[][]{
        {"a", Boolean.TRUE, "3"},
        {"c", Boolean.FALSE, "4"}
      }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
    }
  }

  public void testContentEqualsAfterSettingACustomCellValueConverterOnAColumn() throws Exception {
    table.setCellValueConverter(0, new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        return "custom " + modelObject;
      }
    });
    assertTrue(table.contentEquals(new Object[][]{
      {"custom a", Boolean.TRUE, "3"},
      {"custom c", Boolean.FALSE, "4"}
    }));
    try {
      assertTrue(table.contentEquals(new Object[][]{
        {"a", Boolean.TRUE, "3"},
        {"c", Boolean.FALSE, "4"}
      }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
    }
  }

  public void testContentEqualsAfterSettingACustomCellValueConverterOnAColumnThatHasBeenMoved() throws Exception {
    jTable.moveColumn(0, 1);
    table.setCellValueConverter(0, new TableCellValueConverter() {
      public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
        return "custom " + modelObject;
      }
    });
    assertTrue(table.contentEquals(new Object[][]{
      {"custom true", "a", "3"},
      {"custom false", "c", "4"}
    }));
    try {
      assertTrue(table.contentEquals(new Object[][]{
        {Boolean.TRUE, "a", "3"},
        {Boolean.FALSE, "c", "4"}
      }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
    }
  }

  public void testContentEqualsAfterSettingACustomCellValueConverterThatHandlesSelection() throws Exception {
    jTable.setDefaultRenderer(Boolean.class, new DefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        value = isSelected ? "selected " + value : value;
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      }
    });
    assertTrue(table.contentEquals(new Object[][]{
      {"a", "true", "3"},
      {"c", "false", "4"}
    }));

    table.selectRow(0);
    assertTrue(table.contentEquals(new Object[][]{
      {"a", "selected true", "3"},
      {"c", "false", "4"}
    }));
  }

  public void testCustomContentEquals() throws Exception {
    assertTrue(table.contentEquals(
      new String[]{"0", "1", "2"},
      new Object[][]{
        {"a", Boolean.TRUE, "3"},
        {"c", Boolean.FALSE, "4"}
      }));

    assertTrue(table.contentEquals(
      new String[]{"0", "2", "1"},
      new Object[][]{
        {"a", "3", Boolean.TRUE},
        {"c", "4", Boolean.FALSE}
      }));

    assertTrue(table.contentEquals(
      new String[]{"2", "0"},
      new Object[][]{
        {"3", "a"},
        {"4", "c"}
      }));
  }

  public void testCustomContentEqualsErrors() throws Exception {
    try {
      assertTrue(table.contentEquals(
        new String[]{"0", "2"},
        new Object[][]{
          {"a", "3"},
          {"c", "ERROR!"}
        }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertTrue(e.getMessage().startsWith("Error at (1, 1)"));
    }
  }

  public void testCustomContentEqualsChecksTheNumberOfColumns() throws Exception {
    try {
      assertTrue(table.contentEquals(
        new String[]{"0", "2", "1"},
        new Object[][]{
          {"a", "3"},
          {"a", "3"},
        }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Expected array should have 3 elements for each row - invalid row 0: [a,3]", e.getMessage());
    }
  }

  public void testCustomContentEqualsChecksTheColumnNames() throws Exception {
    try {
      assertTrue(table.contentEquals(
        new String[]{"0", "2", "unknown"},
        new Object[][]{
          {"a", "3", "x"}
        }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Column 'unknown' not found", e.getMessage());
    }
  }

  public void testCustomContentEqualsChecksTheNumberOfRows() throws Exception {
    try {
      assertTrue(table.contentEquals(
        new String[]{"2", "0"},
        new Object[][]{
          {"3", "a"},
          {"4", "c"},
          {"5", "e"}
        }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertTrue(e.getMessage().startsWith("Expected 3 rows but found 2"));
    }
  }

  public void testCellEquals() throws Exception {
    assertTrue(table.cellEquals(0, 0, "a"));
    assertTrue(table.cellEquals(1, 1, Boolean.FALSE));
  }

  public void testCellEqualsError() throws Exception {
    try {
      assertTrue(table.cellEquals(0, 0, "invalidValue"));
      fail();
    }
    catch (AssertionFailedError e) {
      assertEquals("Error at (0,0) - expected:<invalidValue> but was:<a>", e.getMessage());
    }
  }

  public void testCellEqualsWithConverter() throws Exception {
    assertTrue(table.cellEquals(0, 0, "-a-", new DummyTableCellValueConverter()));
    assertTrue(table.cellEquals(1, 1, "-false-", new DummyTableCellValueConverter()));
  }

  public void testRowEquals() throws Exception {
    assertTrue(table.rowEquals(0, new Object[]{"a", Boolean.TRUE, "3"}));
    assertTrue(table.rowEquals(1, new Object[]{"c", Boolean.FALSE, "4"}));
  }

  public void testRowEqualsFailures() throws Exception {
    try {
      assertTrue(table.rowEquals(1, new Object[]{"c", Boolean.TRUE, "4"}));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("expected:<[c,true,4]> but was:<c,false,4>", e.getMessage());
    }

    try {
      assertTrue(table.rowEquals(-1, new Object[]{"a", "b", "c"}));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Row index should be positive", e.getMessage());
    }

    try {
      assertTrue(table.rowEquals(2, new Object[]{"a", "b", "c"}));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Table contains only 2 rows, unable to access row 2", e.getMessage());
    }
  }

  public void testCustomRowEquals() throws Exception {
    assertTrue(table.rowEquals(0, new String[]{"0", "1", "2"}, new Object[]{"a", Boolean.TRUE, "3"}));
    assertTrue(table.rowEquals(1, new String[]{"0", "1", "2"}, new Object[]{"c", Boolean.FALSE, "4"}));

    assertTrue(table.rowEquals(0, new String[]{"0", "2", "1"}, new Object[]{"a", "3", Boolean.TRUE}));

    assertTrue(table.rowEquals(0, new String[]{"2", "0"}, new Object[]{"3", "a"}));
  }

  public void testCustomRowEqualsErrors() throws Exception {
    try {
      assertTrue(table.rowEquals(0, new String[]{"0", "2"}, new Object[]{"a", "xxxxxx"}));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Unexpected content at row 0\n" +
                   "Expected: [a,xxxxxx]\n" +
                   "Actual:   [a,3]", e.getMessage());
    }
  }

  public void testCustomRowEqualsChecksTheNumberOfColumns() throws Exception {
    try {
      assertTrue(table.rowEquals(0, new String[]{"0", "2", "1"}, new Object[]{"a", "3"}));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Expected array should have 3 elements for each row - invalid row 0: [a,3]", e.getMessage());
    }
  }

  public void testCustomRowEqualsChecksTheColumnNames() throws Exception {
    try {
      assertTrue(table.rowEquals(0, new String[]{"0", "2", "unknown"}, new Object[]{"a", "3", "x"}));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Column 'unknown' not found", e.getMessage());
    }
  }

  public void testCustomRowEqualsChecksTheNumberOfRows() throws Exception {
    try {
      assertTrue(table.rowEquals(3, new String[]{"2", "0"}, new Object[]{"3", "a"}));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Table contains only 2 rows, unable to access row 3", e.getMessage());
    }
  }

  public void testColumnEquals() throws Exception {
    assertTrue(table.columnEquals(0, new Object[]{"a", "c"}));
    assertTrue(table.columnEquals(1, new Object[]{Boolean.TRUE, Boolean.FALSE}));
    assertTrue(table.columnEquals(2, new Object[]{"3", "4"}));
  }

  public void testColumnEqualsFailures() throws Exception {
    try {
      assertTrue(table.columnEquals(1, new Object[]{Boolean.TRUE, Boolean.TRUE}));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("expected:<[true,true]> but was:<true,false>", e.getMessage());
    }

    try {
      assertTrue(table.columnEquals(-1, new Object[]{"a", "c"}));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Column index should be positive", e.getMessage());
    }

    try {
      assertTrue(table.columnEquals(3, new Object[]{"3", "4"}));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Table contains only 3 columns, unable to access column 3", e.getMessage());
    }
  }

  public void testGetRowAndColumnCount() throws Exception {
    assertEquals(2, table.getRowCount());
    assertTrue(table.rowCountEquals(2));
    assertEquals(3, table.getColumnCount());
    assertTrue(table.columnCountEquals(3));
  }

  public void testRowCountError() throws Exception {
    try {
      assertTrue(table.rowCountEquals(9999));
      fail();
    }
    catch (AssertionFailedError e) {
      assertEquals("Unexpected number of rows - expected:<9999> but was:<2>", e.getMessage());
    }
  }

  public void testColumnCountError() throws Exception {
    try {
      assertTrue(table.columnCountEquals(9999));
      fail();
    }
    catch (AssertionFailedError e) {
      assertEquals("Unexpected number of columns - expected:<9999> but was:<3>", e.getMessage());
    }
  }

  public void testGetContentAt() throws Exception {
    assertEquals("a", table.getContentAt(0, 0));
    assertEquals(Boolean.TRUE, table.getContentAt(0, 1));
    assertEquals("c", table.getContentAt(1, 0));
    assertEquals(Boolean.FALSE, table.getContentAt(1, 1));

    jTable.getModel().setValueAt(null, 0, 0);
    assertEquals("", table.getContentAt(0, 0));
  }

  public void testGetContentAtWorksWhenTheRendererIsUnusable() throws Exception {
    jTable.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row, int column) {
        return new JPanel();
      }
    });
    assertEquals("a", table.getContentAt(0, 0));
  }

  public void testGetContentAtWithConverter() throws Exception {
    assertEquals("-a-", table.getContentAt(0, 0, new DummyTableCellValueConverter()));

    assertEquals(new Integer(3), table.getContentAt(0, 2, ModelTableCellValueConverter.INSTANCE));
  }

  public void testGetEditorComponentAt() throws Exception {
    Component firstCellComponent = table.getSwingEditorComponentAt(0, 0);
    assertTrue(firstCellComponent instanceof JTextField);

    Component secondCellComponent = table.getSwingEditorComponentAt(0, 1);
    assertTrue(secondCellComponent instanceof JCheckBox);

    Component thirdCellComponent = table.getSwingEditorComponentAt(0, 2);
    assertTrue(thirdCellComponent instanceof JComboBox);
  }

  public void testToString() throws Exception {
    assertEquals("[[a,\ttrue,\t3]\n" +
                 " [c,\tfalse,\t4]]",
                 table.toString());
  }

  public void testForegroundEquals() throws Exception {
    assertTrue(table.foregroundEquals(new String[][]{
      {"black", "black", "black"},
      {"black", "black", "black"}
    }));
    table.foregroundEquals("black");
  }

  public void testBackgrounEqualsWithDefaultValue() throws Exception {
    jTable.setBackground(Color.BLUE);
    assertTrue(table.backgroundEquals("blue"));
    try {
      assertTrue(table.backgroundEquals("green"));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("expected:<GREEN> but was:<0000FF>", e.getMessage());
    }
  }

  public void testBackgroundEquals() throws Exception {
    jTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (row == 1) {
          component.setBackground(Color.red);
        }
        else {
          component.setBackground(Color.blue);
        }
        return component;
      }
    });
    assertTrue(table.backgroundEquals(new Object[][]{
      {"white", "blue", "white"},
      {Color.white, Color.red, Color.white}
    }));

    try {
      assertTrue(table.backgroundEquals(new Object[][]{
        {"white", "green", "white"},
        {Color.white, Color.red, Color.white}
      }));
      throw new AssertionFailureNotDetectedError();
    }
    catch (AssertionFailedError e) {
      assertEquals("Error at (0, 1) - expected:<GREEN> but was:<0000FF>", e.getMessage());
    }
  }

  public void testBackgroundEqualsUsesTheSelectionBackgroundColor() throws Exception {
    jTable.setCellSelectionEnabled(true);
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (isSelected) {
          component.setBackground(Color.red);
        }
        else {
          component.setBackground(Color.black);
        }
        return component;
      }
    };
    jTable.setDefaultRenderer(Object.class, renderer);
    jTable.setDefaultRenderer(Boolean.class, renderer);
    jTable.setDefaultRenderer(Integer.class, renderer);
    table.selectCell(0, 1);
    table.backgroundEquals(new Object[][]{
      {Color.BLACK, "red", Color.BLACK},
      {Color.BLACK, Color.BLACK, Color.BLACK}
    });
  }

  public void testStartsWith() throws Exception {
    assertTrue(table.startsWith(new Object[][]{
      {"a", Boolean.TRUE, "3"}
    }));
  }

  public void testStartsWithFailsBecauseOfWrongData() throws Exception {
    assertFalse(table.startsWith(new Object[][]{
      {"Wrong", Boolean.TRUE, "3"}
    }));
  }

  public void testStartsWithFailsBecauseTheExpectedFirstLineIsNotTheFirst() throws Exception {
    assertFalse(table.startsWith(new Object[][]{
      {"c", Boolean.FALSE, "4"}
    }));
  }

  public void testStartsWithFailsBecauseTheExpectedContentIsTooLarge() throws Exception {
    assertFalse(table.startsWith(new Object[][]{
      {"a", Boolean.TRUE, "3", "5"}
    }));
  }

  public void testStartsWithFailsBecauseTheExpectedContentIsTooLong() throws Exception {
    checkAssertionFails(table.startsWith(new Object[][]{
      {"a", Boolean.TRUE, "3"},
      {"c", Boolean.FALSE, "4"},
      {"d", Boolean.FALSE, "5"}
    }), "Table contains only 2 rows whereas 3 rows are expected.");
  }

  public void testEndsWith() throws Exception {
    assertTrue(table.endsWith(new Object[][]{
      {"c", Boolean.FALSE, "4"}
    }));
  }

  public void testEndsWithFailsBecauseOfWrongData() throws Exception {
    assertFalse(table.endsWith(new Object[][]{
      {"Wrong", Boolean.FALSE, "4"}
    }));
  }

  public void testEndsWithFailsBecauseTheExpectedFirstLineIsNotTheFirst() throws Exception {
    assertFalse(table.endsWith(new Object[][]{
      {"a", Boolean.TRUE, "3"}
    }));
  }

  public void testEndsWithFailsBecauseTheExpectedContentIsTooLong() throws Exception {
    checkAssertionFails(table.endsWith(new Object[][]{
      {"d", Boolean.FALSE, "5"},
      {"a", Boolean.TRUE, "3"},
      {"c", Boolean.FALSE, "4"}
    }), "Table contains only 2 rows whereas 3 rows are expected.");
  }

  public void testContainsRow() throws Exception {
    assertTrue(table.containsRow(new Object[]{
      "a", Boolean.TRUE, "3"
    }));
    assertTrue(table.containsRow(new Object[]{
      "c", Boolean.FALSE, "4"
    }));
  }

  public void testContainsRowFailsBecauseOfWrongData() throws Exception {
    assertFalse(table.containsRow(new Object[]{
      "Wrong", Boolean.TRUE, "3"
    }));
  }

  private static class DummyTableCellValueConverter implements TableCellValueConverter {
    public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
      return "-" + modelObject + "-";
    }
  }
}
