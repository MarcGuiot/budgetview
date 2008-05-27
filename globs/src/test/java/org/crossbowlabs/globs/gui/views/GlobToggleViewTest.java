package org.crossbowlabs.globs.gui.views;

import org.crossbowlabs.globs.gui.DummySelectionListener;
import org.crossbowlabs.globs.gui.utils.GuiComponentTestCase;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.utils.GlobFieldComparator;
import org.uispec4j.Panel;
import org.uispec4j.ToggleButton;

import javax.swing.*;
import java.util.Collections;

public class GlobToggleViewTest extends GuiComponentTestCase {
  public void test() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>" +
                    "<dummyObject id='3' name='name3'/>");
    GlobList list = repository.getAll(DummyObject.TYPE);
    Collections.sort(list, new GlobFieldComparator(DummyObject.NAME));
    JPanel jPanel = GlobToggleView.init(DummyObject.TYPE, list, repository, directory).getComponent();
    Panel panel = new Panel(jPanel);
    ToggleButton button1 = panel.getToggleButton("name1");
    ToggleButton button2 = panel.getToggleButton("name2");
    ToggleButton button3 = panel.getToggleButton("name3");
    DummySelectionListener listener = DummySelectionListener.register(directory, DummyObject.TYPE);

    assertFalse(button1.isSelected());
    assertFalse(button2.isSelected());
    assertFalse(button3.isSelected());

    button1.click();
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "  <item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");
    assertFalse(button2.isSelected());
    assertFalse(button3.isSelected());

    button3.click();
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "  <item key='dummyObject[id=3]'/>" +
                          "</selection>" +
                          "</log>");
    assertFalse(button1.isSelected());
    assertFalse(button2.isSelected());
  }
}
