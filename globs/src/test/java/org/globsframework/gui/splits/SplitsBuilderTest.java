package org.globsframework.gui.splits;

import com.jidesoft.swing.JideSplitPane;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.utils.TestUtils;
import org.uispec4j.finder.ComponentFinder;
import static org.uispec4j.finder.ComponentMatchers.*;
import org.uispec4j.*;

import javax.swing.*;
import java.awt.*;

public class SplitsBuilderTest extends SplitsTestCase {

  public void testTypeError() throws Exception {
    builder.add("label", aTable);
    checkParsingError("<label ref='label'/>",
                      "Error for tag: label - unexpected type 'JTable' for referenced component 'label' " +
                      "- expected type: javax.swing.JLabel");
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
    JButton anotherButton = builder.add("anotherButton", new JButton()).getComponent();
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

    ComponentFinder finder = new ComponentFinder(panel, new org.uispec4j.Panel(panel));

    handler.show("a");
    assertSame(aButton, finder.findComponent(fromClass(JButton.class)));
    assertNull(finder.findComponent(fromClass(JTable.class)));
    assertNull(finder.findComponent(fromClass(JList.class)));

    handler.show("b");
    assertNull(finder.findComponent(fromClass(JButton.class)));
    assertSame(aTable, finder.findComponent(fromClass(JTable.class)));
    assertNull(finder.findComponent(fromClass(JList.class)));

    handler.show("c");
    assertNull(finder.findComponent(fromClass(JButton.class)));
    assertNull(finder.findComponent(fromClass(JTable.class)));
    assertSame(aList, finder.findComponent(fromClass(JList.class)));
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
