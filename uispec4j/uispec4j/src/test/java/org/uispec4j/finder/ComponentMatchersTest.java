package org.uispec4j.finder;

import org.uispec4j.ComponentAmbiguityException;
import org.uispec4j.ItemNotFoundException;
import org.uispec4j.TestUtils;

import javax.swing.*;
import java.awt.*;

public class ComponentMatchersTest extends PanelComponentFinderTestCase {
  private JButton button1;
  private JButton button2;
  private JTextField textField;
  private Component otherButton;

  protected void setUp() throws Exception {
    super.setUp();
    button1 = addComponent(JButton.class, "displayed1");
    button1.setName("inner1");
    button2 = addComponent(JButton.class, "displayed2");
    button2.setName("inner2");
    textField = addComponent(JTextField.class, "displayed1");
    textField.setName("inner1");
    otherButton = addComponent(JButton.class, "other");
    otherButton.setName("else");
  }

  public void testClassComponentMatcher() throws Exception {
    TestUtils.assertSwingComponentsEquals(new JTextField[]{textField},
                                          panel.getSwingComponents(ComponentMatchers.fromClass(JTextField.class)));
  }

  public void testDisplayedNameIdentity() throws Exception {
    TestUtils.assertUIComponentRefersTo(button2,
                                        panel.getButton(ComponentMatchers.displayedNameIdentity("displayed2")));

    try {
      panel.getButton(ComponentMatchers.displayedNameIdentity("displayed"));
      fail();
    }
    catch (ItemNotFoundException e) {
      checkException("No component found", e);
    }

    try {
      panel.getButton(ComponentMatchers.displayedNameIdentity("inner2"));
      fail();
    }
    catch (ItemNotFoundException e) {
      checkException("No component found", e);
    }

    TestUtils.assertSwingComponentsEquals(new Component[]{button1, textField},
                                          panel.getSwingComponents(ComponentMatchers.displayedNameIdentity("displayed1")));
  }

  public void testDisplayedNameSubstring() throws Exception {
    TestUtils.assertUIComponentRefersTo(button2,
                                        panel.getButton(ComponentMatchers.displayedNameSubstring("displayed2")));

    try {
      panel.getButton(ComponentMatchers.displayedNameSubstring("displayed"));
      fail();
    }
    catch (ComponentAmbiguityException e) {
      checkException(Messages.computeAmbiguityMessage(new Component[]{button1, button2}, null, null), e);
    }

    assertNull(panel.findSwingComponent(ComponentMatchers.displayedNameSubstring("inner")));

    TestUtils.assertSwingComponentsEquals(new Component[]{button1, button2, textField},
                                          panel.getSwingComponents(ComponentMatchers.displayedNameSubstring("displayed")));
  }

  public void testDisplayedNameRegexp() throws Exception {
    TestUtils.assertUIComponentRefersTo(button2,
                                        panel.getButton(ComponentMatchers.displayedNameRegexp("displaye.?2")));

    try {
      panel.getButton(ComponentMatchers.displayedNameRegexp("displayed.?"));
      fail();
    }
    catch (ComponentAmbiguityException e) {
      checkException(Messages.computeAmbiguityMessage(new Component[]{button1, button2}, null, null), e);
    }

    try {
      panel.getButton(ComponentMatchers.displayedNameRegexp("inn.*"));
      fail();
    }
    catch (ItemNotFoundException e) {
      checkException("No component found", e);
    }

    TestUtils.assertSwingComponentsEquals(new Component[]{button1, button2, textField},
                                          panel.getSwingComponents(ComponentMatchers.displayedNameRegexp("dis.*")));
  }

  public void testInnerNameIdentity() throws Exception {
    TestUtils.assertUIComponentRefersTo(button2,
                                        panel.getButton(ComponentMatchers.innerNameIdentity("inner2")));

  }

  public void testInnerNameSubstring() throws Exception {
    TestUtils.assertSwingComponentsEquals(new Component[]{button1, button2, textField},
                                          panel.getSwingComponents(ComponentMatchers.innerNameSubstring("inner")));
  }

  public void testInnerNameRegexp() throws Exception {
    TestUtils.assertUIComponentRefersTo(button2,
                                        panel.getButton(ComponentMatchers.innerNameRegexp("inne.?2")));

    try {
      panel.getButton(ComponentMatchers.innerNameRegexp("disp.*"));
      fail();
    }
    catch (ItemNotFoundException e) {
      checkException("No component found", e);
    }

    TestUtils.assertSwingComponentsEquals(new Component[]{button1, button2, textField},
                                          panel.getSwingComponents(ComponentMatchers.innerNameRegexp("inn.*")));
  }

  public void testComponentLabelFor() throws Exception {
    JLabel labelForButton2 = addComponent(JLabel.class, "this is the second button");
    labelForButton2.setLabelFor(button2);

    TestUtils.assertUIComponentRefersTo(button2,
                                        panel.getButton(ComponentMatchers.componentLabelFor("second button")));
  }

  public void testVisible() throws Exception {
    button1.setVisible(true);
    button2.setVisible(false);
    textField.setVisible(true);
    otherButton.setVisible(false);

    TestUtils.assertSwingComponentsEquals(new Component[]{button1, textField},
                                          panel.getSwingComponents(ComponentMatchers.visible(true)));
    TestUtils.assertSwingComponentsEquals(new Component[]{button2, otherButton},
                                          panel.getSwingComponents(ComponentMatchers.visible(false)));
  }

  public void testSearchWithTooltip() throws Exception {
    button1.setToolTipText("button 1");

    TestUtils.assertUIComponentRefersTo(button1,
                                        panel.getButton(ComponentMatchers.toolTipEquals("button 1")));

    TestUtils.assertSwingComponentsEquals(new Component[]{button2, textField, otherButton},
                                          panel.getSwingComponents(ComponentMatchers.toolTipEquals(null)));
  }
}
