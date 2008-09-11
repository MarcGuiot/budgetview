package org.globsframework.gui;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.model.Glob;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.utils.TestUtils;

import java.util.Arrays;
import java.util.List;

public class SelectionServiceTest extends TestCase {
  private DummySelectionListener listener;
  private DummySelectionListener listener2;
  private Glob obj_1;
  private Glob obj_2;
  private Glob obj2_1;
  private SelectionService service;

  protected void setUp() throws Exception {
    obj_1 = GlobBuilder.init(DummyObject.TYPE).set(DummyObject.ID, 1).get();
    obj_2 = GlobBuilder.init(DummyObject.TYPE).set(DummyObject.ID, 2).get();
    obj2_1 = GlobBuilder.init(DummyObject2.TYPE).set(DummyObject2.ID, 1).get();

    service = new SelectionService();
    listener = DummySelectionListener.register(service, DummyObject.TYPE);
    listener2 = DummySelectionListener.register(service, DummyObject2.TYPE);
    service.addListener(listener2, DummyObject2.TYPE);
  }

  public void testMonoSelection() throws Exception {
    List<Glob> currentSelection = Arrays.asList(obj_1, obj_2);
    service.select(currentSelection, DummyObject.TYPE);
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "  <item key='dummyObject[id=1]'/>" +
                          "  <item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");
    listener2.assertEmpty();
    TestUtils.assertEquals(currentSelection, service.getSelection(DummyObject.TYPE));
  }

  public void testMultiSelection() throws Exception {
    service.select(Arrays.asList(obj_1, obj2_1), DummyObject.TYPE, DummyObject2.TYPE);
    String selection = "<log>" +
                       "  <selection types='dummyObject,dummyObject2'>" +
                       "    <item key='dummyObject[id=1]'/>" +
                       "    <item key='dummyObject2[id=1]'/>" +
                       "  </selection>" +
                       "</log>";
    listener.assertEquals(selection);
    listener2.assertEquals(selection);
    assertEquals(obj_1, service.getSelection(DummyObject.TYPE).get(0));
    assertEquals(obj2_1, service.getSelection(DummyObject2.TYPE).get(0));
  }

  public void testRemoveListener() throws Exception {
    service.select(obj_1);
    listener.assertEquals("<log>" +
                          "  <selection types='dummyObject'>" +
                          "    <item key='dummyObject[id=1]'/>" +
                          "  </selection>" +
                          "</log>");

    service.removeListener(listener);
    service.select(obj_1);
    listener.assertEmpty();
  }
}
