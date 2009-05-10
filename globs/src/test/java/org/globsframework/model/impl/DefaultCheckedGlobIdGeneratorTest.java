package org.globsframework.model.impl;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobChecker;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.utils.GlobBuilder;

public class DefaultCheckedGlobIdGeneratorTest extends TestCase {
  private GlobChecker checker = new GlobChecker();
  private GlobRepository repository;

  public void testStandardCase() throws Exception {
    repository = GlobRepositoryBuilder.init().get();
    assertEquals(DefaultGlobIdGenerator.class, repository.getIdGenerator().getClass());

    createWithNoId("obj0");
    createWithNoId("obj1");

    repository.create(DummyObject2.TYPE, value(DummyObject2.LABEL, "other"));

    checker.assertEquals(repository,
                         "<dummyObject id='0' name='obj0'/>" +
                         "<dummyObject id='1' name='obj1'/>" +
                         "<dummyObject2 id='0' label='other'/>");
  }

  public void testInitWithExistingGlobs() throws Exception {
    repository = GlobRepositoryBuilder.init()
      .add(GlobBuilder.init(DummyObject.TYPE).set(DummyObject.ID, 12)
        .set(DummyObject.NAME, "obj12").get())
      .add(GlobBuilder.init(DummyObject.TYPE).set(DummyObject.ID, 14)
        .set(DummyObject.NAME, "obj14").get())
      .get();

    createWithNoId("obj15");

    checker.assertEquals(repository,
                         "<dummyObject id='12' name='obj12'/>" +
                         "<dummyObject id='14' name='obj14'/>" +
                         "<dummyObject id='15' name='obj15'/>");
  }

  public void testMixingHardcodedAndGeneratedIds() throws Exception {
    repository = GlobRepositoryBuilder.init().get();

    createWithNoId("obj0");

    create(1, "obj1");
    create(2, "obj2");

    createWithNoId("obj3");

    checker.assertEquals(repository,
                         "<dummyObject id='0' name='obj0'/>" +
                         "<dummyObject id='1' name='obj1'/>" +
                         "<dummyObject id='2' name='obj2'/>" +
                         "<dummyObject id='3' name='obj3'/>");
  }

  public void testTakesTheFirstAvailableId() throws Exception {
    repository = GlobRepositoryBuilder.init().get();

    createWithNoId("obj0");

    create(1, "obj1");
    create(4, "obj4");

    createWithNoId("obj2");

    checker.assertEquals(repository,
                         "<dummyObject id='0' name='obj0'/>" +
                         "<dummyObject id='1' name='obj1'/>" +
                         "<dummyObject id='2' name='obj2'/>" +
                         "<dummyObject id='4' name='obj4'/>");
  }

  public void testChecksForAllSteps() throws Exception {
    DefaultCheckedGlobIdGenerator generator = new DefaultCheckedGlobIdGenerator();
    repository = GlobRepositoryBuilder.init().get();
    generator.setRepository(repository);

    create(0, "obj1");
    create(4, "obj4");

    assertEquals(5, generator.getNextId(DummyObject.ID, 10));
  }

  private void create(int id, String name) {
    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, id),
                      value(DummyObject.NAME, name));
  }

  private void createWithNoId(String name) {
    repository.create(DummyObject.TYPE, value(DummyObject.NAME, name));
  }
}
