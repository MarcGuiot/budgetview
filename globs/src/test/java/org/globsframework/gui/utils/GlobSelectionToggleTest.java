package org.globsframework.gui.utils;

import org.globsframework.gui.DummySelectionListener;
import org.globsframework.gui.components.GlobSelectionToggle;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.globsframework.model.repository.DefaultGlobRepository;

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

  // Le bouton écoute les ActionEvent depuis 2014 (740f2e340) : seul un clic de l'utilisateur change la
  // sélection, plus un setSelected() fait par le code (mise à jour depuis la sélection).
  private void clickTo(boolean selected) {
    assertEquals(!selected, jToggle.isSelected());
    jToggle.doClick();
    assertEquals(selected, jToggle.isSelected());
  }

  public void testInitializedWithSelection() throws Exception {
    selectionService.select(glob1);

    initToggle();
    assertTrue(jToggle.isSelected());

    clickTo(false);
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

    clickTo(false);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=2]'/>" +
                                   "    <item key='dummyObject[id=3]'/>" +
                                   "  </selection>" +
                                   "</log>");

    clickTo(true);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "  </selection>" +
                                   "</log>");
  }

  public void testInitializedWithNoSelection() throws Exception {
    initToggle();
    assertFalse(jToggle.isSelected());

    clickTo(true);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "  </selection>" +
                                   "</log>");

    clickTo(false);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'/>" +
                                   "</log>");
  }
}
