package org.uispec4j;

import junit.framework.AssertionFailedError;
import org.uispec4j.finder.ComponentMatcher;
import org.uispec4j.utils.UIComponentFactory;
import org.uispec4j.xml.XmlAssert;

import javax.swing.*;

import static org.uispec4j.DummySpinner.*;

public class PanelTest extends UIComponentTestCase {

  public void testGetComponentTypeName() throws Exception {
    assertEquals("panel", UIComponentFactory.createUIComponent(new JPanel()).getDescriptionTypeName());
  }

  public void testGetDescription() throws Exception {
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setName("myTabbedPane");

    JPanel jPanel = new JPanel();
    jPanel.setName("myPanel");
    jPanel.add(tabbedPane);

    JTextField textField = new JTextField();
    textField.setName("myText");
    tabbedPane.addTab("1", textField);
    tabbedPane.addTab("2", new JButton("myButton"));

    Panel panel = new Panel(jPanel);
    assertEquals("JPanel name:'myPanel'\n" +
                 "  JTabbedPane name:'myTabbedPane'\n" +
                 "  JTextField name:'myText'", panel.getDescription());
  }

  public void testFactory() throws Exception {
    checkFactory(new JPanel(), Panel.class);
  }

  protected UIComponent createComponent() {
    return new Panel(new JPanel());
  }

  public void testContainsLabel() throws Exception {
    JPanel jPanel = new JPanel();
    jPanel.add(new JLabel("Some text"));
    Panel panel = new Panel(jPanel);
    assertTrue(panel.containsLabel("Some text"));
    assertTrue(panel.containsLabel("text"));
    assertFalse(panel.containsLabel("unknown"));
  }

  public void testSize() throws Exception {
    JPanel jPanel = new JPanel();
    jPanel.setSize(400, 300);
    Panel panel = new Panel(jPanel);
    assertTrue(panel.sizeEquals(400, 300));
    try {
      panel.sizeEquals(450, 350).check();
    }
    catch (AssertionFailedError e) {
      checkException("expected:(450,350) but was:(400,300)", e);
    }
  }

  public void testGetSpinnerThroughModel() throws Exception {
    checkGetSpinnerByModel(dateModel(), new Getter() {
      public UIComponent get(Panel panel) {
        return panel.getDateSpinner();
      }
    });
    checkGetSpinnerByModel(listModel(), new Getter() {
      public UIComponent get(Panel panel) {
        return panel.getListSpinner();
      }
    });
    checkGetSpinnerByModel(numberModel(), new Getter() {
      public UIComponent get(Panel panel) {
        return panel.getNumberSpinner();
      }
    });
  }

  public void testGetSpinnerThroughModelAndComponentName() throws Exception {
    checkGetSpinnerByModel(dateModel(), new Getter() {
      public UIComponent get(Panel panel) {
        return panel.getDateSpinner("spinner1");
      }
    });
    checkGetSpinnerByModel(listModel(), new Getter() {
      public UIComponent get(Panel panel) {
        return panel.getListSpinner("spinner1");
      }
    });
    checkGetSpinnerByModel(numberModel(), new Getter() {
      public UIComponent get(Panel panel) {
        return panel.getNumberSpinner("spinner1");
      }
    });
  }

  public void testGetSpinnerThroughModelAndMatcher() throws Exception {
    checkGetSpinnerByModel(dateModel(), new Getter() {
      public UIComponent get(Panel panel) {
        return panel.getDateSpinner(ComponentMatcher.ALL);
      }
    });
    checkGetSpinnerByModel(listModel(), new Getter() {
      public UIComponent get(Panel panel) {
        return panel.getListSpinner(ComponentMatcher.ALL);
      }
    });
    checkGetSpinnerByModel(numberModel(), new Getter() {
      public UIComponent get(Panel panel) {
        return panel.getNumberSpinner(ComponentMatcher.ALL);
      }
    });
  }

  private void checkGetSpinnerByModel(SpinnerModel model, Getter getter) {
    JPanel jPanel = new JPanel();
    jPanel.add(new JSpinner(new MySpinnerModel()));

    Panel panel = new Panel(jPanel);
    try {
      getter.get(panel);
      fail();
    }
    catch (ItemNotFoundException e) {
      checkException("No component found", e);
    }

    JSpinner jSpinner = new JSpinner(model);
    jSpinner.setName("spinner1");
    jPanel.add(jSpinner);
    assertSame(jSpinner, getter.get(panel).getAwtComponent());
  }

  private interface Getter {
    UIComponent get(Panel panel);
  }

  private static class MySpinnerModel extends AbstractSpinnerModel {
    public Object getNextValue() {
      return null;
    }

    public Object getPreviousValue() {
      return null;
    }

    public Object getValue() {
      return null;
    }

    public void setValue(Object value) {
    }
  }
}
