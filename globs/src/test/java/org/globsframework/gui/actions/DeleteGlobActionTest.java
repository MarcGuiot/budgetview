package org.globsframework.gui.actions;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.model.DummyChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;

import java.util.Arrays;

public class DeleteGlobActionTest extends GuiComponentTestCase {
  private DeleteGlobAction action;

  protected void setUp() throws Exception {
    super.setUp();
    repository = checker.parse(
      "<dummyObject id='1'/>" +
      "<dummyObject id='2'/>" +
      "<dummyObject id='3'/>" +
      "<dummyObject2 id='0'/>"
    );
    changeListener = new DummyChangeSetListener();
    repository.addChangeListener(changeListener);

    action = new DeleteGlobAction("-", DummyObject.TYPE, repository, directory);
  }

  public void testActionIsDisabledAtFirst() throws Exception {
    assertFalse(action.isEnabled());
  }

  public void testStandardScenario() throws Exception {
    Glob obj1 = repository.get(key1);
    Glob obj2 = repository.get(key2);
    selectionService.select(Arrays.asList(obj1, obj2), DummyObject.TYPE);

    assertTrue(action.isEnabled());

    action.actionPerformed(null);
    changeListener.assertLastChangesEqual(
      "<delete type='dummyObject' id='1'/>" +
      "<delete type='dummyObject' id='2'/>"
    );

    changeListener.reset();
    assertFalse(action.isEnabled());
    action.actionPerformed(null);
    changeListener.assertNoChanges();
  }

  public void testActionIgnoresOtherTypes() throws Exception {
    selectionService.select(repository.getAll(DummyObject2.TYPE), DummyObject2.TYPE);
    assertFalse(action.isEnabled());
    action.actionPerformed(null);
    changeListener.assertNoChanges();
  }

  public void testCondition() throws Exception {
    action.setCondition(new DeleteGlobAction.Condition() {
      public boolean accept(GlobList list) {
        return list.size() == 2;
      }
    });

    Glob obj1 = repository.get(key1);
    Glob obj2 = repository.get(key2);
    Glob obj3 = repository.get(key3);

    selectionService.select(Arrays.asList(obj1), DummyObject.TYPE);
    assertFalse(action.isEnabled());

    selectionService.select(Arrays.asList(obj1, obj2), DummyObject.TYPE);
    assertTrue(action.isEnabled());

    selectionService.select(Arrays.asList(obj1, obj2, obj3), DummyObject.TYPE);
    assertFalse(action.isEnabled());
  }
}
