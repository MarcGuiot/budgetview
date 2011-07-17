package org.globsframework.gui.utils;

import org.globsframework.gui.DummySelectionListener;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.utils.TestUtils;

import javax.swing.*;
import java.util.Arrays;

public class GlobSelectionToggleTest extends GuiComponentTestCase {

  private Glob glob1;
  private Glob glob2;
  private Glob glob3;
  private GlobSelectionToggle toggle;
  private JToggleButton jToggle;
  private DummySelectionListener selectionListener;

  public void setUp() throws Exception {
    super.setUp();
    repository = new DefaultGlobRepository();
    glob1 = repository.create(key1);
    glob2 = repository.create(key2);
    glob3 = repository.create(key3);
  }

  private void initToggle() {
    toggle = new GlobSelectionToggle(key1, repository, directory);
    jToggle = toggle.getComponent();

    selectionListener = DummySelectionListener.register(selectionService, DummyObject.TYPE);
  }

  public void testInitializedWithSelection() throws Exception {
    selectionService.select(glob1);

    initToggle();
    assertTrue(jToggle.isSelected());

    jToggle.setSelected(false);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'/>" +
                                   "</log>");

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertTrue(jToggle.isSelected());

    selectionService.select(Arrays.asList(glob2, glob3), DummyObject.TYPE);
    assertFalse(jToggle.isSelected());

    selectionService.select(Arrays.asList(glob1, glob2, glob3), DummyObject.TYPE);
    assertTrue(jToggle.isSelected());

    selectionListener.reset();

    jToggle.setSelected(false);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=2]'/>" +
                                   "    <item key='dummyObject[id=3]'/>" +
                                   "  </selection>" +
                                   "</log>");

    jToggle.setSelected(true);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "  </selection>" +
                                   "</log>");
  }

  public void testInitializedWithNoSelection() throws Exception {
    initToggle();
    assertFalse(jToggle.isSelected());

    jToggle.setSelected(true);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "  </selection>" +
                                   "</log>");

    jToggle.setSelected(false);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'/>" +
                                   "</log>");
  }
}
