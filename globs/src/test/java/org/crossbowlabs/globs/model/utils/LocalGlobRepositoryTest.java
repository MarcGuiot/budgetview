package org.crossbowlabs.globs.model.utils;

import junit.framework.TestCase;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.metamodel.DummyObject2;
import org.crossbowlabs.globs.model.DummyChangeSetListener;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.GlobChecker;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.utils.exceptions.InvalidState;

public class LocalGlobRepositoryTest extends TestCase {
  private GlobChecker checker = new GlobChecker();

  public void testStandardCase() throws Exception {
    GlobRepository source = checker.parse(
      "<dummyObject id='0' name='name'/>" +
      "<dummyObject id='1' name='name' value='1.1'/>" +
      "<dummyObject id='2' name='name' value='2.2'/>" +
      "<dummyObject2 id='0'/>");

    LocalGlobRepository local = LocalGlobRepositoryBuilder.init(source).copy(DummyObject.TYPE).get();
    checker.assertEquals(local,
                         "<dummyObject id='0' name='name'/>" +
                         "<dummyObject id='1' name='name' value='1.1'/>" +
                         "<dummyObject id='2' name='name' value='2.2'/>");

    local.create(DummyObject.TYPE,
                 value(DummyObject.ID, 3),
                 value(DummyObject.NAME, "obj3"));

    local.update(Key.create(DummyObject.TYPE, 1), DummyObject.NAME, "newName");

    local.delete(Key.create(DummyObject.TYPE, 2));

    checker.assertEquals(source,
                         "<dummyObject id='0' name='name'/>" +
                         "<dummyObject id='1' name='name' value='1.1'/>" +
                         "<dummyObject id='2' name='name' value='2.2'/>" +
                         "<dummyObject2 id='0'/>");

    checker.assertChangesEqual(local.getCurrentChanges(),
                               "<update type='dummyObject' id='1' name='newName'/>" +
                               "<create type='dummyObject' id='3' name='obj3'/>" +
                               "<delete type='dummyObject' id='2' name='name' value='2.2'/>");

    local.commitChanges(false);

    checker.assertEquals(source,
                         "<dummyObject id='0' name='name'/>" +
                         "<dummyObject id='1' name='newName' value='1.1'/>" +
                         "<dummyObject id='3' name='obj3'/>" +
                         "<dummyObject2 id='0'/>");

    assertTrue(local.getCurrentChanges().isEmpty());
  }

  public void testDispose() throws Exception {
    GlobRepository source = checker.parse("<dummyObject id='0' name='name'/>");

    LocalGlobRepository local = LocalGlobRepositoryBuilder.init(source).copy(DummyObject.TYPE).get();
    assertFalse(local.getAll(DummyObject.TYPE).isEmpty());

    local.dispose();
    try {
      local.getAll(DummyObject.TYPE);
      fail();
    }
    catch (InvalidState e) {
    }
  }

  public void testIdGeneration() throws Exception {
    GlobRepository source = checker.parse(
      "<dummyObject name='name'/>" +
      "<dummyObject name='name' value='1.1'/>" +
      "<dummyObject2 id='0'/>");

    LocalGlobRepository local = LocalGlobRepositoryBuilder.init(source).copy(DummyObject.TYPE).get();
    local.create(DummyObject.TYPE, value(DummyObject.NAME, "new"));
    local.commitChanges(true);

    checker.assertEquals(source,
                         "<dummyObject id='0' name='name'/>" +
                         "<dummyObject id='1' name='name' value='1.1'/>" +
                         "<dummyObject id='2' name='new'/>" +
                         "<dummyObject2 id='0'/>");
  }

  public void testRollback() throws Exception {
    GlobRepository source = checker.parse(
      "<dummyObject id='0' name='name'/>" +
      "<dummyObject id='1' name='name' value='1.1'/>" +
      "<dummyObject id='2' name='name' value='2.2'/>" +
      "<dummyObject2 id='0'/>" +
      "<dummyObject2 id='1'/>");

    LocalGlobRepository local = LocalGlobRepositoryBuilder.init(source).copy(DummyObject.TYPE)
      .copy(source.get(Key.create(DummyObject2.TYPE, 1))).get();
    checker.assertEquals(local,
                         "<dummyObject id='0' name='name'/>" +
                         "<dummyObject id='1' name='name' value='1.1'/>" +
                         "<dummyObject id='2' name='name' value='2.2'/>" +
                         "<dummyObject2 id='1'/>");

    local.create(DummyObject.TYPE,
                 value(DummyObject.ID, 3),
                 value(DummyObject.NAME, "obj3"));

    local.update(Key.create(DummyObject.TYPE, 1), DummyObject.NAME, "newName");

    local.delete(Key.create(DummyObject.TYPE, 2));

    local.delete(Key.create(DummyObject2.TYPE, 1));

    DummyChangeSetListener listener = new DummyChangeSetListener();
    local.addChangeListener(listener);
    local.rollback();
    listener.assertResetListEquals(DummyObject.TYPE, DummyObject2.TYPE);
    checker.assertEquals(local,
                         "<dummyObject id='0' name='name'/>" +
                         "<dummyObject id='1' name='name' value='1.1'/>" +
                         "<dummyObject id='2' name='name' value='2.2'/>" +
                         "<dummyObject2 id='1'/>");

    assertTrue(local.getCurrentChanges().isEmpty());

    local.create(DummyObject.TYPE,
                 value(DummyObject.ID, 3),
                 value(DummyObject.NAME, "obj3"));

    local.update(Key.create(DummyObject.TYPE, 1), DummyObject.NAME, "newName");

    local.delete(Key.create(DummyObject.TYPE, 2));

    checker.assertChangesEqual(local.getCurrentChanges(),
                               "<update type='dummyObject' id='1' name='newName'/>" +
                               "<create type='dummyObject' id='3' name='obj3'/>" +
                               "<delete type='dummyObject' id='2' name='name' value='2.2'/>");

    local.commitChanges(false);

    checker.assertEquals(source,
                         "<dummyObject id='0' name='name'/>" +
                         "<dummyObject id='1' name='newName' value='1.1'/>" +
                         "<dummyObject id='3' name='obj3'/>" +
                         "<dummyObject2 id='0'/>" +
                         "<dummyObject2 id='1'/>");

    assertTrue(local.getCurrentChanges().isEmpty());
  }
}
