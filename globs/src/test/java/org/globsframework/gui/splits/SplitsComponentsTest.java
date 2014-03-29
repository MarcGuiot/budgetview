package org.globsframework.gui.splits;

import org.globsframework.gui.splits.components.ShadowedLabelUI;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.font.Fonts;
import org.globsframework.gui.splits.font.FontsTest;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.layout.SwingStretches;
import org.globsframework.gui.splits.layout.TabHandler;
import org.globsframework.gui.splits.utils.DummyAction;
import org.globsframework.gui.splits.utils.DummyImageLocator;
import org.globsframework.gui.splits.utils.DummyLayoutManager;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.uispec4j.TextBox;
import org.uispec4j.finder.ComponentFinder;
import org.uispec4j.finder.ComponentMatchers;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;

public class SplitsComponentsTest extends SplitsTestCase {
  public void testBorderLayout() throws Exception {
    builder.add(aTable, aList, aButton);

    JPanel panel =
      parse(
        "<borderLayout>" +
        "  <component ref='aButton' borderPos='north'/>" +
        "  <component ref='aTable' borderPos='center'/>" +
        "  <component ref='aList' borderPos='west'/>" +
        "</borderLayout>");

    BorderLayout layout = (BorderLayout)panel.getLayout();
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

  public void testCreatingAFrame() throws Exception {
    JFrame frame =
      parse(
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
    JButton button = (JButton)frame.getContentPane().getComponent(0);
    assertNotNull(button);
    assertEquals("Click", button.getText());
  }

  public void testCreatingAList() throws Exception {
    JList list = parse("<list/>");
    assertNotNull(list);
  }

  public void testReferencingAList() throws Exception {
    builder.add(aList);
    assertSame(aList, parse("<list ref='aList'/>"));
  }

  public void testCreatingAButtonWithAnAction() throws Exception {
    DummyAction action = new DummyAction();
    builder.add("action1", action);
    JButton btn = parse("<button text='blah' action='action1'/>");
    assertEquals("blah", btn.getText());
    new org.uispec4j.Button(btn).click();
    assertTrue(action.wasClicked());
  }

  public void testTheActionNameIsSetOnTheButton() throws Exception {
    builder.add("action1", new DummyAction());
    JButton btn = parse("<button text='blah' action='action1'/>");
    assertEquals("action1", btn.getName());
  }

  public void testTheActionNameDoesNotOverrideTheButtonName() throws Exception {
    builder.add("action1", new DummyAction());
    JButton btn = parse("<button text='blah' action='action1' name='myname'/>");
    assertEquals("myname", btn.getName());
  }

  public void testCreatingAToggleButtonWithAnAction() throws Exception {
    DummyAction action = new DummyAction();
    builder.add("action1", action);
    JToggleButton btn = parse("<toggleButton text='blah' action='action1'/>");
    assertEquals("blah", btn.getText());
    new org.uispec4j.ToggleButton(btn).click();
    assertTrue(action.wasClicked());
  }

  public void testButtonUsesTheActionLabelIfNoneWasGiven() throws Exception {
    DummyAction action = new DummyAction();
    builder.add("action1", action);
    JButton btn = parse("<button action='action1'/>");
    assertEquals("dummyAction", btn.getText());
  }

  public void testButtonIcons() throws Exception {
    JButton btn =
      parse("<button icon='icon1' pressedIcon='icon2' rolloverIcon='icon3'/>");
    assertSame(DummyImageLocator.ICON1, btn.getIcon());
    assertSame(DummyImageLocator.ICON2, btn.getPressedIcon());
    assertSame(DummyImageLocator.ICON3, btn.getRolloverIcon());
    assertTrue(btn.isRolloverEnabled());
  }

  public void testCreatingATextField() throws Exception {
    DummyAction action = new DummyAction();
    builder.add("action1", action);
    JTextField textField = parse("<textField text='blah' action='action1'/>");
    assertEquals("blah", textField.getText());
    new org.uispec4j.TextBox(textField).setText("newText");
    assertTrue(action.wasClicked());
  }

  public void testCreatingATextArea() throws Exception {
    JTextArea textArea = parse("<textArea text='blah'/>");
    assertEquals("blah", textArea.getText());
  }

  public void testCreatingAnEditorPane() throws Exception {
    JEditorPane editorPane = parse("<editorPane text='blah'/>");
    assertEquals("blah", editorPane.getText());
  }

  public void testCreatingAHtmlEditorPane() throws Exception {
    textLocator.set("editor.text", "<html><b>Hello</b> world!</html>");
    JEditorPane editorPane = parse("<htmlEditorPane text='$editor.text'/>");
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

  public void testHtmlHyperlinkListener() throws Exception {

    final StringBuilder log = new StringBuilder();
    builder.add("listener", new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        log.append(e.getDescription());
      }
    });

    textLocator.set("editor.text", "<html><a href='link'>Click me</a> please</html>");
    JEditorPane editorPane = parse("<htmlEditorPane text='$editor.text' hyperlinkListener='listener'/>");
    assertEquals("text/html", editorPane.getContentType());
    TextBox textBox = new TextBox(editorPane);
    textBox.clickOnHyperlink("Click me");

    assertEquals("link", log.toString());
  }

  public void testUsingAnImageLocatorInHtmlComponents() throws Exception {
    textLocator.set("editor.text", "<html><img src='anImage'/></html>");
    parse("<htmlEditorPane text='$editor.text' useImageLocator='true'/>");
    assertEquals("anImage", iconLocator.lastRequestedImageName);
  }

  public void testSettingTheImageLocatorOnAnHtmlEditorDoesNoClearTheText() throws Exception {
    JEditorPane editor = new JEditorPane();
    GuiUtils.initReadOnlyHtmlComponent(editor);
    final String text = "<html><img src='anImage'></html>";
    editor.setText(text);
    builder.add("editor", editor);

    TextBox textBox = new TextBox((JEditorPane)parse("<htmlEditorPane ref='editor' useImageLocator='true'/>"));

    assertThat(textBox.htmlEquals("<html>\n" +
                                  "  <head>\n" +
                                  "  </head>\n" +
                                  "  <body>\n" +
                                  "    <img src='anImage'>\n" +
                                  "  </body>\n" +
                                  "</html>"));
    assertEquals("anImage", iconLocator.lastRequestedImageName);
  }

  public void testCreatingAComboBox() throws Exception {
    JComboBox combo = parse("<comboBox/>");
    assertNotNull(combo);
  }

  public void testReferencingAComboBox() throws Exception {
    JComboBox combo = new JComboBox();
    builder.add("combo", combo);
    assertSame(combo, parse("<comboBox ref='combo'/>"));
  }

  public void testCreatingACheckBox() throws Exception {
    JCheckBox check = parse("<checkBox/>");
    assertNotNull(check);
  }

  public void testCreatingARadioButton() throws Exception {
    JRadioButton button = parse("<radioButton/>");
    assertNotNull(button);
  }

  public void testCreatingAProgressBar() throws Exception {
    JProgressBar progressBar = parse("<progressBar/>");
    assertNotNull(progressBar);
  }

  public void testReferencingACheckBox() throws Exception {
    JCheckBox check = new JCheckBox();
    builder.add("check", check);
    assertSame(check, parse("<checkBox ref='check'/>"));
  }

  public void testLabelFor() throws Exception {
    JLabel label = builder.add("label", new JLabel()).getComponent();
    JTextField textField = builder.add("editor", new JTextField()).getComponent();
    JPanel jPanel = parse("<row>" +
                          "  <label ref='label' text='Title' labelFor='editor'/>" +
                          "  <textField ref='editor'/>" +
                          "</row>");
    assertSame(textField, label.getLabelFor());

    org.uispec4j.Panel panel = new org.uispec4j.Panel(jPanel);
    TextBox editor = panel.getTextBox(ComponentMatchers.componentLabelFor("Title"));
    assertSame(textField, editor.getAwtComponent());
  }

  public void testLabelForError() throws Exception {
    try {
      parse("<label text='Title' labelFor='???'/>");
      fail();
    }
    catch (SplitsException e) {
      checkExceptionCause(e, "Label 'Title' references an unknown component '???'");
    }
  }

  public void testLabelsAreNotOpaqueButThisCanBeOverriden() throws Exception {
    JLabel label1 = parse("<label text='foo'/>");
    assertFalse(label1.isOpaque());

    JLabel label2 = this.parse("<label text='foo' opaque='true'/>");
    assertTrue(label2.isOpaque());
  }

  public void testPanel() throws Exception {
    builder.add("btn", aButton);
    builder.add("myPanel", new MyPanel());
    MyPanel panel = parse(
      "<panel ref='myPanel'>" +
      "  <button ref='btn'/>" +
      "</panel>");
    assertSame(aButton, panel.getComponent(0));
  }

  public void testAPanelCanHaveOnlyOneChild() throws Exception {
    builder.add("myPanel", new MyPanel());
    try {
      parse(
        "<panel ref='myPanel'>" +
        "  <button/>" +
        "  <button/>" +
        "</panel>");
      fail();
    }
    catch (Exception e) {
      checkExceptionCause(e, "panel components cannot have more than one subcomponent");
    }
  }

  public void testAPanelWithACustomLayoutCanHaveSeveralChildren() throws Exception {
    builder.add("myPanel", new JPanel());
    JPanel panel = parse(
      "<panel ref='myPanel' layout='" + DummyLayoutManager.class.getName() + "'>" +
      "  <button/>" +
      "  <button/>" +
      "</panel>");
    assertEquals(2, panel.getComponentCount());
  }

  private static class MyPanel extends JPanel {
  }

  public void testScrollPane() throws Exception {
    builder.add(aTable);
    JScrollPane scrollPane = parse(
      "<scrollPane verticalUnitIncrement='12' horizontalUnitIncrement='15'" +
      "            verticalScrollbarPolicy='always' horizontalScrollbarPolicy='asNeeded'>" +
      "  <table ref='aTable'/>" +
      "</scrollPane>");
    assertSame(scrollPane, aTable.getParent().getParent());
    assertEquals(12, scrollPane.getVerticalScrollBar().getUnitIncrement());
    assertEquals(15, scrollPane.getHorizontalScrollBar().getUnitIncrement());
    assertEquals(0, scrollPane.getVerticalScrollBar().getValue());

    assertEquals(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, scrollPane.getVerticalScrollBarPolicy());
    assertEquals(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED, scrollPane.getHorizontalScrollBarPolicy());
  }

  public void testCanUseRefsForScrollPanes() throws Exception {
    JScrollPane ref = new JScrollPane();
    builder.add("scroll", ref);

    JScrollPane parsed = parse(
      "<scrollPane ref='scroll'>" +
      "  <button/>" +
      "</scrollPane>");

    assertSame(parsed, ref);
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
      checkExceptionCause(e, "scrollPane must have exactly one subcomponent");
    }
  }

  public void testScrollPaneMustHaveAtLeastOneSubcomponent() throws Exception {
    try {
      parseWithoutSchemaValidation(
        "<scrollPane/>");
      fail();
    }
    catch (Exception e) {
      checkExceptionCause(e, "scrollPane must have exactly one subcomponent");
    }
  }

  public void testScrollPaneBackgroundColor() throws Exception {
    JScrollPane scrollPane = parse(
      "<scrollPane viewportBackground='#00FF00'>" +
      "  <button/>" +
      "</scrollPane>");
    assertEquals(Color.GREEN, scrollPane.getViewport().getBackground());
  }

  public void testScrollPaneBackgroundColorWithNamedColor() throws Exception {
    colorService.set("bgcolor", Color.PINK);
    JScrollPane scrollPane = parse(
      "<scrollPane viewportBackground='bgcolor'>" +
      "  <button/>" +
      "</scrollPane>");
    assertEquals(Color.PINK, scrollPane.getViewport().getBackground());
  }

  public void testScrollPaneViewportOpacity() throws Exception {
    JScrollPane scrollPane = parse(
      "<scrollPane viewportOpaque='false'>" +
      "  <button/>" +
      "</scrollPane>");
    assertFalse(scrollPane.getViewport().isOpaque());
  }

  public void testScrollPaneWithForcedVerticalScroll() throws Exception {
    JScrollPane scrollPane = parse(
      "<scrollPane forceVerticalScroll='true'>" +
      "  <button/>" +
      "</scrollPane>");

    JPanel panel = (JPanel)scrollPane.getViewport().getView();
    assertFalse(panel.isOpaque());

    assertTrue(panel instanceof Scrollable);
    Scrollable scrollable = (Scrollable)panel;
    assertTrue(scrollable.getScrollableTracksViewportWidth());
    assertFalse(scrollable.getScrollableTracksViewportHeight());
  }

  public void testScrollPaneWithForcedVerticalScrollErrors() throws Exception {
    checkParsingError(
      "<scrollPane forceVerticalScroll='true' horizontalScrollbarPolicy='always'>" +
      "  <button/>" +
      "</scrollPane>",
      "horizontalScrollbarPolicy cannot be set when forceVerticalScroll is set to true");

    checkParsingError(
      "<scrollPane forceVerticalScroll='true' horizontalUnitIncrement='10'>" +
      "  <button/>" +
      "</scrollPane>",
      "horizontalUnitIncrement cannot be set when forceVerticalScroll is set to true");

  }

  public void testTabs() throws Exception {
    JTabbedPane tabs = parse(
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
    JLabel label = (JLabel)tabs.getComponentAt(0);
    assertEquals("Blah", label.getText());

    assertEquals("Tab 2", tabs.getTitleAt(1));
    JButton button = (JButton)tabs.getComponentAt(1);
    assertEquals("OK", button.getText());
  }

  public void testReferencingATab() throws Exception {
    JTabbedPane tabs = new JTabbedPane();
    builder.add("tabs", tabs);
    assertSame(tabs, parse(
      "<tabs ref='tabs'>" +
      "  <tab title='Tab 1'>" +
      "    <label text='Blah'/>" +
      "  </tab>" +
      "</tabs>"));
  }

  public void testTabGroupCanOnlyHaveTabElements() throws Exception {
    try {
      parseWithoutSchemaValidation(
        "<tabs>" +
        " <grid/>" +
        "</tabs>");
      fail();
    }
    catch (Exception e) {
      checkExceptionCause(e, "Invalid component 'grid' found in 'tabs' component -  " +
                             "only 'tab' subcomponents are accepted");
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
    catch (Exception e) {
      checkExceptionCause(e, "Tab component 'Tab 1' must have exactly one subcomponent");
    }

    try {
      parseWithoutSchemaValidation(
        "<tabs>" +
        "  <tab title='Tab 1'/>" +
        "</tabs>");
      fail();
    }
    catch (Exception e) {
      checkExceptionCause(e, "Tab component 'Tab 1' must have exactly one subcomponent");
    }
  }

  public void testCanUseTextLocatorInTabNames() throws Exception {

    textLocator.set("key1", "Tab 1");
    textLocator.set("key2", "Tab 2");

    JTabbedPane tabs = parse(
      "<tabs>" +
      "  <tab title='$key1'>" +
      "    <label text='Blah'/>" +
      "  </tab>" +
      "  <tab title='$key2'>" +
      "    <button text='OK'/>" +
      "  </tab>" +
      "</tabs>"
    );

    assertEquals(2, tabs.getTabCount());
    assertEquals("Tab 1", tabs.getTitleAt(0));
    assertEquals("Tab 2", tabs.getTitleAt(1));
  }

  public void testTabHandler() throws Exception {
    TabHandler tabHandler = builder.addTabHandler("tabHandler");

    JTabbedPane tabs = parse(
      "<tabs ref='tabHandler'>" +
      "  <tab title='Tab1'>" +
      "    <label text='Blah'/>" +
      "  </tab>" +
      "  <tab title='Tab2'>" +
      "    <button text='OK'/>" +
      "  </tab>" +
      "  <tab title='Tab3'>" +
      "    <button text='OK'/>" +
      "  </tab>" +
      "</tabs>"
    );

    tabHandler.select(1);
    assertEquals(1, tabs.getSelectedIndex());

    tabHandler.select(0);
    assertEquals(0, tabs.getSelectedIndex());

    try {
      tabHandler.select(3);
    }
    catch (InvalidParameter e) {
      assertEquals("Invalid index 3 - should be between 0 and 2", e.getMessage());
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

  private void checkFiller(String xml, double weightx, double weighty, int maxX, int maxY) throws Exception {
    JPanel panel = parse(xml);

    Box.Filler filler = (Box.Filler)panel.getComponent(0);
    assertNotNull(filler);

    GridBagConstraints constraints = getConstraints(panel, filler);
    assertEquals(weightx, constraints.weightx);
    assertEquals(weighty, constraints.weighty);

    assertEquals(new Dimension(maxX, maxY), filler.getMaximumSize());
  }

  public void testFillerWithUnknownDirection() throws Exception {
    try {
      parseWithoutSchemaValidation("<filler fill='unknown'/>");
      fail();
    }
    catch (Exception e) {
      checkExceptionCause(e, "Unknown direction for filler: unknown");
    }
  }

  public void testCanUseConstantsForIntegers() throws Exception {
    JLabel label = parse("<label horizontalAlignment='right'/>");
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

  public void testResettingTheBorderWithAnEmptyString() throws Exception {
    aList.setBorder(BorderFactory.createEtchedBorder());
    builder.add("list", aList);
    parse("<list ref='list' border=''/>");
    assertNull(aList.getBorder());
  }

  private <T extends Border> T checkBorder(String desc, Class<T> c) throws Exception {
    JLabel label = parse("<label border='" + desc + "'/>");
    Border border = label.getBorder();
    assertNotNull(border);
    assertTrue(c.isInstance(border));
    return (T)border;
  }

  public void testHardcodedFont() throws Exception {
    JLabel label = parse("<label font='Arial,italic,24'/>");
    FontsTest.checkFont(label.getFont(), "Arial", Font.ITALIC, 24);
  }

  public void testFontService() throws Exception {
    Font font = Fonts.parseFont("Arial,italic,12");
    fontService.set("font1", font);
    JLabel label = parse("<label font='$font1'/>");
    assertEquals(font, label.getFont());
  }

  public void testCardLayout() throws Exception {
    builder.add(aButton, aList, aTable);
    CardHandler handler = builder.addCardHandler("myHandler");

    JPanel panel = parse("<cards ref='myHandler'>" +
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

    ComponentFinder finder = new ComponentFinder(panel, new org.uispec4j.Panel(panel));

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

    assertTrue(panel.isVisible());

    handler.setVisible(false);
    assertFalse(panel.isVisible());

    handler.setVisible(true);
    assertTrue(panel.isVisible());
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
      checkExceptionCause(e, "Panel 'myPanel' must use a CardLayout, " +
                             "preferably through a CardHandler");
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
      checkExceptionCause(e, "cards components must reference a registered panel (use ref='xxx')");
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
      checkExceptionCause(e, "Card items must have a 'name' attribute");
    }
  }

  public void testCardContainedComponentsHaveTheNameOfTheCard() throws Exception {
    CardHandler handler = builder.addCardHandler("myHandler");
    JPanel jPanel = parse("<cards ref='myHandler'>" +
                          "  <card name='a'>" +
                          "    <button/>" +
                          "  </card>" +
                          "  <card name='b'>" +
                          "    <label name='label'/>" +
                          "  </card>" +
                          "</cards>");

    handler.show("a");
    JButton button = (JButton)jPanel.getComponent(0);
    assertEquals("a", button.getName());

    handler.show("b");
    JLabel label = (JLabel)jPanel.getComponent(1);
    assertEquals("label", label.getName());
  }

  public void testShadowedLabel() throws Exception {
    colorService.set("theColor", Color.PINK);
    JLabel label = parse("<label shadowDirection='northeast' shadowColor='theColor'/>");
    ShadowedLabelUI ui = (ShadowedLabelUI)label.getUI();
    assertEquals(Color.PINK, ui.getShadowColor());
    assertEquals(ShadowedLabelUI.Direction.NORTHEAST, ui.getDirection());
  }

  public void testShadowedLabelDefaultColor() throws Exception {
    JLabel label = parse("<label shadowDirection='northeast'/>");
    ShadowedLabelUI ui = (ShadowedLabelUI)label.getUI();
    assertEquals(Color.BLACK, ui.getShadowColor());
    assertEquals(ShadowedLabelUI.Direction.NORTHEAST, ui.getDirection());
  }

  public void testShadowedLabelDefaultDirection() throws Exception {
    JLabel label = parse("<label shadowColor='#FF0000'/>");
    ShadowedLabelUI ui = (ShadowedLabelUI)label.getUI();
    assertEquals(Color.RED, ui.getShadowColor());
    assertEquals(ShadowedLabelUI.Direction.SOUTHEAST, ui.getDirection());
  }

  public void testShadowedLabelDirectionError() throws Exception {
    try {
      parse("<label shadowDirection='unknown'/>");
      fail();
    }
    catch (SplitsException e) {
      checkExceptionCause(e, "unknown value 'unknown'");
    }
  }

  public void testAutoHideIfDisabled() throws Exception {
    JButton button = parse("<button autoHideIfDisabled='true'/>");
    assertTrue(button.isVisible());

    button.setVisible(true);
    assertTrue(button.isEnabled());
    assertTrue(button.isVisible());

    button.setEnabled(false);
    assertFalse(button.isVisible());

    button.setEnabled(true);
    assertTrue(button.isVisible());

    button.setEnabled(false);
    assertFalse(button.isVisible());
  }

  public void testAutoHideIfDisabledInitializedWithDisabledComponent() throws Exception {
    JButton button = parse("<button enabled='false' autoHideIfDisabled='true'/>");
    assertFalse(button.isVisible());
  }

  public void testSeparator() throws Exception {
    JSeparator separator = parse("<separator orientation='vertical'/>");
    assertEquals(SwingConstants.VERTICAL, separator.getOrientation());
  }
}
