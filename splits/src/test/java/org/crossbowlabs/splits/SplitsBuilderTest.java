package org.crossbowlabs.splits;

import junit.framework.TestCase;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.components.JStyledPanel;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.Anchor;
import org.crossbowlabs.splits.layout.CardHandler;
import org.crossbowlabs.splits.layout.Fill;
import org.crossbowlabs.splits.layout.SwingStretches;
import org.crossbowlabs.splits.utils.*;
import org.uispec4j.finder.ComponentFinder;
import org.uispec4j.finder.ComponentMatchers;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.awt.*;
import java.io.Reader;
import java.io.StringReader;

public class SplitsBuilderTest extends TestCase {

  private SplitsBuilder builder;
  private ColorService colorService = new ColorService();
  private DummyIconLocator iconLocator = new DummyIconLocator();
  private DummyTextLocator textLocator = new DummyTextLocator();
  private JTable aTable = new JTable();
  private JList aList = new JList();
  private JButton aButton = new JButton();

  protected void setUp() throws Exception {
    builder = new SplitsBuilder(colorService, iconLocator, textLocator);

    aTable.setName("aTable");
    aList.setName("aList");
    aButton.setName("aButton");
  }

  public void testMovableSplits() throws Exception {
    builder.add(aTable, aList, aButton);
    JSplitPane hSplit =
      (JSplitPane) parse(
        "<horizontalSplit>" +
        "  <component ref='aList'/>" +
        "  <verticalSplit>" +
        "    <component ref='aTable'/>" +
        "    <component ref='aButton'/>" +
        "  </verticalSplit>" +
        "</horizontalSplit>");
    assertEquals(JSplitPane.HORIZONTAL_SPLIT, hSplit.getOrientation());
    assertSame(aList, hSplit.getLeftComponent());

    JSplitPane vSplit = (JSplitPane) hSplit.getRightComponent();
    assertEquals(JSplitPane.VERTICAL_SPLIT, vSplit.getOrientation());
    assertSame(aTable, vSplit.getLeftComponent());
    assertSame(aButton, vSplit.getRightComponent());
  }

  public void testMovableSplitProperties() throws Exception {
    builder.add(aTable, aList, aButton);
    JSplitPane hSplit =
      (JSplitPane) parse(
        "<horizontalSplit dividerSize='21' dividerLocation='250'>" +
        "  <component ref='aList'/>" +
        "  <component ref='aTable'/>" +
        "</horizontalSplit>");
    assertEquals(21, hSplit.getDividerSize());
    assertEquals(250, hSplit.getDividerLocation());
  }

  public void testDefaultMovableSplitLocation() throws Exception {
    builder.add(aTable, aList, aButton);
    JSplitPane hSplit =
      (JSplitPane) parse(
        "<horizontalSplit>" +
        "  <component ref='aList'/>" +
        "  <component ref='aTable'/>" +
        "</horizontalSplit>");
    assertEquals(0.1, hSplit.getResizeWeight(), 0.1);
  }

  public void testUsingRefOnMovableSplit() throws Exception {
    JSplitPane splitPane = new JSplitPane();
    splitPane.setDividerSize(2);

    builder.add(aTable, aList);
    builder.add("aSplit", splitPane);

    JSplitPane hSplit =
      (JSplitPane) parse(
        "<horizontalSplit ref='aSplit' continuousLayout='true'>" +
        "  <component ref='aList'/>" +
        "  <component ref='aTable'/>" +
        "</horizontalSplit>");

    assertEquals(JSplitPane.HORIZONTAL_SPLIT, hSplit.getOrientation());
    assertEquals(2, hSplit.getDividerSize());
    assertTrue(hSplit.isContinuousLayout());
  }

  public void testRowsAndColumns() throws Exception {
    builder.add(aTable, aList, aButton);
    JPanel row =
      (JPanel) parse(
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

    JPanel column = (JPanel) rowComponents[1];
    assertFalse(column.isOpaque());

    Component[] columnComponents = column.getComponents();
    assertEquals(2, columnComponents.length);
    assertSame(aTable, columnComponents[0]);
    assertSame(aButton, columnComponents[1]);
  }

  public void testMargins() throws Exception {
    builder.add(aTable, aList, aButton);

    JPanel row =
      (JPanel) parse(
        "<row>" +
        "  <component ref='aList'/>" +
        "  <component ref='aTable' " +
        "             marginTop='1' marginBottom='2' " +
        "             marginLeft='3' marginRight='4'/>" +
        "  <component ref='aButton' margin='10' marginTop='20'/>" +
        "</row>");

    Component[] rowComponents = row.getComponents();
    assertEquals(3, rowComponents.length);
    assertSame(aList, rowComponents[0]);
    assertEquals(new Insets(1, 3, 2, 4), getConstraints(rowComponents[1], aTable).insets);
    assertEquals(new Insets(20, 10, 10, 10), getConstraints(rowComponents[2], aButton).insets);
  }

  public void testGrid() throws Exception {
    builder.add(aTable, aList, aButton);

    JPanel panel =
      (JPanel) parse(
        "<grid>" +
        "  <component ref='aButton' gridPos='(0,0)'/>" +
        "  <component ref='aList' gridPos='(0,1)'/>" +
        "  <component ref='aTable' gridPos='(1,0,1,2)'/>" +
        "</grid>");

    Component[] components = panel.getComponents();
    assertEquals(3, components.length);

    checkGridPos(panel, aButton,
                 0, 0, 1, 1,
                 SwingStretches.NULL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.NONE, Anchor.CENTER, new Insets(0, 0, 0, 0));
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
      (JPanel) parse(
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
      (JPanel) parse(
        "<grid defaultFill='both' defaultAnchor='north'>" +
        "  <component ref='aButton' gridPos='(0,0)'/>" +
        "  <component ref='aList' gridPos='(0,1)' fill='horizontal' anchor='east' />" +
        "</grid>");

    assertEquals(2, panel.getComponentCount());
    checkGridPos(panel, aButton,
                 0, 0, 1, 1,
                 SwingStretches.NULL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.BOTH, Anchor.NORTH, new Insets(0, 0, 0, 0));
    checkGridPos(panel, aList,
                 0, 1, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.LARGE_WEIGHT,
                 Fill.HORIZONTAL, Anchor.EAST, new Insets(0, 0, 0, 0));
  }

  public void testDefaultGridBagPositionAttributesForRow() throws Exception {
    builder.add(aButton, aList);
    JPanel panel =
      (JPanel) parse(
        "<row defaultFill='both' defaultAnchor='north'>" +
        "  <component ref='aButton'/>" +
        "  <component ref='aList' fill='horizontal' anchor='east' />" +
        "</row>");

    assertEquals(2, panel.getComponentCount());
    checkGridPos(panel, aButton,
                 0, 0, 1, 1,
                 SwingStretches.NULL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.BOTH, Anchor.NORTH, new Insets(0, 0, 0, 0));
    checkGridPos(panel, aList,
                 1, 0, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.LARGE_WEIGHT,
                 Fill.HORIZONTAL, Anchor.EAST, new Insets(0, 0, 0, 0));
  }

  public void testDefaultGridBagPositionAttributesForColumn() throws Exception {
    builder.add(aButton, aList);
    JPanel panel =
      (JPanel) parse(
        "<column defaultFill='both' defaultAnchor='north'>" +
        "  <component ref='aButton'/>" +
        "  <component ref='aList' fill='horizontal' anchor='east' />" +
        "</column>");

    assertEquals(2, panel.getComponentCount());
    checkGridPos(panel, aButton,
                 0, 0, 1, 1,
                 SwingStretches.NULL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.BOTH, Anchor.NORTH, new Insets(0, 0, 0, 0));
    checkGridPos(panel, aList,
                 0, 1, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.LARGE_WEIGHT,
                 Fill.HORIZONTAL, Anchor.EAST, new Insets(0, 0, 0, 0));
  }

  public void testDefaultGridBagMarginForGrid() throws Exception {
    builder.add(aButton, aList, aTable);
    JPanel panel =
      (JPanel) parse(
        "<grid defaultMargin='10'>" +
        "  <component ref='aButton' gridPos='(0,0)'/>" +
        "  <component ref='aList' gridPos='(0,1)' marginTop='5' marginBottom='20'/>" +
        "  <component ref='aTable' gridPos='(0,2)' marginLeft='2' marginRight='3'/>" +
        "</grid>");

    assertEquals(3, panel.getComponentCount());
    checkGridPos(panel, aButton,
                 0, 0, 1, 1,
                 SwingStretches.NULL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.NONE, Anchor.CENTER, new Insets(10, 10, 10, 10));
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
      (JPanel) parse(
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
    assertEquals(new Insets(10, 10, 10, 10), getConstraints(rowComponents[0], aButton).insets);
    assertEquals(new Insets(5, 10, 20, 10), getConstraints(rowComponents[1], aList).insets);
    assertEquals(new Insets(10, 2, 10, 3), getConstraints(rowComponents[2], aTable).insets);
  }

  public void testBorderLayout() throws Exception {
    builder.add(aTable, aList, aButton);

    JPanel panel =
      (JPanel) parse(
        "<borderLayout>" +
        "  <component ref='aButton' borderPos='north'/>" +
        "  <component ref='aTable' borderPos='center'/>" +
        "  <component ref='aList' borderPos='west'/>" +
        "</borderLayout>");

    BorderLayout layout = (BorderLayout) panel.getLayout();
    assertEquals("North", layout.getConstraints(aButton));
    assertEquals("Center", layout.getConstraints(aTable));
    assertEquals("West", layout.getConstraints(aList));
  }

  public void testBorderLayoutWithNoBorderPosError() throws Exception {
    builder.add(aTable, aList);
    checkParsingError("<borderLayout>" +
                      "  <component ref='aList' borderPos='center'/>" +
                      "  <component ref='aTable'/>" +
                      "</borderLayout>",
                      "Element 'component' in borderLayout must have a property 'borderPos' set to " +
                      "[center|north|south|east|west]");
  }

  public void testBorderLayoutWithUnknownBorderPosError() throws Exception {
    builder.add(aTable, aList);
    checkParsingError("<borderLayout>" +
                      "  <component ref='aList' borderPos='center'/>" +
                      "  <component ref='aTable' borderPos='__UNKNOWN__'/>" +
                      "</borderLayout>",
                      "Element 'component' in borderLayout must have a property 'borderPos' set to " +
                      "[center|north|south|east|west]");
  }

  public void testCreatingAComponentWithAName() throws Exception {
    JFrame frame = (JFrame) parse("<frame>" +
                                  "  <button text='Click!'/>" +
                                  "</frame>");
    JButton button = (JButton) frame.getContentPane().getComponent(0);
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
    catch (SplitsException e) {
      assertEquals("No component found for ref: anUndefinedId", e.getMessage());
    }
  }

  public void testAssignsNamesToCreatedComponents() throws Exception {
    checkAssignedName("<button name='btn'/>", "btn");
    checkAssignedName("<table name='aTable'/>", "aTable");
    checkAssignedName("<list name='aList'/>", "aList");
    checkAssignedName("<styledPanel name='styled'/>", "styled");
    checkAssignedName("<label name='lbl'/>", "lbl");
  }

  private void checkAssignedName(String xml, String name) throws Exception {
    assertEquals(name, parse(xml).getName());
  }

  public void testSizes() throws Exception {
    JFrame frame =
      (JFrame) parse("<frame size='(800,600)' minimumSize='(200, 150)' maximumSize='(1000, 900)'/>");
    assertEquals(new Dimension(800, 600), frame.getSize());
    assertEquals(new Dimension(200, 150), frame.getMinimumSize());
    assertEquals(new Dimension(1000, 900), frame.getMaximumSize());

    JButton button =
      (JButton) parse("<button text='Click!' size='(80,60)' minimumSize='(20, 15)' maximumSize='(100, 90)'/>");
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
      catch (SplitsException e) {
      }
    }
  }

  public void testCreatingAFrame() throws Exception {
    JFrame frame =
      (JFrame) parse(
        "<frame title='The Title'>" +
        "  <button text='Click'/>" +
        "</frame>");
    assertNotNull(frame);
    checkFrame(frame);
  }

  public void testReferencingAFrame() throws Exception {
    JFrame frame = new JFrame();
    builder.add("theFrame", frame);
    parse(
      "<frame ref='theFrame' title='The Title'>" +
      "  <button text='Click'/>" +
      "</frame>");
    checkFrame(frame);
  }

  private void checkFrame(JFrame frame) {
    assertEquals("The Title", frame.getTitle());
    JButton button = (JButton) frame.getContentPane().getComponent(0);
    assertNotNull(button);
    assertEquals("Click", button.getText());
  }

  public void testCreatingAList() throws Exception {
    JList list = (JList) parse("<list/>");
    assertNotNull(list);
  }

  public void testReferencingAList() throws Exception {
    builder.add(aList);
    assertSame(aList, parse("<list ref='aList'/>"));
  }

  public void testCreatingAButtonWithAnAction() throws Exception {
    DummyAction action = new DummyAction();
    builder.add("action1", action);
    JButton btn = (JButton) parse("<button text='blah' action='action1'/>");
    assertEquals("blah", btn.getText());
    new org.uispec4j.Button(btn).click();
    assertTrue(action.wasClicked());
  }

  public void testCreatingAToggleButtonWithAnAction() throws Exception {
    DummyAction action = new DummyAction();
    builder.add("action1", action);
    JToggleButton btn = (JToggleButton) parse("<toggleButton text='blah' action='action1'/>");
    assertEquals("blah", btn.getText());
    new org.uispec4j.ToggleButton(btn).click();
    assertTrue(action.wasClicked());
  }

  public void testPropertiesAreSetEvenWhenTheComponentIsWrappedInAMarginPanel() throws Exception {
    builder.add("button", aButton);
    parse("<button ref='button' text='blah' margin='5'/>");
    assertEquals("blah", aButton.getText());
  }

  public void testButtonUsesTheActionLabelIfNoneWasGiven() throws Exception {
    DummyAction action = new DummyAction();
    builder.add("action1", action);
    JButton btn = (JButton) parse("<button action='action1'/>");
    assertEquals("dummyAction", btn.getText());
  }

  public void testButtonIcons() throws Exception {
    JButton btn =
      (JButton) parse("<button icon='icon1' pressedIcon='icon2' rolloverIcon='icon3'/>");
    assertSame(DummyIconLocator.ICON1, btn.getIcon());
    assertSame(DummyIconLocator.ICON2, btn.getPressedIcon());
    assertSame(DummyIconLocator.ICON3, btn.getRolloverIcon());
    assertTrue(btn.isRolloverEnabled());
  }

  public void testCreatingATextField() throws Exception {
    DummyAction action = new DummyAction();
    builder.add("action1", action);
    JTextField textField = (JTextField) parse("<textField text='blah' action='action1'/>");
    assertEquals("blah", textField.getText());
    new org.uispec4j.TextBox(textField).setText("newText");
    assertTrue(action.wasClicked());
  }

  public void testCreatingATextArea() throws Exception {
    JTextArea textArea = (JTextArea) parse("<textArea text='blah'/>");
    assertEquals("blah", textArea.getText());
  }

  public void testCreatingAnEditorPane() throws Exception {
    JEditorPane editorPane = (JEditorPane) parse("<editorPane text='blah'/>");
    assertEquals("blah", editorPane.getText());
  }

  public void testCreatingAHtmlEditorPane() throws Exception {
    textLocator.set("editor.text", "<html><b>Hello</b> world!</html>");
    JEditorPane editorPane = (JEditorPane) parse("<htmlEditorPane text='$editor.text'/>");
    assertEquals("text/html", editorPane.getContentType());
    assertEquals("<html>\n" +
                 "  <head>\n" +
                 "    \n" +
                 "  </head>\n" +
                 "  <body>\n" +
                 "    <b>Hello</b> world!\n" +
                 "  </body>\n" +
                 "</html>\n",
                 editorPane.getText());
  }

  public void testCreatingAComboBox() throws Exception {
    JComboBox combo = (JComboBox) parse("<comboBox/>");
    assertNotNull(combo);
  }

  public void testReferencingAComboBox() throws Exception {
    JComboBox combo = new JComboBox();
    builder.add("combo", combo);
    assertSame(combo, parse("<comboBox ref='combo'/>"));
  }

  public void testCreatingACheckBox() throws Exception {
    JCheckBox check = (JCheckBox) parse("<checkBox/>");
    assertNotNull(check);
  }

  public void testReferencingACheckBox() throws Exception {
    JCheckBox check = new JCheckBox();
    builder.add("check", check);
    assertSame(check, parse("<checkBox ref='check'/>"));
  }

  public void testFixedForeground() throws Exception {
    JButton button = (JButton) parse("<button foreground='FF0000'/>");
    assertEquals(Color.RED, button.getForeground());
  }

  public void testVariableForeground() throws Exception {
    colorService.set("fgkey", Color.RED);
    JButton btn = (JButton) parse("<button foreground='fgkey'/>");
    assertEquals(Color.RED, btn.getForeground());
    colorService.set("fgkey", Color.BLUE);
    assertEquals(Color.BLUE, btn.getForeground());
  }

  public void testFixedBackground() throws Exception {
    JButton button = (JButton) parse("<button background='#FF0000'/>");
    assertEquals(Color.RED, button.getBackground());
  }

  public void testVariableBackground() throws Exception {
    colorService.set("bgkey", Color.RED);
    JButton btn = (JButton) parse("<button background='bgkey'/>");
    assertEquals(Color.RED, btn.getBackground());
    colorService.set("bgkey", Color.BLUE);
    assertEquals(Color.BLUE, btn.getBackground());
  }

  public void testLabelsAreNotOpaqueButThisCanBeOverriden() throws Exception {
    assertFalse(parse("<label text='foo'/>").isOpaque());
    assertTrue(parse("<label text='foo' opaque='true'/>").isOpaque());
  }

  public void testPanel() throws Exception {
    builder.add("btn", aButton);
    MyPanel myPanel = new MyPanel();
    builder.add("myPanel", myPanel);
    MyPanel panel = (MyPanel) parse(
      "<panel ref='myPanel'>" +
      "  <button ref='btn'/>" +
      "</panel>");
    assertSame(aButton, panel.getComponent(0));
  }

  public void testAPanelCanHaveOnlyOneChild() throws Exception {
    MyPanel myPanel = new MyPanel();
    builder.add("myPanel", myPanel);
    try {
      MyPanel panel = (MyPanel) parse(
        "<panel ref='myPanel'>" +
        "  <button/>" +
        "  <button/>" +
        "</panel>");
    }
    catch (Exception e) {
      assertEquals("panel components cannot have more than one subcomponent", e.getMessage());
    }
  }

  private static class MyPanel extends JPanel {
  }

  public void testStyledPanel() throws Exception {
    builder.add("btn", aButton);
    JStyledPanel panel = (JStyledPanel) parse(
      "<styledPanel topColor='top' bottomColor='bottom' " +
      "             borderWidth='2' borderColor='border'" +
      "             cornerRadius='12'>" +
      "  <button ref='btn'/>" +
      "</styledPanel>");

    colorService.set("top", Color.YELLOW);
    colorService.set("bottom", Color.CYAN);
    colorService.set("border", Color.GREEN);

    assertEquals(Color.YELLOW, panel.getTopColor());
    assertEquals(Color.CYAN, panel.getBottomColor());
    assertEquals(Color.GREEN, panel.getBorderColor());

    assertEquals(2, panel.getBorderWidth());
    assertEquals(12, panel.getCornerRadius());

    assertSame(aButton, panel.getComponent(0));
  }
  public void testScrollPane() throws Exception {
    builder.add(aTable);
    JScrollPane scrollPane = (JScrollPane) parse(
      "<scrollPane>" +
      "  <table ref='aTable'/>" +
      "</scrollPane>");
    assertSame(scrollPane, aTable.getParent().getParent());
  }

  public void testScrollPaneAcceptsOnlyOneSubcomponent() throws Exception {
    try {
      parseWithoutSchemaValidation(
        "<scrollPane>" +
        "  <table/>" +
        "  <button/>" +
        "</scrollPane>");
      fail();
    }
    catch (Exception e) {
      assertEquals("scrollPane must have exactly one subcomponent", e.getMessage());
    }
  }

  public void testScrollPaneMustHaveAtLeastOneSubcomponent() throws Exception {
    try {
      parseWithoutSchemaValidation(
        "<scrollPane/>");
      fail();
    }
    catch (Exception e) {
      assertEquals("scrollPane must have exactly one subcomponent", e.getMessage());
    }
  }

  public void testScrollPaneBackgroundColor() throws Exception {
    JScrollPane scrollPane = (JScrollPane) parse(
      "<scrollPane viewportBackground='#FF0000'>" +
      "  <button/>" +
      "</scrollPane>");
    assertEquals(Color.RED, scrollPane.getViewport().getBackground());
  }

  public void testScrollPaneViewportOpacity() throws Exception {
    JScrollPane scrollPane = (JScrollPane) parse(
      "<scrollPane viewportOpaque='false'>" +
      "  <button/>" +
      "</scrollPane>");
    assertFalse(scrollPane.getViewport().isOpaque());
  }

  public void testTabs() throws Exception {
    JTabbedPane tabs = (JTabbedPane) parse(
      "<tabs>" +
      "  <tab title='Tab 1'>" +
      "    <label text='Blah'/>" +
      "  </tab>" +
      "  <tab title='Tab 2'>" +
      "    <button text='OK'/>" +
      "  </tab>" +
      "</tabs>"
    );
    assertEquals(2, tabs.getTabCount());
    assertEquals("Tab 1", tabs.getTitleAt(0));
    JLabel label = (JLabel) tabs.getComponentAt(0);
    assertEquals("Blah", label.getText());

    assertEquals("Tab 2", tabs.getTitleAt(1));
    JButton button = (JButton) tabs.getComponentAt(1);
    assertEquals("OK", button.getText());
  }

  public void testTabGroupCanOnlyHaveTabElements() throws Exception {
    try {
      parseWithoutSchemaValidation(
        "<tabs>" +
        " <grid/>" +
        "</tabs>");
      fail();
    }
    catch (SplitsException e) {
      assertEquals("Invalid component 'grid' found in 'tabs' component -  only 'tab' subcomponents are accepted",
                   e.getMessage());
    }
  }

  public void testTabMustHaveExactlyOneComponent() throws Exception {
    try {
      parseWithoutSchemaValidation(
        "<tabs>" +
        "  <tab title='Tab 1'>" +
        "    <column/>" +
        "    <row/>" +
        "  </tab>" +
        "</tabs>");
      fail();
    }
    catch (SplitsException e) {
      assertEquals("Tab component 'Tab 1' must have exactly one subcomponent", e.getMessage());
    }

    try {
      parseWithoutSchemaValidation(
        "<tabs>" +
        "  <tab title='Tab 1'/>" +
        "</tabs>");
      fail();
    }
    catch (SplitsException e) {
      assertEquals("Tab component 'Tab 1' must have exactly one subcomponent", e.getMessage());
    }
  }

  public void testFiller() throws Exception {
    checkFiller("<column>" +
                "  <filler/>" +
                "</column>",
                SwingStretches.LARGE_WEIGHT, SwingStretches.LARGE_WEIGHT,
                Short.MAX_VALUE, Short.MAX_VALUE);

    checkFiller("<column>" +
                "  <filler fill='both'/>" +
                "</column>",
                SwingStretches.LARGE_WEIGHT, SwingStretches.LARGE_WEIGHT,
                Short.MAX_VALUE, Short.MAX_VALUE);

    checkFiller("<column>" +
                "  <filler fill='vertical'/>" +
                "</column>",
                SwingStretches.NULL_WEIGHT, SwingStretches.LARGE_WEIGHT,
                0, Short.MAX_VALUE);

    checkFiller("<column>" +
                "  <filler fill='horizontal'/>" +
                "</column>",
                SwingStretches.LARGE_WEIGHT, SwingStretches.NULL_WEIGHT,
                Short.MAX_VALUE, 0);
  }

  public void testFillerWithUnknownDirection() throws Exception {
    try {
      parseWithoutSchemaValidation("<filler fill='unknown'/>");
      fail();
    }
    catch (Exception e) {
      assertEquals("Unknown direction for filler: unknown", e.getMessage());
    }
  }

  public void testCanUseConstantsForIntegers() throws Exception {
    JLabel label = (JLabel) parse("<label horizontalAlignment='JLabel.RIGHT'/>");
    assertEquals(JLabel.RIGHT, label.getHorizontalAlignment());
  }

  public void testEtchedBorder() throws Exception {
    checkBorder("etched", EtchedBorder.class);
  }

  public void testBevelBorder() throws Exception {
    BevelBorder lowered = checkBorder("bevel(lowered)", BevelBorder.class);
    assertEquals(BevelBorder.LOWERED, lowered.getBevelType());

    BevelBorder raised = checkBorder("bevel(raised)", BevelBorder.class);
    assertEquals(BevelBorder.RAISED, raised.getBevelType());
  }

  public void testFont() throws Exception {
    JLabel label = (JLabel) parse("<label font='Arial,italic,24'/>");
    SplitsUtilsTest.checkFont(label.getFont(), "Arial", Font.ITALIC, 24);
  }

  public void testCardLayout() throws Exception {
    builder.add(aButton, aList, aTable);
    CardHandler handler = builder.addCardHandler("myHandler");

    JPanel panel = (JPanel) parse("<cards ref='myHandler'>" +
                                  "  <card name='a'>" +
                                  "    <button ref='aButton'/>" +
                                  "  </card>" +
                                  "  <card name='b'>" +
                                  "    <table ref='aTable'/>" +
                                  "  </card>" +
                                  "  <card name='c'>" +
                                  "    <list ref='aList'/>" +
                                  "  </card>" +
                                  "</cards>");

    assertTrue(panel.getLayout() instanceof CardLayout);

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

  public void testCardLayoutMustReferenceAPanelWithACardLayout() throws Exception {
    try {
      builder.add("myPanel", new JPanel());
      parse(
        "<cards ref='myPanel'>" +
        "  <card name='a'>" +
        "    <button/>" +
        "  </card>" +
        "</cards>");
      fail();
    }
    catch (Exception e) {
      assertEquals("Panel 'myPanel' must use a CardLayout, preferably through a CardHandler", e.getMessage());
    }
  }

  public void testCardLayoutMustReferenceARegisteredPanel() throws Exception {
    try {
      parseWithoutSchemaValidation(
        "<cards>" +
        "  <card name='a'>" +
        "    <button/>" +
        "  </card>" +
        "</cards>");
      fail();
    }
    catch (Exception e) {
      assertEquals("cards components must reference a registered panel (use ref='xxx')", e.getMessage());
    }
  }

  public void testCardsComponentsMustHaveNames() throws Exception {
    try {
      builder.add("panel", new JPanel());
      parseWithoutSchemaValidation(
        "<cards ref='panel'>" +
        "  <card>" +
        "    <button/>" +
        "  </card>" +
        "</cards>");
      fail();
    }
    catch (Exception e) {
      assertEquals("Card items must have a 'name' attribute", e.getMessage());
    }
  }

  public void testTextLocator() throws Exception {
    JLabel label = (JLabel) parse("<label text='$aa.bb.cc'/>");
    assertEquals("aa bb cc", label.getText());
  }

  private <T extends Border> T checkBorder(String desc, Class<T> c) throws Exception {
    JLabel label = (JLabel) parse("<label border='" + desc + "'/>");
    Border border = label.getBorder();
    assertNotNull(border);
    assertTrue(c.isInstance(border));
    return (T) border;
  }

  private void checkFiller(String xml, double weightx, double weighty, int maxX, int maxY) throws Exception {
    JPanel panel = (JPanel) parse(xml);

    Box.Filler filler = (Box.Filler) panel.getComponent(0);
    assertNotNull(filler);

    GridBagConstraints constraints = getConstraints(panel, filler);
    assertEquals(weightx, constraints.weightx);
    assertEquals(weighty, constraints.weighty);

    assertEquals(new Dimension(maxX, maxY), filler.getMaximumSize());
  }

  private void checkGridPos(JPanel panel, JComponent component,
                            int x, int y, int w, int h,
                            double weightX, double weightY,
                            Fill fill, Anchor anchor, Insets insets) {
    GridBagConstraints constraints = getConstraints(panel, component);
    assertEquals(x, constraints.gridx);
    assertEquals(y, constraints.gridy);
    assertEquals(w, constraints.gridwidth);
    assertEquals(h, constraints.gridheight);
    assertEquals(weightX, constraints.weightx);
    assertEquals(weightY, constraints.weighty);
    assertEquals(fill.getValue(), constraints.fill);
    assertEquals(anchor.getValue(), constraints.anchor);
    assertEquals(insets, constraints.insets);
  }

  private GridBagConstraints getConstraints(Component parent, JComponent component) {
    JPanel panel = (JPanel) parent;
    GridBagLayout layout = (GridBagLayout) panel.getLayout();
    return layout.getConstraints(component);
  }

  private Component parse(String xml) throws Exception {
    validateDocument(toStream(xml));
    return builder.parse(toStream(xml));
  }

  private Component parseWithoutSchemaValidation(String xml) throws Exception {
    return builder.parse(toStream(xml));
  }

  private StringReader toStream(String xml) {
    return new StringReader("<splits>" + xml + "</splits>");
  }

  private void validateDocument(Reader reader) throws Exception {
    String schemaLang = "http://www.w3.org/2001/XMLSchema";
    SchemaFactory factory = SchemaFactory.newInstance(schemaLang);
    Schema schema = factory.newSchema(new StreamSource(getClass().getResourceAsStream("/splits.xsd")));
    Validator validator = schema.newValidator();

    // at last perform validation:
    validator.validate(new StreamSource(reader));
  }

  private void checkParsingError(String xml, String error) throws Exception {
    try {
      parse(xml);
      fail();
    }
    catch (SplitsException e) {
      assertEquals(error, e.getMessage());
    }
  }

  public static void main(String[] args) {
    JEditorPane editorPane = new JEditorPane();
    editorPane.setContentType("text/html");
    editorPane.setText("<html><p>Hello <br> <b>world</b> !</p></html>");
    GuiUtils.show(editorPane);
  }
}
