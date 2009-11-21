package org.globsframework.model.impl;

import junit.framework.Assert;
import org.globsframework.metamodel.*;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.utils.*;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.*;

import java.util.*;

public class DefaultGlobRepositoryTest extends DefaultGlobRepositoryTestCase {

  public void testFindUnique() throws Exception {
    init("<dummyObject id='0' name='name'/>" +
         "<dummyObject id='1' name='name' value='1.1'/>" +
         "<dummyObject id='2' name='name' value='2.2'/>");

    Glob glob = repository.findUnique(DummyObject.TYPE,
                                      value(DummyObject.ID, 1),
                                      value(DummyObject.VALUE, 1.1));

    assertNotNull(glob);
    assertEquals(1, glob.get(DummyObject.ID).intValue());
    assertEquals(1.1, glob.get(DummyObject.VALUE), 0.01);

    assertNull(repository.findUnique(DummyObject.TYPE, value(DummyObject.NAME, "unknown")));

    try {
      repository.findUnique(DummyObject.TYPE, value(DummyObject.NAME, "name"));
      fail();
    }
    catch (ItemAmbiguity e) {
    }
  }

  public void testFindUniqueWithMatcher() throws Exception {
    init("<dummyObject id='0' name='name'/>" +
         "<dummyObject id='1' name='Name1' value='1.1'/>" +
         "<dummyObject id='2' name='name' value='2.2'/>");

    Glob glob = repository.findUnique(DummyObject.TYPE,
                                      GlobMatchers.fieldEquals(DummyObject.NAME, "Name1"));
    assertNotNull(glob);
    assertEquals(1, glob.get(DummyObject.ID).intValue());
    assertEquals(1.1, glob.get(DummyObject.VALUE), 0.01);

    assertNull(repository.findUnique(DummyObject.TYPE, GlobMatchers.fieldEquals(DummyObject.NAME, "unknown")));

    try {
      repository.findUnique(DummyObject.TYPE, GlobMatchers.fieldEquals(DummyObject.NAME, "name"));
      fail();
    }
    catch (ItemAmbiguity e) {
    }
  }

  public void testFindWithNullKeyReturnsNull() throws Exception {
    initRepository();
    assertNull(repository.find(null));
    assertFalse(repository.contains((Key)null));
  }

  public void testGetAll() throws Exception {
    String xmlDescription = "<dummyObject id='1' name='name' value='1.1'/>" +
                            "<dummyObject2 id='2' label='name2'/>";
    GlobRepository repository = checker.parse(xmlDescription);
    GlobTestUtils.assertEquals(repository, xmlDescription);
  }

  public void testGetSorted() throws Exception {
    init("<dummyObject id='1' name='name'/>" +
         "<dummyObject2 id='4' label='name2'/>" +
         "<dummyObject2 id='2' label='other'/>" +
         "<dummyObject2 id='1' label='name2'/>" +
         "<dummyObject2 id='3' label='name2'/>" +
         "");
    SortedSet<Glob> result = repository.getSorted(DummyObject2.TYPE, new GlobFieldComparator(DummyObject2.ID),
                                                  GlobMatchers.fieldEquals(DummyObject2.LABEL, "name2"));
    Glob firstGlob = repository.get(getKey2(1));
    assertEquals(firstGlob, result.first());
    Glob lastGlob = repository.get(getKey2(4));
    assertEquals(lastGlob, result.last());
  }

  public void testContains() throws Exception {
    String xmlDescription = "<dummyObject id='1' name='name' value='1.1'/>";
    GlobRepository repository = checker.parse(xmlDescription);

    assertTrue(repository.contains(Key.create(DummyObject.TYPE, 1)));
    assertFalse(repository.contains(Key.create(DummyObject.TYPE, 2)));

    assertTrue(repository.contains(DummyObject.TYPE, GlobMatchers.fieldEquals(DummyObject.NAME, "name")));
    assertFalse(repository.contains(DummyObject.TYPE, GlobMatchers.fieldEquals(DummyObject.NAME, "other")));

    assertTrue(repository.contains(DummyObject.TYPE, GlobMatchers.ALL));
    assertFalse(repository.contains(DummyObject.TYPE, GlobMatchers.NONE));

    assertFalse(repository.contains(DummyObject2.TYPE, GlobMatchers.ALL));

    assertTrue(repository.contains(DummyObject.TYPE));
    assertFalse(repository.contains(DummyObject2.TYPE));
  }

  public void testFindTarget() throws Exception {
    init("<dummyObjectWithLinks id='0' targetId1='1' targetId2='2'/>" +
         "<dummyObjectWithCompositeKey id1='1' id2='2' name='targetName'/>");
    Glob source = repository.get(getLinksKey(0));
    Glob target = repository.findLinkTarget(source, DummyObjectWithLinks.COMPOSITE_LINK);
    assertEquals("targetName", target.get(DummyObjectWithCompositeKey.NAME));
  }

  public void testFindTargetWithNoSource() throws Exception {
    initRepository();
    assertNull(repository.findLinkTarget(null, DummyObjectWithLinks.COMPOSITE_LINK));
  }

  public void testFindTargetWithNoResult() throws Exception {
    init("<dummyObjectWithLinks id='0' targetId1='1' targetId2='2'/>" +
         "<dummyObjectWithCompositeKey id1='99' id2='99' name='targetName'/>");
    assertNull(repository.findLinkTarget(repository.get(getLinksKey(0)), DummyObjectWithLinks.COMPOSITE_LINK));
  }

  public void testFindLinkedToWithLinkField() throws Exception {
    init("<dummyObject id='1' name='obj1'/>" +
         "<dummyObject id='2' linkName='obj1'/>" +
         "<dummyObject id='3' linkName='obj1'/>");

    Glob glob1 = repository.get(getKey(1));
    GlobTestUtils.assertListEquals(repository.findLinkedTo(glob1, DummyObject.LINK),
                                   repository,
                                   "<dummyObject id='2'/>" +
                                   "<dummyObject id='3'/>");

    assertEquals(0, repository.findLinkedTo(repository.get(getKey(2)), DummyObject.LINK).size());
  }

  public void testFindLinkedToWithMultiFieldLinks() throws Exception {
    init("<dummyObjectWithCompositeKey id1='1' id2='2' name='targetName'/>" +
         "<dummyObjectWithLinks id='0' targetId1='1' targetId2='2'/>" +
         "<dummyObjectWithLinks id='1' targetId1='1' targetId2='2'/>" +
         "<dummyObjectWithLinks id='2' targetId1='2' targetId2='2'/>"
    );

    Glob target = repository.get(
      KeyBuilder
        .init(DummyObjectWithCompositeKey.ID1, 1)
        .set(DummyObjectWithCompositeKey.ID2, 2)
        .get());

    Glob source0 = repository.get(Key.create(DummyObjectWithLinks.TYPE, 0));
    Glob source1 = repository.get(Key.create(DummyObjectWithLinks.TYPE, 1));
    TestUtils.assertSetEquals(repository.findLinkedTo(target, DummyObjectWithLinks.COMPOSITE_LINK), source0, source1);
  }

  public void testApply() throws Exception {
    init("<dummyObject id='1' name='name'/>" +
         "<dummyObject2 id='4' label='name2'/>" +
         "<dummyObject2 id='2' label='other'/>" +
         "<dummyObject2 id='1' label='name2'/>" +
         "<dummyObject2 id='3' label='name2'/>" +
         "");
    final GlobList actual = new GlobList();
    GlobMatcher matcher = GlobMatchers.fieldEquals(DummyObject2.LABEL, "name2");
    repository.apply(DummyObject2.TYPE, matcher, new GlobFunctor() {
      public void run(Glob glob, GlobRepository repository) throws Exception {
        actual.add(glob);
      }
    });
    TestUtils.assertEquals(repository.getAll(DummyObject2.TYPE, matcher), actual);
  }

  public void testCreateGlob() throws Exception {
    initRepository();

    FieldValues values = FieldValuesBuilder
      .init(DummyObject.ID, 1)
      .set(DummyObject.NAME, "name1")
      .set(DummyObject.PRESENT, true)
      .get();

    Glob glob = repository.create(DummyObject.TYPE, values.toArray());
    assertTrue(glob.matches(values));

    checker.assertEquals(repository,
                         "<dummyObject id='1' name='name1' present='true'/>");

    Key key = KeyBuilder.createFromValues(DummyObject.TYPE, values);
    assertEquals(key, glob.getKey());
    assertSame(glob, repository.get(key));
  }

  public void testCreateWithDefaultValues() throws Exception {
    initRepository();

    Glob glob = repository.create(DummyObjectWithDefaultValues.TYPE, value(DummyObjectWithDefaultValues.DOUBLE, null));
    assertNull(glob.get(DummyObjectWithDefaultValues.DOUBLE));
    assertEquals(7, glob.get(DummyObjectWithDefaultValues.INTEGER).intValue());
  }

  public void testFindOrCreate() throws Exception {
    initRepository();

    Glob obj1 = repository.findOrCreate(Key.create(DummyObject.TYPE, 1),
                                        value(DummyObject.NAME, "name"));
    checker.assertEquals(repository, "<dummyObject id='1' name='name'/>");

    Glob newObj1 = repository.findOrCreate(Key.create(DummyObject.TYPE, 1),
                                           value(DummyObject.NAME, "newName-ignored"));
    assertNotNull(newObj1);
    checker.assertEquals(repository, "<dummyObject id='1' name='name'/>");
    assertEquals(obj1.getKey(), newObj1.getKey());
  }

  public void testCreateWithExistingKey() throws Exception {
    init("<dummyObject id='1' name='name'/>");

    try {
      createDummyObject(repository, 1);
      fail();
    }
    catch (ItemAlreadyExists e) {
    }
    changeListener.assertNoChanges();
  }

  public void testCreateWithFromKey() throws Exception {
    initRepository();

    repository.create(Key.create(DummyObject.TYPE, 125),
                      value(DummyObject.NAME, "name"));
    checker.assertEquals(repository, "<dummyObject id='125' name='name'/>");
  }

  public void testCreateUsesDefaultValues() throws Exception {
    initRepository();

    Glob glob = repository.create(DummyObjectWithDefaultValues.TYPE);
    assertEquals(7, glob.get(DummyObjectWithDefaultValues.INTEGER).intValue());
    assertEquals(3.14159265, glob.get(DummyObjectWithDefaultValues.DOUBLE));
    assertEquals(5l, glob.get(DummyObjectWithDefaultValues.LONG).longValue());
    assertEquals(true, glob.get(DummyObjectWithDefaultValues.BOOLEAN).booleanValue());
    assertTrue(glob.isTrue(DummyObjectWithDefaultValues.BOOLEAN));
    TestUtils.assertDateEquals((Date)DummyObjectWithDefaultValues.DATE.getDefaultValue(),
                               glob.get(DummyObjectWithDefaultValues.DATE), 500);
    TestUtils.assertDateEquals((Date)DummyObjectWithDefaultValues.DATE.getDefaultValue(),
                               glob.get(DummyObjectWithDefaultValues.TIMESTAMP), 500);
  }

  public void testCreationEvent() throws Exception {
    initRepository();

    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 1),
                      value(DummyObject.NAME, "name1"));

    changeListener.assertLastChangesEqual(
      "<create type='dummyObject' id='1' name='name1'/>"
    );
  }

  public void testCreationAndUpdateHaveNoKeyFieldsInTheFieldValuesParameter() throws Exception {
    init("<dummyObject id='1'/>" +
         "<dummyObject id='2'/>");

    repository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        changeSet.safeVisit(new DefaultChangeSetVisitor() {
          public void visitCreation(Key key, FieldValues values) throws Exception {
            values.apply(new NoKeyFieldChecker());
          }

          public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
            values.apply(new NoKeyFieldChecker());
          }
        });
      }
    });

    repository.update(getKey(2), DummyObject.NAME, "newName");
    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 3),
                      value(DummyObject.NAME, "name3"));
  }

  private static class NoKeyFieldChecker implements FieldValues.Functor {
    public void process(Field field, Object value) throws Exception {
      if (field.isKeyField()) {
        Assert.fail(field.getName() + " is a key field");
      }
    }
  }

  public void testCreateGeneratesIntegerIdsAutomaticallyIfNeeded() throws Exception {
    initRepository();
    Glob dummy = repository.create(DummyObject.TYPE, value(DummyObject.NAME, "foo"));
    assertNotNull(dummy.get(DummyObject.ID));
    assertEquals("foo", dummy.get(DummyObject.NAME));
  }

  public void testNoIdGenerationForCompositeKeys() throws Exception {
    checkCreationWithMissingKeyError(DummyObjectWithCompositeKey.TYPE,
                                     "Field 'id1' missing (should not be NULL) for identifying a Glob of type: dummyObjectWithCompositeKey");
  }

  public void testNoIdGenerationForNonIntegerKeys() throws Exception {
    checkCreationWithMissingKeyError(DummyObjectWithStringKey.TYPE,
                                     "Field 'id' missing for identifying a Glob of type: dummyObjectWithStringKey");
  }

  private void checkCreationWithMissingKeyError(GlobType type, String message) {
    initRepository();
    try {
      repository.create(type);
      fail();
    }
    catch (MissingInfo e) {
      assertEquals(message, e.getMessage());
    }
    changeListener.assertNoChanges();
  }

  public void testUpdate() throws Exception {
    init("<dummyObject id='0' name='name'/>");

    repository.update(getKey(0), DummyObject.NAME, "newName");
    checker.assertEquals(repository, "<dummyObject id='0' name='newName'/>");

    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='0' name='newName' _name='name'/>"
    );
  }

  public void testUpdateWithSeveralValues() throws Exception {
    init("<dummyObject id='0' name='name'/>");
    repository.update(getKey(0),
                      value(DummyObject.NAME, "newName"),
                      value(DummyObject.PRESENT, true),
                      value(DummyObject.LINK, 7));
    checker.assertEquals(repository,
                         "<dummyObject id='0' name='newName' present='true' link='7'/>");

    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='0' " +
      "  name='newName' _name='name' " +
      "  present='true' _present='(null)'" +
      "  link='7' _link='(null)'/>"
    );
  }

  public void testUpdateErrors() throws Exception {
    init("<dummyObject id='0' name='name'/>");
    try {
      repository.update(getKey(1), DummyObject.NAME, "newName");
      fail();
    }
    catch (ItemNotFound e) {
      assertEquals("Object 'dummyObject[id=1]' does not exist", e.getMessage());
    }

    try {
      repository.update(getKey(0), DummyObject2.LABEL, "newName");
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("'dummyObject2.label' is not a field of type 'dummyObject'", e.getMessage());
    }
    changeListener.assertNoChanges();
  }

  public void testUpdateWithReadOnlyGlob() throws Exception {
    Key key = initWithReadOnlyGlob(1);
    try {
      repository.update(key, DummyObject.VALUE, 2);
      fail();
    }
    catch (OperationDenied e) {
      assertEquals("Object '" + key + "' cannot be modified", e.getMessage());
    }
    changeListener.assertNoChanges();
  }

  public void testUpdatingAKeyFieldIsForbidden() throws Exception {
    init("<dummyObject id='1' name='name'/>");

    try {
      repository.update(getKey(1), DummyObject.ID, 3);
      fail();
    }
    catch (OperationDenied e) {
      assertEquals("Field 'id' of object 'dummyObject[id=1]' is a key and cannot be changed",
                   e.getMessage());
    }
    changeListener.assertNoChanges();
  }

  public void testUpdatesToSameValueAreIgnored() throws Exception {
    init("<dummyObject id='1' name='name'/>");
    repository.update(getKey(1), DummyObject.NAME, "name");
    repository.update(getKey(1), value(DummyObject.NAME, "name"));
    changeListener.assertNoChanges();
  }

  public void testSequenceOfUpdatesPreservesThePreviousValue() throws Exception {
    init("<dummyObject id='0' name='name'/>");

    repository.startChangeSet();
    repository.update(getKey(0), DummyObject.NAME, "newName1");
    repository.update(getKey(0), DummyObject.NAME, "newName2");
    repository.update(getKey(0), DummyObject.NAME, "newName3");
    repository.completeChangeSet();

    checker.assertEquals(repository, "<dummyObject id='0' name='newName3'/>");

    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='0' name='newName3' _name='name'/>"
    );
  }

  public void testCreateAndUpdateSequenceLeavesAnEmptyPreviousValue() throws Exception {
    initRepository();
    repository.startChangeSet();
    repository.create(getKey(0), value(DummyObject.NAME, "initialName"));
    repository.update(getKey(0), DummyObject.NAME, "newName");
    repository.completeChangeSet();
    checker.assertEquals(repository, "<dummyObject id='0' name='newName'/>");

    changeListener.assertLastChangesEqual(
      "<create type='dummyObject' id='0' name='newName'/>"
    );
  }

  public void testUpdateDeleteSequencePreservesPreviousValues() throws Exception {
    init("<dummyObject id='0' name='name' value='1.4'/>");

    repository.startChangeSet();
    repository.update(getKey(0), DummyObject.NAME, "newName");
    repository.delete(getKey(0));
    repository.completeChangeSet();

    changeListener.assertLastChangesEqual(
      "<delete type='dummyObject' id='0' _name='name' _value='1.4'/>"
    );
  }

  public void testDeleteCreateSequencePreservesPreviousValues() throws Exception {
    init("<dummyObject id='0' name='name' value='1.4'/>");

    repository.startChangeSet();
    repository.delete(getKey(0));
    repository.create(getKey(0), value(DummyObject.NAME, "newName"));
    repository.completeChangeSet();
    checker.assertEquals(repository, "<dummyObject id='0' name='newName'/>");

    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='0' name='newName' _name='name' value='(null)' _value='1.4'/>"
    );
  }

  public void testUpdatingALink() throws Exception {
    init("<dummyObject id='1'/>" +
         "<dummyObject id='2'/>");
    repository.setTarget(getKey(1), DummyObject.LINK, getKey(3));
    assertEquals(3, repository.get(getKey(1)).get(DummyObject.LINK).intValue());
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' link='3' _link='(null)'/>"
    );
    changeListener.reset();
    repository.setTarget(getKey(1), DummyObject.LINK, getKey(3));
    changeListener.assertNoChanges();
  }

  public void testResettingALink() {
    init("<dummyObject id='1' name='name' link='12'/>");
    repository.setTarget(getKey(1), DummyObject.LINK, null);
    assertNull(repository.get(getKey(1)).get(DummyObject.LINK));
  }

  public void testUpdateLinkTargetError() throws Exception {
    init("<dummyObject id='1'/>" +
         "<dummyObject2 id='1'/>");
    try {
      repository.setTarget(getKey(1), DummyObject.LINK, getKey2(1));
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("Key 'dummyObject2[id=1]' is not a valid target for link 'link[dummyObject => dummyObject]'", e.getMessage());
    }
  }

  public void testUpdateLinkOnWrongSource() {
    init("<dummyObject2 id='1'/>");
    try {
      repository.setTarget(getKey2(1), DummyObject.LINK, null);
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("Type 'dummyObject2' is not a valid source for link  'link[dummyObject => dummyObject]'",
                   e.getMessage());
    }
  }

  public void testDeletion() throws Exception {
    init("<dummyObject id='1' name='name'/>");
    repository.delete(getKey(1));
    checker.assertEquals(repository, "");
    changeListener.assertLastChangesEqual(
      "<delete type='dummyObject' id='1' _name='name'/>"
    );
  }

  public void testDeletionOfAList() throws Exception {
    init("<dummyObject id='1' name='obj1'/>" +
         "<dummyObject id='2' name='obj2'/>" +
         "<dummyObject id='3' name='obj3'/>"
    );
    Glob glob1 = repository.get(getKey(1));
    Glob glob2 = repository.get(getKey(2));
    repository.delete(new GlobList(glob1, glob2));
    checker.assertEquals(repository, "<dummyObject id='3'/>");
    changeListener.assertLastChangesEqual(
      "<delete type='dummyObject' id='1' _name='obj1'/>" +
      "<delete type='dummyObject' id='2' _name='obj2'/>"
    );
    checkDummyObjectDisabled(glob1);
    checkDummyObjectDisabled(glob2);
  }

  public void testDeleteInvalidatesTheDeletedGlobInstance() throws Exception {
    init("<dummyObject id='1' name='name'/>");
    Key key = getKey(1);
    Glob glob = repository.get(key);
    repository.delete(key);
    checkDummyObjectDisabled(glob);
  }

  public void testDeleteAllInvalidatesTheDeletedGlobInstance() throws Exception {
    init("<dummyObject id='1' name='name'/>");
    Key key = getKey(1);
    Glob glob = repository.get(key);
    assertTrue(glob.exists());

    repository.deleteAll(DummyObject.TYPE);
    checkDummyObjectDisabled(glob);
  }

  private void checkDummyObjectDisabled(Glob glob) {
    assertFalse(glob.exists());
    try {
      glob.get(DummyObject.NAME);
      fail();
    }
    catch (InvalidState e) {
      assertEquals("Using a deleted instance of 'dummyObject' : " + glob.getKey(), e.getMessage());
    }
  }

  public void testDeletionErrors() throws Exception {
    init("<dummyObject id='0' name='name'/>");
    try {
      repository.delete(getKey(1));
      fail();
    }
    catch (ItemNotFound e) {
      assertEquals("Object 'dummyObject[id=1]' does not exist", e.getMessage());
    }
    changeListener.assertNoChanges();
  }

  public void testDeletingAReadOnlyObjectIsForbidden() throws Exception {
    Key key = initWithReadOnlyGlob(1);
    try {
      repository.delete(key);
      fail();
    }
    catch (OperationDenied e) {
      assertEquals("Object '" + key + "' cannot be modified", e.getMessage());
    }
    try {
      repository.delete(new GlobList(repository.get(key)));
      fail();
    }
    catch (OperationDenied e) {
      assertEquals("Object '" + key + "' cannot be modified", e.getMessage());
    }
    try {
      repository.deleteAll(DummyObject.TYPE);
      fail();
    }
    catch (OperationDenied e) {
      assertEquals("Object '" + key + "' cannot be modified", e.getMessage());
    }
    changeListener.assertNoChanges();
  }

  public void testDeleteAll() throws Exception {
    init(
      "<dummyObject id='1'/>" +
      "<dummyObject id='2'/>" +
      "<dummyObject2 id='1'/>" +
      "<dummyObjectWithCompositeKey id1='1' id2='2'/>"
    );
    repository.deleteAll(DummyObject.TYPE, DummyObjectWithCompositeKey.TYPE);
    changeListener.assertLastChangesEqual(
      "<delete type='dummyObject' id='2'/>" +
      "<delete type='dummyObject' id='1'/>" +
      "<delete type='dummyObjectWithCompositeKey' id1='1' id2='2'/>"
    );
  }

  public void testDeleteAndRecreateKeepTheSameInstance() throws Exception {
    init(
      "<dummyObject id='1' name='value' />" +
      "<dummyObject id='2'/>");
    Key key1 = Key.create(DummyObject.TYPE, 1);
    Glob glob1 = repository.get(key1);
    Key key2 = Key.create(DummyObject.TYPE, 2);
    repository.startChangeSet();
    repository.deleteAll(DummyObject.TYPE);
    repository.create(key1, FieldValue.value(DummyObject.VALUE, 3.3));
    repository.completeChangeSet();
    assertSame(glob1, repository.get(key1));
    assertEquals(3.3, glob1.get(DummyObject.VALUE));
  }

  public void testDeleteAndRecreateResetDefaultValue() throws Exception {
    Key key = Key.create(DummyObjectWithDefaultValues.TYPE, 1);
    repository = new DefaultGlobRepository();
    Glob glob = repository.create(key, FieldValue.value(DummyObjectWithDefaultValues.LONG, 23L));
    repository.startChangeSet();
    repository.deleteAll(DummyObjectWithDefaultValues.TYPE);
    repository.create(key);
    repository.completeChangeSet();
    assertSame(glob, repository.get(key));
    assertEquals(5L, glob.get(DummyObjectWithDefaultValues.LONG).longValue());
  }

  public void testChangeSetIsResetAtEachStep() throws Exception {
    init(
      "<dummyObject id='1' name='name1'/>" +
      "<dummyObject id='2' name='name2'/>"
    );

    createDummyObject(repository, 3);
    changeListener.assertLastChangesEqual(
      "<create type='dummyObject' id='3'/>"
    );

    repository.update(getKey(1), DummyObject.NAME, "newName");
    changeListener.assertLastChangesEqual(
      "<update type='dummyObject' id='1' name='newName' _name='name1'/>"
    );

    repository.delete(getKey(2));
    changeListener.assertLastChangesEqual(
      "<delete type='dummyObject' id='2' _name='name2'/>"
    );

    createDummyObject(repository, 4);
    changeListener.assertLastChangesEqual(
      "<create type='dummyObject' id='4'/>"
    );
  }

  public void testBulkModifications() throws Exception {
    init(
      "<dummyObject id='1' name='name1'/>" +
      "<dummyObject id='2' name='name2'/>"
    );

    repository.startChangeSet();
    createDummyObject(repository, 3);
    repository.update(getKey(1), DummyObject.NAME, "newName");
    repository.delete(getKey(2));
    changeListener.assertNoChanges();

    checker.assertEquals(repository,
                         "<dummyObject id='1' name='newName'/>" +
                         "<dummyObject id='3'/>");

    repository.completeChangeSet();
    changeListener.assertLastChangesEqual(
      "<create type='dummyObject' id='3'/>" +
      "<update type='dummyObject' id='1' name='newName' _name='name1'/>" +
      "<delete type='dummyObject' id='2' _name='name2'/>"
    );
  }

  public void testImbricatedBulkModes() throws Exception {
    init(
      "<dummyObject id='1' name='name1'/>" +
      "<dummyObject id='2' name='name2'/>"
    );

    repository.startChangeSet();
    createDummyObject(repository, 3);

    repository.startChangeSet();
    repository.update(getKey(1), DummyObject.NAME, "newName");

    repository.startChangeSet();
    repository.delete(getKey(2));

    checker.assertEquals(repository,
                         "<dummyObject id='1' name='newName'/>" +
                         "<dummyObject id='3'/>");

    changeListener.assertNoChanges();

    repository.completeChangeSet();
    changeListener.assertNoChanges();

    repository.completeChangeSet();
    changeListener.assertNoChanges();

    repository.completeChangeSet();
    changeListener.assertLastChangesEqual(
      "<create type='dummyObject' id='3'/>" +
      "<update type='dummyObject' id='1' name='newName' _name='name1'/>" +
      "<delete type='dummyObject' id='2' _name='name2'/>"
    );
  }

  public void testTriggersAreCalledBeforeListeners() throws Exception {
    initRepository();
    repository.addTrigger(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
        changeSet.safeVisit(DummyObject.TYPE, new ChangeSetVisitor() {
          public void visitCreation(Key key, FieldValues values) throws Exception {
            String newValue = (values.contains(DummyObject.NAME) ? values.get(DummyObject.NAME) : "null") + "-created";
            repository.update(key, DummyObject.NAME, newValue);
            createObj2(repository, key, key + " created");
          }

          public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
            repository.update(key, DummyObject.NAME,
                              (values.contains(DummyObject.NAME) ? values.get(DummyObject.NAME) : "null") + "-updated");
            createObj2(repository, key, key + " updated");
          }

          public void visitDeletion(Key key, FieldValues values) throws Exception {
            createObj2(repository, key, key + " deleted");
          }
        });
      }
    });

    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 1),
                      value(DummyObject.NAME, "name1"));
    changeListener.assertLastChangesEqual(
      "<create type='dummyObject' id='1' name='name1-created'/>" +
      "<create type='dummyObject2' id='1' label='dummyObject[id=1] created'/>");
    clearTaggingObjects(repository, changeListener);

    repository.startChangeSet();
    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 2),
                      value(DummyObject.NAME, "name2"));
    repository.update(getKey(1), DummyObject.VALUE, 1.1);
    repository.completeChangeSet();
    changeListener.assertLastChangesEqual(
      "<create type='dummyObject' id='2' name='name2-created'/>" +
      "<create type='dummyObject2' id='1' label='dummyObject[id=1] updated'/>" +
      "<update type='dummyObject' id='1' name='null-updated' _name='name1-created' value='1.1' _value='(null)'/>" +
      "<create type='dummyObject2' id='2' label='dummyObject[id=2] created'/>");
    clearTaggingObjects(repository, changeListener);

    repository.delete(getKey(1));
    changeListener.assertLastChangesEqual(
      "<delete type='dummyObject' id='1' _name='null-updated' _value='1.1'/>" +
      "<create type='dummyObject2' id='1' label='dummyObject[id=1] deleted'/>");
  }

  public void testCompleteBulkDispatchingWithoutTriggers() throws Exception {
    init("<dummyObject2 id='2'/>");

    DummyChangeSetListener listener = new DummyChangeSetListener();
    repository.addTrigger(listener);

    repository.startChangeSet();

    repository.create(DummyObject.TYPE, value(DummyObject.ID, 1), value(DummyObject.NAME, "obj1"));

    DefaultChangeSet changeSet = new DefaultChangeSet();
    changeSet.processCreation(DummyObject.TYPE,
                              FieldValuesBuilder.init()
                                .set(DummyObject.ID, 3)
                                .set(DummyObject.NAME, "obj3")
                                .get());
    changeSet.processUpdate(getKey(1), DummyObject.NAME, "newObj1", null);
    changeSet.processDeletion(getKey2(2), FieldValues.EMPTY);

    repository.apply(changeSet);

    repository.completeChangeSetWithoutTriggers();

    listener.assertNoChanges();
    checker.assertEquals(repository,
                         "<dummyObject id='1' name='newObj1'/>" +
                         "<dummyObject id='3' name='obj3'/>");
  }

  public void testCompleteBulkDispatchingWithoutTriggersMustBeTheTopLevelComplete() throws Exception {
    init("<dummyObject id='1'/>");

    repository.startChangeSet();
    repository.startChangeSet();
    try {
      repository.completeChangeSetWithoutTriggers();
      fail();
    }
    catch (InvalidState e) {
      assertEquals("This method must be called for the outermost enterBulkDispatchingMode call", e.getMessage());
    }
  }

  private void createObj2(GlobRepository repository, Key referenceKey, String text) {
    repository.create(DummyObject2.TYPE,
                      value(DummyObject2.ID, referenceKey.get(DummyObject.ID)),
                      value(DummyObject2.LABEL, text));
  }

  public void testReset() throws Exception {
    init(
      "<dummyObject id='1'/>" +
      "<dummyObject id='2'/>" +
      "<dummyObject2 id='1'/>"
    );

    Glob dummyObject3 =
      GlobBuilder.init(DummyObject.TYPE).set(DummyObject.ID, 3).get();
    repository.reset(new GlobList(dummyObject3), DummyObject.TYPE);

    changeListener.assertResetListEquals(DummyObject.TYPE);
    trigger.assertResetListEquals(DummyObject.TYPE);

    GlobList actualObjects = repository.getAll(DummyObject.TYPE);
    assertTrue(Arrays.equals(dummyObject3.toArray(), actualObjects.get(0).toArray()));
  }

  public void testResetAlsoResetsTypesWhenNoCorrespondingGlobsAreInTheGlobsList() throws Exception {
    init(
      "<dummyObject id='1'/>" +
      "<dummyObject id='2'/>" +
      "<dummyObject2 id='1'/>"
    );
    repository.reset(GlobList.EMPTY, DummyObject.TYPE);

    changeListener.assertResetListEquals(DummyObject.TYPE);
    trigger.assertResetListEquals(DummyObject.TYPE);

    GlobList actualObjects = repository.getAll(DummyObject.TYPE);
    TestUtils.assertEquals(Collections.EMPTY_LIST, actualObjects);
  }

  public void testResetOnlyReplacesGlobsWithSpecifiedType() throws Exception {
    init(
      "<dummyObject id='1'/>" +
      "<dummyObject id='2'/>" +
      "<dummyObject2 id='1'/>"
    );
    Glob newDummyObject =
      GlobBuilder.init(DummyObject.TYPE).set(DummyObject.ID, 3).get();
    Glob newDummyObject2 =
      GlobBuilder.init(DummyObject2.TYPE).set(DummyObject2.ID, 2).get();
    repository.reset(new GlobList(newDummyObject, newDummyObject2), DummyObject2.TYPE);

    changeListener.assertResetListEquals(DummyObject2.TYPE);
    trigger.assertResetListEquals(DummyObject2.TYPE);

    checker.assertEquals(repository,
                         "<dummyObject id='1'/>" +
                         "<dummyObject id='2'/>" +
                         "<dummyObject2 id='2'/>");
  }

  public void testChangesMadeWithinResetEventAreDispatchedAfterTheResetNotification() throws Exception {
    init(
      "<dummyObject id='1'/>" +
      "<dummyObject id='2'/>" +
      "<dummyObject2 id='1'/>"
    );
    final List<String> log = new ArrayList<String>();
    final GlobIdGenerator idGenerator = repository.getIdGenerator();
    repository.addTrigger(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        long nextId = idGenerator.getNextId(DummyObject.ID, 1);
        log.add("trigger.globsChanged.begin " + nextId);
        createDummyObject(repository, nextId);
        log.add("trigger.globsChanged.end " + nextId);
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        long nextId = idGenerator.getNextId(DummyObject.ID, 1);
        log.add("trigger.globsReset.begin " + nextId);
        createDummyObject(repository, nextId);
        log.add("trigger.globsReset.end " + nextId);
      }
    });
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        log.add("listener.globsChanged");
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        log.add("listener.globsReset");
      }
    });

    Glob dummyObject3 =
      GlobBuilder.init(DummyObject.TYPE).set(DummyObject.ID, 3).get();
    repository.reset(new GlobList(dummyObject3), DummyObject.TYPE);

    TestUtils.assertEquals(Arrays.asList(
      "trigger.globsReset.begin 100",
      "trigger.globsReset.end 100",
      "listener.globsReset",
      "trigger.globsChanged.begin 101",
      "trigger.globsChanged.end 101",
      "listener.globsChanged"
    ), log);
  }

  public void testApplyChangeSet() throws Exception {
    init(
      "<dummyObject id='1' name='obj1'/>" +
      "<dummyObject2 id='2'/>"
    );

    DefaultChangeSet changeSet = new DefaultChangeSet();
    changeSet.processCreation(DummyObject.TYPE,
                              FieldValuesBuilder.init()
                                .set(DummyObject.ID, 3)
                                .set(DummyObject.NAME, "obj3")
                                .get());

    changeSet.processUpdate(getKey(1), DummyObject.NAME, "newObj1", null);
    changeSet.processDeletion(getKey2(2), FieldValues.EMPTY);

    repository.apply(changeSet);
    checker.assertEquals(repository,
                         "<dummyObject id='1' name='newObj1'/>" +
                         "<dummyObject id='3' name='obj3'/>");
  }

  public void testApplyChangeSetDoesNotChangeTheRepositoryInCaseOfError() throws Exception {
    init(
      "<dummyObject id='1' name='obj1'/>" +
      "<dummyObject2 id='2'/>"
    );

    DefaultChangeSet changeSet = new DefaultChangeSet();
    changeSet.processCreation(DummyObject.TYPE,
                              FieldValuesBuilder.init()
                                .set(DummyObject.ID, 3)
                                .set(DummyObject.NAME, "obj3")
                                .get());

    changeSet.processUpdate(getKey(1), DummyObject.NAME, "newObj1", null);
    changeSet.processDeletion(getKey2(2), FieldValues.EMPTY);
    changeSet.processDeletion(getKey(666), FieldValues.EMPTY);

    try {
      repository.apply(changeSet);
      fail();
    }
    catch (InvalidParameter invalidParameter) {
    }

    checker.assertEquals(repository,
                         "<dummyObject id='1' name='obj1'/>" +
                         "<dummyObject2 id='2'/>");
  }

  public void testApplyChangeSetErrorOnCreate() throws Exception {
    init(
      "<dummyObject id='1' name='obj1'/>"
    );

    DefaultChangeSet changeSet = new DefaultChangeSet();
    changeSet.processCreation(DummyObject.TYPE,
                              FieldValuesBuilder.init()
                                .set(DummyObject.ID, 1)
                                .set(DummyObject.NAME, "obj3")
                                .get());
    checkApplyChangeSetError(changeSet,
                             "Object dummyObject[id=1] already exists\n" +
                             "-- New object values:\n" +
                             "name=obj3\n" +
                             "-- Existing object:\n" +
                             "id=1\n" +
                             "name=obj1\n" +
                             "value=null\n" +
                             "present=null\n" +
                             "date=null\n" +
                             "timestamp=null\n" +
                             "password=null\n" +
                             "link=null\n");
  }

  public void testApplyChangeSetErrorOnUpdate() throws Exception {
    init(
      "<dummyObject id='1' name='obj1'/>"
    );

    MutableChangeSet changeSet = new DefaultChangeSet();
    changeSet.processUpdate(getKey(2), DummyObject.NAME, "newObj1", null);
    checkApplyChangeSetError(changeSet, "Object dummyObject[id=2] not found - cannot apply update");
  }

  public void testApplyChangeSetErrorOnDelete() throws Exception {
    init(
      "<dummyObject id='1' name='obj1'/>"
    );

    MutableChangeSet changeSet = new DefaultChangeSet();
    changeSet.processDeletion(getKey(2), FieldValues.EMPTY);
    checkApplyChangeSetError(changeSet, "Object dummyObject[id=2] not found - cannot apply deletion");
  }

  public void testChangeSetIsCleanedUpWithReverseOperations() throws Exception {
    init(
      "<dummyObject id='2' name='name2'/>" +
      "<dummyObject id='3' name='name3'/>"
    );

    repository.startChangeSet();
    repository.create(getKey(1));
    repository.update(getKey(2), DummyObject.NAME, "newName");
    repository.delete(getKey(3));

    repository.delete(getKey(1));
    repository.update(getKey(2), DummyObject.NAME, "name2");
    repository.create(getKey(3), value(DummyObject.NAME, "name3"));

    repository.completeChangeSet();

    changeListener.assertNoChanges();
  }

  public void testTriggerShowEachOtherChanges() throws Exception {
    final boolean[] call = new boolean[]{false};
    repository = new DefaultGlobRepository();
    repository.addTrigger(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        repository.create(getKey(1));
      }
    });
    repository.addTrigger(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        Set<Key> keySet = changeSet.getCreated(DummyObject.TYPE);
        assertEquals(2, keySet.size());
        TestUtils.assertSetEquals(keySet, getKey(0), getKey(1));
        call[0] = true;
      }
    });
    repository.create(getKey(0));
    assertTrue(call[0]);
  }

  public void testResetUpdateIndex() throws Exception {
    init("<dummyObject id='0' name='name'/>" +
         "<dummyObject id='1' name='name1' value='1.1'/>" +
         "<dummyObject id='2' name='name2' value='2.2'/>");

    GlobList listForName1 = repository.findByIndex(DummyObject.NAME_INDEX, "name1");
    assertEquals(1, listForName1.size());
    Glob dummyObject3 =
      GlobBuilder.init(DummyObject.TYPE).set(DummyObject.ID, 3)
        .set(DummyObject.NAME, "name3").get();
    repository.reset(new GlobList(dummyObject3), DummyObject.TYPE);
    assertEquals(1, repository.getAll(DummyObject.TYPE).size());
    GlobList shouldBeEmpty = repository.findByIndex(DummyObject.NAME_INDEX, "name1");
    assertTrue(shouldBeEmpty.isEmpty());
    GlobList listForName3 = repository.findByIndex(DummyObject.NAME_INDEX, "name3");
    assertEquals(1, listForName3.size());
  }

  private void checkApplyChangeSetError(ChangeSet changeSet, String expectedMessage) {
    try {
      repository.apply(changeSet);
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals(expectedMessage, e.getMessage());
    }
  }

  private void createDummyObject(GlobRepository repository, long nextId) {
    repository.create(DummyObject.TYPE, value(DummyObject.ID, (int)nextId));
  }

  private void clearTaggingObjects(GlobRepository repository, DummyChangeSetListener listener) {
    repository.deleteAll(DummyObject2.TYPE);
    listener.reset();
  }

}
