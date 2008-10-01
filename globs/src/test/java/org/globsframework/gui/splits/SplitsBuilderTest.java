package org.globsframework.gui.splits;

import com.jidesoft.swing.JideSplitPane;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.SwingStretches;
import org.globsframework.utils.TestUtils;
import org.uispec4j.finder.ComponentFinder;
import org.uispec4j.finder.ComponentMatchers;

import javax.swing.*;
import java.awt.*;

public class SplitsBuilderTest extends SplitsTestCase {

  public void testTypeError() throws Exception {
    builder.add("label", aTable);
    checkParsingError("<label ref='label'/>", "unexpected type");
  }

  public void testReferencingASubclass() throws Exception {
    builder.add("label", new JLabel("text") {
      // JLabel subclass
    });
    JLabel parsedLabel = parse("<label ref='label'/>");
    assertEquals("text", parsedLabel.getText());
  }

  public void testMovableSplits() throws Exception {
    builder.add(aTable, aList, aButton);
    JButton anotherButton = builder.add("anotherButton", new JButton());
    JideSplitPane hSplit =
      parse(
        "<horizontalSplit>" +
        "  <component ref='aList'/>" +
        "  <verticalSplit>" +
        "    <component ref='aTable'/>" +
        "    <component ref='aButton'/>" +
        "    <component ref='anotherButton'/>" +
        "  </verticalSplit>" +
        "</horizontalSplit>");
    assertEquals(JSplitPane.HORIZONTAL_SPLIT, hSplit.getOrientation());
    assertEquals(2, hSplit.getPaneCount());
    assertSame(aList, hSplit.getPaneAt(0));

    JideSplitPane vSplit = (JideSplitPane)hSplit.getPaneAt(1);
    assertEquals(JSplitPane.VERTICAL_SPLIT, vSplit.getOrientation());
    assertEquals(3, vSplit.getPaneCount());
    assertSame(aTable, vSplit.getPaneAt(0));
    assertSame(aButton, vSplit.getPaneAt(1));
    assertSame(anotherButton, vSplit.getPaneAt(2));
  }

  public void testMovableSplitProperties() throws Exception {
    builder.add(aTable, aList, aButton);
    JideSplitPane hSplit =
      parse(
        "<horizontalSplit dividerSize='21'>" +
        "  <component ref='aList'/>" +
        "  <component ref='aTable'/>" +
        "</horizontalSplit>");
    assertEquals(21, hSplit.getDividerSize());
  }

  public void testHorizontalSplitProportionsAreBasedOnTheComponentWeights() throws Exception {
    checkMovableSplitsProportions("horizontalSplit", 0.1, 0.8);
  }

  public void testVerticalSplitProportionsAreBasedOnTheComponentWeights() throws Exception {
    checkMovableSplitsProportions("verticalSplit", 0.5, 0.5);
  }

  private void checkMovableSplitsProportions(String tag, double... expected) throws Exception {
    builder.add(aTable, aList, aButton);
    JideSplitPane hSplit =
      parse(
        "<" + tag + ">" +
        "  <list ref='aList'/>" +
        "  <table ref='aTable'/>" +
        "  <button ref='aButton'/>" +
        "</" + tag + ">");
    TestUtils.assertEquals(0.05, hSplit.getProportions(), expected);
  }

  public void testUsingRefOnMovableSplit() throws Exception {
    JideSplitPane splitPane = new JideSplitPane();
    splitPane.setDividerSize(2);

    builder.add(aTable, aList);
    builder.add("aSplit", splitPane);

    JideSplitPane hSplit =
      parse(
        "<horizontalSplit ref='aSplit' continuousLayout='true'>" +
        "  <component ref='aList'/>" +
        "  <component ref='aTable'/>" +
        "</horizontalSplit>");

    assertEquals(JideSplitPane.HORIZONTAL_SPLIT, hSplit.getOrientation());
    assertEquals(2, hSplit.getDividerSize());
    assertTrue(hSplit.isContinuousLayout());
  }

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
    JPanel panel1 = builder.add("panel1", new JPanel());
    JPanel panel2 = builder.add("panel2", new JPanel());
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
      checkException(e, "Referenced component 'unknown' not found");
    }
  }

  public void testReferencedColumnComponentWithAnInvalidType() throws Exception {
    try {
      builder.add("panel", new JButton());
      parse("<column ref='panel'>" +
            "  <label/>" +
            "</column>");
      fail();
    }
    catch (Exception e) {
      checkException(e, "Referenced component 'panel' must be a JPanel");
    }
  }

  public void testCreatingAComponentWithAName() throws Exception {
    JFrame frame = parse("<frame>" +
                         "  <button text='Click!'/>" +
                         "</frame>");
    JButton button = (JButton)frame.getContentPane().getComponent(0);
    assertEquals("Click!", button.getText());
  }

  public void testReportsUndefinedComponents() throws Exception {
    try {
      parseWithoutSchemaValidation(
        "<row>" +
        "  <component ref='anUndefinedId'/>'" +
        "</row>");
      fail();
    }
    catch (Exception e) {
      checkException(e, "No component found for ref: anUndefinedId");
    }
  }

  public void testAssignsNamesToCreatedComponents() throws Exception {
    checkAssignedName("<button name='btn'/>", "btn");
    checkAssignedName("<table name='aTable'/>", "aTable");
    checkAssignedName("<list name='aList'/>", "aList");
    checkAssignedName("<label name='lbl'/>", "lbl");
  }

  private void checkAssignedName(String xml, String name) throws Exception {
    assertEquals(name, this.<Component>parse(xml).getName());
  }

  public void testSizes() throws Exception {
    JFrame frame =
      parse("<frame size='(800,600)' minimumSize='(200, 150)' maximumSize='(1000, 900)'/>");
    assertEquals(new Dimension(800, 600), frame.getSize());
    assertEquals(new Dimension(200, 150), frame.getMinimumSize());
    assertEquals(new Dimension(1000, 900), frame.getMaximumSize());

    JButton button =
      parse("<button text='Click!' size='(80,60)' minimumSize='(20, 15)' maximumSize='(100, 90)'/>");
    assertEquals(new Dimension(80, 60), button.getSize());
    assertEquals(new Dimension(20, 15), button.getMinimumSize());
    assertEquals(new Dimension(100, 90), button.getMaximumSize());
  }

  public void testRefMustReferToExistingComponents() throws Exception {
    checkIdErrors("<button ref='foo'/>",
                  "<list ref='foo'/>",
                  "<table ref='foo'/>");
  }

  private void checkIdErrors(String... xmlStreams) throws Exception {
    for (String xml : xmlStreams) {
      try {
        parse(xml);
        fail("No failure reported for XML: " + xml);
      }
      catch (Exception e) {
        // OK
      }
    }
  }

  public void testPropertiesAreSetEvenWhenTheComponentIsWrappedInAMarginPanel() throws Exception {
    builder.add("button", aButton);
    parse("<button ref='button' text='blah' margin='5'/>");
    assertEquals("blah", aButton.getText());
  }

  public void testFixedForeground() throws Exception {
    JButton button = parse("<button foreground='FF0000'/>");
    assertEquals(Color.RED, button.getForeground());
  }

  public void testVariableForeground() throws Exception {
    colorService.set("fgkey", Color.RED);
    JButton btn = parse("<button foreground='fgkey'/>");
    assertEquals(Color.RED, btn.getForeground());
    colorService.set("fgkey", Color.BLUE);
    assertEquals(Color.BLUE, btn.getForeground());
  }

  public void testFixedBackground() throws Exception {
    JButton button = parse("<button background='#FF0000'/>");
    assertEquals(Color.RED, button.getBackground());
  }

  public void testVariableBackground() throws Exception {
    colorService.set("bgkey", Color.RED);
    JButton btn = parse("<button background='bgkey'/>");
    assertEquals(Color.RED, btn.getBackground());
    colorService.set("bgkey", Color.BLUE);
    assertEquals(Color.BLUE, btn.getBackground());
  }

  public void testComplexContainment() throws Exception {
    builder.add(aButton, aList, aTable);
    CardHandler handler = builder.addCardHandler("myHandler");

    JPanel panel = parse("<row>" +
                         "  <borderLayout>" +
                         "    <label text='hello' borderPos='north'/>" +
                         "    <cards ref='myHandler' borderPos='center'>" +
                         "      <card name='a'>" +
                         "        <grid>" +
                         "          <button ref='aButton' gridPos='(0,0)'/>" +
                         "        </grid>" +
                         "      </card>" +
                         "      <card name='b'>" +
                         "        <table ref='aTable'/>" +
                         "      </card>" +
                         "      <card name='c'>" +
                         "        <list ref='aList'/>" +
                         "      </card>" +
                         "    </cards>" +
                         "  </borderLayout>" +
                         "</row>");

    ComponentFinder finder = new ComponentFinder(panel);

    handler.show("a");
    assertSame(aButton, finder.findComponent(ComponentMatchers.fromClass(JButton.class)));
    assertNull(finder.findComponent(ComponentMatchers.fromClass(JTable.class)));
    assertNull(finder.findComponent(ComponentMatchers.fromClass(JList.class)));

    handler.show("b");
    assertNull(finder.findComponent(ComponentMatchers.fromClass(JButton.class)));
    assertSame(aTable, finder.findComponent(ComponentMatchers.fromClass(JTable.class)));
    assertNull(finder.findComponent(ComponentMatchers.fromClass(JList.class)));

    handler.show("c");
    assertNull(finder.findComponent(ComponentMatchers.fromClass(JButton.class)));
    assertNull(finder.findComponent(ComponentMatchers.fromClass(JTable.class)));
    assertSame(aList, finder.findComponent(ComponentMatchers.fromClass(JList.class)));
  }

  public void testTextLocator() throws Exception {
    JLabel label = parse("<label text='$aa.bb.cc'/>");
    assertEquals("aa bb cc", label.getText());
  }

  public void testCursor() throws Exception {
    JLabel label = parse("<label cursor='hand'/>");
    assertEquals(Cursor.HAND_CURSOR, label.getCursor().getType());
  }
}
