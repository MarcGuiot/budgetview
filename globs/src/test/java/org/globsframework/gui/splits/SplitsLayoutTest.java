package org.globsframework.gui.splits;

import org.globsframework.gui.splits.layout.SwingStretches;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.Anchor;

import javax.swing.*;
import java.awt.*;

public class SplitsLayoutTest extends SplitsTestCase {
  public void testRowsAndColumns() throws Exception {
    builder.add(aTable, aList, aButton);
    JPanel row =
      parse(
        "<row>" +
        "  <component ref='aList'/>" +
        "  <column>" +
        "    <component ref='aTable'/>" +
        "    <component ref='aButton'/>" +
        "  </column>" +
        "</row>");
    assertFalse(row.isOpaque());

    Component[] rowComponents = row.getComponents();
    assertEquals(2, rowComponents.length);
    assertSame(aList, rowComponents[0]);

    JPanel column = (JPanel)rowComponents[1];
    assertFalse(column.isOpaque());

    Component[] columnComponents = column.getComponents();
    assertEquals(2, columnComponents.length);
    assertSame(aTable, columnComponents[0]);
    assertSame(aButton, columnComponents[1]);
  }

  public void testMargins() throws Exception {
    builder.add(aTable, aList, aButton);

    JPanel row =
      parse(
        "<row>" +
        "  <component ref='aList'/>" +
        "  <component ref='aTable' " +
        "             marginTop='1' marginBottom='2' " +
        "             marginLeft='3' marginRight='4'/>" +
        "  <button ref='aButton' margin='10' marginTop='20'/>" +
        "</row>");

    Component[] rowComponents = row.getComponents();
    assertEquals(3, rowComponents.length);
    assertSame(aList, rowComponents[0]);
    assertEquals(new Insets(1, 3, 2, 4), getInsets(rowComponents[1], aTable));
    assertEquals(new Insets(20, 10, 10, 10), getInsets(rowComponents[2], aButton));
  }

  public void testGrid() throws Exception {
    builder.add(aTable, aList, aButton);

    JPanel panel =
      parse(
        "<grid>" +
        "  <component ref='aButton' gridPos='(0,0)'/>" +
        "  <component ref='aList' gridPos='(0,1)'/>" +
        "  <component ref='aTable' gridPos='(1,0,1,2)'/>" +
        "</grid>");

    Component[] components = panel.getComponents();
    assertEquals(3, components.length);

    checkGridPos(panel, aButton,
                 0, 0, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.HORIZONTAL, Anchor.CENTER, new Insets(0, 0, 0, 0));
    checkGridPos(panel, aList,
                 0, 1, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.LARGE_WEIGHT,
                 Fill.BOTH, Anchor.CENTER, new Insets(0, 0, 0, 0));
    checkGridPos(panel, aTable,
                 1, 0, 1, 2,
                 SwingStretches.LARGE_WEIGHT, SwingStretches.LARGE_WEIGHT,
                 Fill.BOTH, Anchor.CENTER, new Insets(0, 0, 0, 0));
  }

  public void testCanOverrideGridBagProperties() throws Exception {
    builder.add(aTable, aList, aButton);

    JPanel panel =
      parse(
        "<grid>" +
        "  <component ref='aButton' gridPos='(0,0)' " +
        "             fill='vertical' anchor='north' weightX='2.0' weightY='3.0'/>" +
        "  <component ref='aList' gridPos='(0,1)'/>" +
        "</grid>");

    assertEquals(2, panel.getComponentCount());
    checkGridPos(panel, aButton,
                 0, 0, 1, 1,
                 2.0, 3.0,
                 Fill.VERTICAL, Anchor.NORTH, new Insets(0, 0, 0, 0));
    checkGridPos(panel, aList,
                 0, 1, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.LARGE_WEIGHT,
                 Fill.BOTH, Anchor.CENTER, new Insets(0, 0, 0, 0));
  }

  public void testCanUseLeftRighTopOrBottomInAnchors() throws Exception {
    builder.add(aTable, aList, aButton);

    JPanel panel =
      parse(
        "<grid>" +
        "  <component ref='aButton' gridPos='(0,0)' anchor='left'/>" +
        "  <component ref='aList' gridPos='(0,1)' anchor='right'/>" +
        "  <component ref='aTable' gridPos='(1,0,1,2)' anchor='top'/>" +
        "</grid>");

    assertEquals(Anchor.WEST.getValue(), getConstraints(panel, aButton).anchor);
    assertEquals(Anchor.EAST.getValue(), getConstraints(panel, aList).anchor);
    assertEquals(Anchor.NORTH.getValue(), getConstraints(panel, aTable).anchor);
  }

  public void testRequiresGridPosAttributesOnAllGridSubcomponents() throws Exception {
    builder.add(aTable, aList, aButton);
    checkParsingError("<grid>" +
                      "  <component ref='aList' gridPos='(0,0)'/>" +
                      "  <component ref='aTable' />" +
                      "  <component ref='aButton' gridPos='(1,1)'/>" +
                      "</grid>",
                      "Grid element 'component' must have a GridPos attribute");
  }

  public void testDefaultGridBagPositionAttributesForGrid() throws Exception {
    builder.add(aButton, aList);
    JPanel panel =
      parse(
        "<grid defaultFill='both' defaultAnchor='north'>" +
        "  <component ref='aButton' gridPos='(0,0)'/>" +
        "  <component ref='aList' gridPos='(0,1)' fill='horizontal' anchor='east' />" +
        "</grid>");

    assertEquals(2, panel.getComponentCount());
    checkGridPos(panel, aButton,
                 0, 0, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.BOTH, Anchor.NORTH, new Insets(0, 0, 0, 0));
    checkGridPos(panel, aList,
                 0, 1, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.LARGE_WEIGHT,
                 Fill.HORIZONTAL, Anchor.EAST, new Insets(0, 0, 0, 0));
  }

  public void testDefaultGridBagPositionAttributesForRow() throws Exception {
    builder.add(aButton, aList);
    JPanel panel =
      parse(
        "<row defaultFill='both' defaultAnchor='north'>" +
        "  <component ref='aButton'/>" +
        "  <component ref='aList' fill='horizontal' anchor='east' />" +
        "</row>");

    assertEquals(2, panel.getComponentCount());
    checkGridPos(panel, aButton,
                 0, 0, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.BOTH, Anchor.NORTH, new Insets(0, 0, 0, 0));
    checkGridPos(panel, aList,
                 1, 0, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.LARGE_WEIGHT,
                 Fill.HORIZONTAL, Anchor.EAST, new Insets(0, 0, 0, 0));
  }

  public void testDefaultGridBagPositionAttributesForColumn() throws Exception {
    builder.add(aButton, aList);
    JPanel panel =
      parse(
        "<column defaultFill='both' defaultAnchor='north'>" +
        "  <component ref='aButton'/>" +
        "  <component ref='aList' fill='horizontal' anchor='east' />" +
        "</column>");

    assertEquals(2, panel.getComponentCount());
    checkGridPos(panel, aButton,
                 0, 0, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.BOTH, Anchor.NORTH, new Insets(0, 0, 0, 0));
    checkGridPos(panel, aList,
                 0, 1, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.LARGE_WEIGHT,
                 Fill.HORIZONTAL, Anchor.EAST, new Insets(0, 0, 0, 0));
  }

  public void testDefaultGridBagMarginForGrid() throws Exception {
    builder.add(aButton, aList, aTable);
    JPanel panel =
      parse(
        "<grid defaultMargin='10'>" +
        "  <component ref='aButton' gridPos='(0,0)'/>" +
        "  <component ref='aList' gridPos='(0,1)' marginTop='5' marginBottom='20'/>" +
        "  <component ref='aTable' gridPos='(0,2)' marginLeft='2' marginRight='3'/>" +
        "</grid>");

    assertEquals(3, panel.getComponentCount());
    checkGridPos(panel, aButton,
                 0, 0, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.HORIZONTAL, Anchor.CENTER, new Insets(10, 10, 10, 10));
    checkGridPos(panel, aList,
                 0, 1, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.LARGE_WEIGHT,
                 Fill.BOTH, Anchor.CENTER, new Insets(5, 10, 20, 10));
    checkGridPos(panel, aTable,
                 0, 2, 1, 1,
                 SwingStretches.LARGE_WEIGHT, SwingStretches.LARGE_WEIGHT,
                 Fill.BOTH, Anchor.CENTER, new Insets(10, 2, 10, 3));
  }

  public void testDefaultGridBagMarginForRow() throws Exception {
    checkDefaultGridBagMargin("row");
  }

  public void testDefaultGridBagMarginForColumn() throws Exception {
    checkDefaultGridBagMargin("column");
  }

  private void checkDefaultGridBagMargin(String tagName) throws Exception {
    builder.add(aButton, aList, aTable);
    JPanel panel =
      parse(
        "<" +
        tagName +
        " defaultMargin='10'>" +
        "  <component ref='aButton'/>" +
        "  <component ref='aList' marginTop='5' marginBottom='20'/>" +
        "  <component ref='aTable' marginLeft='2' marginRight='3'/>" +
        "</" +
        tagName +
        ">");

    Component[] rowComponents = panel.getComponents();
    assertEquals(3, rowComponents.length);
    assertEquals(new Insets(10, 10, 10, 10), getInsets(rowComponents[0], aButton));
    assertEquals(new Insets(5, 10, 20, 10), getInsets(rowComponents[1], aList));
    assertEquals(new Insets(10, 2, 10, 3), getInsets(rowComponents[2], aTable));
  }

  public void testCanUseReferencedComponentsForColumnsAndRows() throws Exception {
    JPanel panel1 = builder.add("panel1", new JPanel()).getComponent();
    JPanel panel2 = builder.add("panel2", new JPanel()).getComponent();
    JFrame frame = parse("<frame>" +
                         "  <column ref='panel1' background='#00FF00'>" +
                         "    <row ref='panel2' background='#0000FF'>" +
                         "      <label text='hello'/>" +
                         "    </row>" +
                         "  </column>" +
                         "</frame>");

    JPanel column = (JPanel)frame.getContentPane().getComponent(0);
    assertSame(panel1, column);
    assertEquals(Color.GREEN, column.getBackground());

    JPanel row = (JPanel)column.getComponent(0);
    assertSame(panel2, row);
    assertEquals(Color.BLUE, row.getBackground());

    JLabel label = (JLabel)panel2.getComponent(0);
    assertEquals("hello", label.getText());
  }

  public void testReferencedColumnComponentNotFound() throws Exception {
    try {
      parse("<column ref='unknown'>" +
            "  <label/>" +
            "</column>");
      fail();
    }
    catch (Exception e) {
      checkExceptionCause(e, "Referenced component 'unknown' not found");
    }
  }

  public void testHorizontalBoxes() throws Exception {
    checkBoxSequence("horizontalBoxes");
  }

  public void testVerticalBoxes() throws Exception {
    checkBoxSequence("verticalBoxes");
  }

  private void checkBoxSequence(String tag) throws Exception {
    builder.add(aButton, aList);
    JPanel panel =
      parse(
        "<" + tag + ">" +
        "  <component ref='aButton'/>" +
        "  <component ref='aList' fill='horizontal' anchor='east' />" +
        "</" + tag + ">");

    assertTrue(panel.getLayout() instanceof BoxLayout);
    assertEquals(2, panel.getComponentCount());
    assertSame(aButton, panel.getComponent(0));
    assertSame(aList, panel.getComponent(1));
  }
}
