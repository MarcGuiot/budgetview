package org.crossbowlabs.globs.model.utils;

import junit.framework.TestCase;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.model.*;
import static org.crossbowlabs.globs.model.utils.GlobMatchers.*;
import org.crossbowlabs.globs.utils.TestUtils;

public class GlobMatchersTest extends TestCase {
  protected Glob a;
  protected Glob b;
  protected Glob c;
  protected Glob d;
  protected Glob unknown;
  protected GlobRepository repository;
  protected GlobList list;

  public void setUp() throws Exception {
    GlobChecker checker = new GlobChecker();
    repository = checker.parse(
          "<dummyObject id='0' name='obj_a' value='1' present='true'/>" +
          "<dummyObject id='1' name='obj_b' value='1' present='false'/>" +
          "<dummyObject id='2' name='obj_c' value='2' present='true'/>" +
          "<dummyObject id='3' name='obj_d' value='2'/>"
    );

    a = repository.get(Key.create(DummyObject.TYPE, 0));
    b = repository.get(Key.create(DummyObject.TYPE, 1));
    c = repository.get(Key.create(DummyObject.TYPE, 2));
    d = repository.get(Key.create(DummyObject.TYPE, 3));

    list = new GlobList(a, b, c, d);
  }

  public void testFieldEquals() throws Exception {
    check(fieldEquals(DummyObject.NAME, "obj_a"), a);
    check(fieldEqualsIgnoreCase(DummyObject.NAME, "OBJ_a"), a);
    check(fieldEquals(DummyObject.PRESENT, true), a, c);
  }

  public void testAnd() throws Exception {
    check(and(fieldEquals(DummyObject.NAME, "obj_b"), fieldEquals(DummyObject.ID, 1)),
          b);

    check(and(fieldEquals(DummyObject.NAME, "obj_b"), fieldEquals(DummyObject.ID, 1), GlobMatchers.ALL),
          b);

    check(and(fieldEquals(DummyObject.NAME, "obj_b"), fieldEquals(DummyObject.ID, 1), GlobMatchers.NONE));
  }

  public void testOr() throws Exception {
    check(or(fieldEquals(DummyObject.NAME, "obj_a"), fieldEquals(DummyObject.NAME, "obj_c")),
          a, c);

    check(or(fieldEquals(DummyObject.NAME, "obj_a"), GlobMatchers.ALL),
          a, b, c, d);

    check(or(fieldEquals(DummyObject.NAME, "obj_a"), GlobMatchers.NONE),
          a);
  }

  public void testNot() throws Exception {
    GlobMatcher matcher = not(fieldEquals(DummyObject.NAME, "obj_c"));
    check(matcher, a, b, d);
  }

  public void testNull() throws Exception {
    check(isNull(DummyObject.PRESENT), d);
    check(isNotNull(DummyObject.PRESENT), a, b, c);
  }

  public void testFieldIn() throws Exception {
    check(fieldIn(DummyObject.ID, new Integer[0]));
    check(fieldIn(DummyObject.ID, 0, 2), a, c);
  }

  private void check(GlobMatcher matcher, Glob... result) {
    GlobList actual = list.filter(matcher, repository);
    TestUtils.assertEquals(actual, result);
  }
}
