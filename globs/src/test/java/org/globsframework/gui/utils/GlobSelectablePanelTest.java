package org.globsframework.gui.utils;

import org.globsframework.gui.DummySelectionListener;
import org.globsframework.gui.components.GlobSelectablePanel;
import org.globsframework.gui.splits.DummySplitsNode;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.uispec4j.Mouse;
import org.uispec4j.Panel;

import javax.swing.*;
import java.util.Arrays;

public class GlobSelectablePanelTest extends GuiComponentTestCase {

  private Glob glob1;
  private Glob glob2;
  private Glob glob3;

  public void setUp() throws Exception {
    super.setUp();
    repository = new DefaultGlobRepository();
    glob1 = repository.create(key1);
    glob2 = repository.create(key2);
    glob3 = repository.create(key3);
  }

  public void test() throws Exception {

    DummySplitsNode node = new DummySplitsNode();

    GlobSelectablePanel panel =
      new GlobSelectablePanel(node,
                              "selected", "unselected",
                              "selectedRollover", "unselectedRollover",
                              repository, directory, key1, key2);
    JPanel jPanel = node.getComponent();

    DummySelectionListener selectionListener =
      DummySelectionListener.register(selectionService, DummyObject.TYPE);

    node.checkLastStyle(null);

    selectionService.select(glob1);
    node.checkLastStyle("selected");

    selectionService.clear(DummyObject.TYPE);
    node.checkLastStyle("unselected");

    Mouse.enter(jPanel, 1, 1);
    node.checkLastStyle("unselectedRollover");

    Mouse.click(new Panel(jPanel));
    node.checkLastStyle("selectedRollover");

    Mouse.exit(jPanel, 1, 1);
    node.checkLastStyle("selected");

    selectionService.select(glob3);
    node.checkLastStyle("unselected");

    selectionListener.reset();

    Mouse.enter(jPanel, 1, 1);
    node.checkLastStyle("unselectedRollover");

    Mouse.click(new Panel(jPanel));
    node.checkLastStyle("selectedRollover");
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "  </selection>" +
                                   "</log>");

    Mouse.exit(jPanel, 1, 1);
    node.checkLastStyle("selected");

    selectionService.clearAll();
    node.checkLastStyle("unselected");

    selectionService.select(Arrays.asList(glob3, glob2), DummyObject.TYPE);
    node.checkLastStyle("selected");

    panel.dispose();
    selectionService.select(glob3);
    node.checkLastStyle("selected");
  }

  public void testReclickUnselectsElement() throws Exception {
    DummySplitsNode node = new DummySplitsNode();
    new GlobSelectablePanel(node,
                            "selected", "unselected",
                            "selectedRollover", "unselectedRollover",
                            repository, directory, key1);
    JPanel jPanel = node.getComponent();

    DummySelectionListener selectionListener =
      DummySelectionListener.register(selectionService, DummyObject.TYPE);

    Mouse.enter(jPanel, 1, 1);
    Mouse.click(new Panel(jPanel));
    Mouse.exit(jPanel, 1, 1);
    node.checkLastStyle("selected");
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "  </selection>" +
                                   "</log>");

    Mouse.enter(jPanel, 1, 1);
    Mouse.click(new Panel(jPanel));
    Mouse.exit(jPanel, 1, 1);
    node.checkLastStyle("unselected");
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "  </selection>" +
                                   "</log>");
  }

  public void testReclickUnselectsElementInMultiSelection() throws Exception {
    DummySplitsNode node = new DummySplitsNode();
    new GlobSelectablePanel(node,
                            "selected", "unselected",
                            "selectedRollover", "unselectedRollover",
                            repository, directory, key1);
    JPanel jPanel = node.getComponent();

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);

    node.checkLastStyle("selected");

    DummySelectionListener selectionListener =
      DummySelectionListener.register(selectionService, DummyObject.TYPE);

    Mouse.enter(jPanel, 1, 1);
    Mouse.click(new Panel(jPanel));
    Mouse.exit(jPanel, 1, 1);
    node.checkLastStyle("unselected");
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=2]'/>" +
                                   "  </selection>" +
                                   "</log>");
  }

  public void testDragCanBeUsedForMultiSelection() throws Exception {
    DummySplitsNode node1 = new DummySplitsNode();
    new GlobSelectablePanel(node1,
                            "selected", "unselected",
                            "selectedRollover", "unselectedRollover",
                            repository, directory, key1);
    JPanel jPanel1 = node1.getComponent();

    DummySplitsNode node2 = new DummySplitsNode();
    new GlobSelectablePanel(node2,
                            "selected", "unselected",
                            "selectedRollover", "unselectedRollover",
                            repository, directory, key2);
    JPanel jPanel2 = node2.getComponent();

    DummySelectionListener selectionListener =
      DummySelectionListener.register(selectionService, DummyObject.TYPE);

    node1.checkLastStyle(null);
    node2.checkLastStyle(null);

    Mouse.drag(jPanel1, 1, 1);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "  </selection>" +
                                   "</log>");
    node1.checkLastStyle("selected");
    node2.checkLastStyle("unselected");

    Mouse.drag(jPanel2, 1, 1);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "    <item key='dummyObject[id=2]'/>" +
                                   "  </selection>" +
                                   "</log>");
    node1.checkLastStyle("selected");
    node2.checkLastStyle("selected");
  }

  public void testDragDoesNoInsertDuplicatedElementsInTheSelection() throws Exception {
    DummySplitsNode node1 = new DummySplitsNode();
    new GlobSelectablePanel(node1,
                            "selected", "unselected",
                            "selectedRollover", "unselectedRollover",
                            repository, directory, key1);
    JPanel jPanel1 = node1.getComponent();

    DummySelectionListener selectionListener =
      DummySelectionListener.register(selectionService, DummyObject.TYPE);

    Mouse.drag(jPanel1, 1, 1);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "  </selection>" +
                                   "</log>");
    node1.checkLastStyle("selected");

    Mouse.drag(jPanel1, 1, 2);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "  </selection>" +
                                   "</log>");
    node1.checkLastStyle("selected");
  }

  public void testShiftClickCanBeUsedForMultiSelection() throws Exception {
    DummySplitsNode node1 = new DummySplitsNode();
    new GlobSelectablePanel(node1,
                            "selected", "unselected",
                            "selectedRollover", "unselectedRollover",
                            repository, directory, key1);
    JPanel jPanel1 = node1.getComponent();

    DummySplitsNode node2 = new DummySplitsNode();
    new GlobSelectablePanel(node2,
                            "selected", "unselected",
                            "selectedRollover", "unselectedRollover",
                            repository, directory, key2);
    JPanel jPanel2 = node2.getComponent();

    DummySelectionListener selectionListener =
      DummySelectionListener.register(selectionService, DummyObject.TYPE);

    node1.checkLastStyle(null);
    node2.checkLastStyle(null);

    Mouse.enter(jPanel1, 1, 1);
    Mouse.click(new Panel(jPanel1), org.uispec4j.Key.Modifier.SHIFT);
    Mouse.exit(jPanel1, 1, 1);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "  </selection>" +
                                   "</log>");
    node1.checkLastStyle("selected");
    node2.checkLastStyle("unselected");

    Mouse.enter(jPanel2, 1, 1);
    Mouse.click(new Panel(jPanel2), org.uispec4j.Key.Modifier.SHIFT);
    Mouse.exit(jPanel2, 1, 1);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=1]'/>" +
                                   "    <item key='dummyObject[id=2]'/>" +
                                   "  </selection>" +
                                   "</log>");
    node1.checkLastStyle("selected");
    node2.checkLastStyle("selected");

    Mouse.enter(jPanel1, 1, 1);
    Mouse.click(new Panel(jPanel1), org.uispec4j.Key.Modifier.SHIFT);
    Mouse.exit(jPanel1, 1, 1);
    selectionListener.assertEquals("<log>" +
                                   "  <selection types='dummyObject'>" +
                                   "    <item key='dummyObject[id=2]'/>" +
                                   "  </selection>" +
                                   "</log>");
    node1.checkLastStyle("unselected");
    node2.checkLastStyle("selected");
  }

  public void testTypeCheck() throws Exception {
    DummySplitsNode node = new DummySplitsNode();

    try {
      new GlobSelectablePanel(node,
                              "selected", "unselected",
                              "selectedRollover", "unselectedRollover",
                              repository, directory,
                              Key.create(DummyObject.TYPE, 1),
                              Key.create(DummyObject2.TYPE, 2));
    }
    catch (InvalidParameter e) {
      assertEquals("Keys should be of type 'dummyObject' - unexpected key: dummyObject2[id=2]",
                   e.getMessage());
    }
  }
}
