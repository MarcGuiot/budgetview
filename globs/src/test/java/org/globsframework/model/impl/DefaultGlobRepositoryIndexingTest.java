package org.globsframework.model.impl;

import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObjectIndex;
import org.globsframework.metamodel.Field;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.*;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultGlobRepositoryIndexingTest extends DefaultGlobRepositoryTestCase {
  public void testUniqueIndex() throws Exception {
    init("<dummyObject id='1' name='obj1'/>" +
         "<dummyObject id='2' name='obj3'/>");
    assertTrue(repository.findByIndex(DummyObject.NAME_INDEX, "obj2").isEmpty());
    assertEquals(1, findIDByNameIndex("obj1"));
    assertEquals(2, findIDByNameIndex("obj3"));
    Glob obj1 = findGlobByNameIndex("obj1");
    Glob obj3 = findGlobByNameIndex("obj3");
    repository.update(obj1.getKey(), DummyObject.NAME, "obj3");
    repository.update(obj3.getKey(), DummyObject.NAME, "obj1");
    assertEquals(2, findIDByNameIndex("obj1"));
    assertEquals(1, findIDByNameIndex("obj3"));
    repository.update(obj3.getKey(), DummyObject.NAME, "obj4");
    assertEquals(2, findIDByNameIndex("obj4"));
    repository.delete(obj3.getKey());
    assertTrue(repository.findByIndex(DummyObject.NAME_INDEX, "obj4").isEmpty());

    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 3),
                      value(DummyObject.NAME, "obj5"));
    assertEquals(3, findIDByNameIndex("obj5"));
  }

  public void testNotUniqueIndex() throws Exception {
    init("<dummyObject id='1' name='obj1' date = '2001/01/01'/>" +
         "<dummyObject id='2' name='obj3' date = '2001/01/01'/>");
    assertTrue(repository.findByIndex(DummyObject.DATE_INDEX, Dates.parse("2002/01/01")).isEmpty());
    TestUtils.assertSetEquals(Arrays.asList(1, 2), findIDByDateIndex("2001/01/01"));
    Glob obj1 = findGlobByNameIndex("obj1");
    Glob obj3 = findGlobByNameIndex("obj3");
    repository.update(obj1.getKey(), DummyObject.DATE, Dates.parse("2003/01/01"));
    assertEquals(2, findIDByDateIndex("2001/01/01").get(0).intValue());
    assertEquals(1, findIDByDateIndex("2003/01/01").get(0).intValue());
    repository.delete(obj3.getKey());
    assertTrue(repository.findByIndex(DummyObject.DATE_INDEX, Dates.parse("2001/01/01")).isEmpty());

    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 3),
                      value(DummyObject.NAME, "obj5"),
                      value(DummyObject.DATE, Dates.parse("2004/01/01")));
    assertEquals(Arrays.asList(3), findIDByDateIndex("2004/01/01"));
  }


  public void testMultifieldUniqueIndex() throws Exception {
    init("<dummyObjectIndex id='1' value1='2' value2='3' name='a' />" +
         "<dummyObjectIndex id='2' value1='2' value2='3' name='b' />" +
         "<dummyObjectIndex id='3' value1='2' value2='4' name='a' />" +
         "<dummyObjectIndex id='4' value1='2' value2='5' name='a' />" +
         "<dummyObjectIndex id='5' value1='3' value2='3' name='aaa' />");
    checkId(1).of(2, 3, "a");
    checkId(1, 2).of(2, 3);
    checkId().of(2, 3, "c");
    checkId(3).of(2, 4, "a");
    checkId(1, 2, 3, 4).of(2);
    change(1, 2, 3, "c");
    checkId(1).of(2, 3, "c");
    change(2, 3, 3, "b");
    checkId(2, 5).of(3, 3);
    checkId(1, 3, 4).of(2);
    create(6, 2, 3, "d");
    checkId(1, 3, 4, 6).of(2);
    delete(1);
    checkId(3, 4, 6).of(2);
    change(3, 3, 3, "e");
    checkId(2, 3, 5).of(3, 3);
    checkId(4).of(2, 5, "a");
  }

  public void testUniqueIndexWithDeleteCreate() throws Exception {
    init("<dummyObject id='3' name='obj1'/>" +
         "<dummyObject id='4' name='obj3'/>");
    Glob glob3 = repository.get(Key.create(DummyObject.TYPE, 4));
    Glob glob4 = repository.get(Key.create(DummyObject.TYPE, 3));
    LocalGlobRepositoryBuilder builder = LocalGlobRepositoryBuilder.init(repository);
    builder.copy(DummyObject.TYPE);
    LocalGlobRepository localGlobRepository = builder.get();
    localGlobRepository.create(Key.create(DummyObject.TYPE, 5),
                               FieldValue.value(DummyObject.NAME, "obj1"));
    localGlobRepository.create(Key.create(DummyObject.TYPE, 1),
                               FieldValue.value(DummyObject.NAME, "obj3"));
    localGlobRepository.update(glob3.getKey(), DummyObject.NAME, "obj2");
    localGlobRepository.update(glob4.getKey(), DummyObject.NAME, "obj4");
    localGlobRepository.commitChanges(true);
  }

  public void testMultiFieldNotUniqueIndex() throws Exception {
    init("<dummyObjectIndex id='1' value1='2' value2='3' name='a' />" +
         "<dummyObjectIndex id='2' value1='2' value2='3' name='b' />" +
         "<dummyObjectIndex id='3' value1='2' value2='4' name='a' />" +
         "<dummyObjectIndex id='4' value1='2' value2='5' name='a' />" +
         "<dummyObjectIndex id='5' value1='3' value2='3' name='aaa' />");
    checkIdUnique(1, 2).of(2, 3);
    checkIdUnique(1, 2, 3, 4).of(2);
    change(2, 2, 4, "b");
    checkIdUnique(1).of(2, 3);
    checkIdUnique(2, 3).of(2, 4);
    checkIdUnique(1, 2, 3, 4).of(2);
    change(5, 2, 1, "b");
    checkIdUnique(1, 2, 3, 4, 5).of(2);
    checkIdUnique(5).of(2, 1);
  }

  private void create(int id, int value1, int value2, String name) {
    repository.create(DummyObjectIndex.TYPE,
                      value(DummyObjectIndex.ID, id),
                      value(DummyObjectIndex.VALUE_1, value1),
                      value(DummyObjectIndex.VALUE_2, value2),
                      value(DummyObjectIndex.NAME, name));
  }

  private void delete(int id) {
    repository.delete(Key.create(DummyObjectIndex.TYPE, id));
  }

  private void change(int id, int value1, int value2, String name) {
    Key key = Key.create(DummyObjectIndex.TYPE, id);
    repository.startChangeSet();
    update(key, DummyObjectIndex.VALUE_2, value2);
    update(key, DummyObjectIndex.VALUE_1, value1);
    update(key, DummyObjectIndex.NAME, name);
    repository.completeChangeSet();
  }

  private <T> void update(Key key, Field field, T value) {
    T oldValue = (T)repository.get(key).getValue(field);
    if (oldValue != value) {
      repository.update(key, field, value);
    }
  }

  private class CheckValues {
    private List<Integer> ids;

    public CheckValues(int[] ids) {
      this.ids = new ArrayList<Integer>();
      for (int id : ids) {
        this.ids.add(id);
      }
    }

    public void of(int value1, int value2) {
      GlobRepository.MultiFieldIndexed result =
        repository
          .findByIndex(DummyObjectIndex.VALUES_INDEX, DummyObjectIndex.VALUE_1, value1)
          .findByIndex(DummyObjectIndex.VALUE_2, value2);
      checkIds(result);
    }

    public void of(int value1) {
      GlobRepository.MultiFieldIndexed result =
        repository.findByIndex(DummyObjectIndex.VALUES_INDEX, DummyObjectIndex.VALUE_1, value1);
      checkIds(result);
    }

    private void checkIds(GlobRepository.MultiFieldIndexed result) {
      GlobList globs = result.getGlobs();
      assertEquals(ids.size(), globs.size());
      TestUtils.assertSetEquals(ids, globs.getValueSet(DummyObjectIndex.ID));
    }
  }

  private class CheckValueAndName {
    private List<Integer> ids;

    public CheckValueAndName(int[] ids) {
      this.ids = new ArrayList<Integer>();
      for (int id : ids) {
        this.ids.add(id);
      }
    }

    public void of(int value1, int value2, String name) {
      GlobRepository.MultiFieldIndexed result =
        repository.findByIndex(DummyObjectIndex.VALUES_AND_NAME_INDEX, DummyObjectIndex.VALUE_1, value1)
          .findByIndex(DummyObjectIndex.VALUE_2, value2).findByIndex(DummyObjectIndex.NAME, name);
      checkIds(result);

    }

    public void of(int value1, int value2) {
      GlobRepository.MultiFieldIndexed result =
        repository.findByIndex(DummyObjectIndex.VALUES_AND_NAME_INDEX, DummyObjectIndex.VALUE_1, value1)
          .findByIndex(DummyObjectIndex.VALUE_2, value2);
      checkIds(result);
    }

    public void of(int value1) {
      GlobRepository.MultiFieldIndexed result =
        repository.findByIndex(DummyObjectIndex.VALUES_AND_NAME_INDEX, DummyObjectIndex.VALUE_1, value1);
      checkIds(result);

    }

    private void checkIds(GlobRepository.MultiFieldIndexed result) {
      GlobList globs = result.getGlobs();
      assertEquals(ids.size(), globs.size());
      TestUtils.assertSetEquals(ids, globs.getValueSet(DummyObjectIndex.ID));
      final List<Integer> actual = new ArrayList<Integer>();
      result.saveApply(new GlobFunctor() {
        public void run(Glob glob, GlobRepository repository) throws Exception {
          actual.add(glob.get(DummyObjectIndex.ID));
        }
      }, repository);
      TestUtils.assertSetEquals(ids, actual);
    }
  }

  private CheckValueAndName checkId(int... id) {
    return new CheckValueAndName(id);
  }

  private CheckValues checkIdUnique(int... id) {
    return new CheckValues(id);
  }

  private int findIDByNameIndex(String value) {
    return findGlobByNameIndex(value).get(DummyObject.ID);
  }

  private Glob findGlobByNameIndex(String value) {
    return repository.findByIndex(DummyObject.NAME_INDEX, value).get(0);
  }

  private List<Integer> findIDByDateIndex(String value) {
    List<Integer> val = new ArrayList<Integer>();
    GlobList globByDateIndex = findGlobByDateIndex(value);
    for (Glob glob : globByDateIndex) {
      val.add(glob.get(DummyObject.ID));
    }
    return val;
  }

  private GlobList findGlobByDateIndex(String value) {
    return repository.findByIndex(DummyObject.DATE_INDEX, Dates.parse(value));
  }

}
