package org.globsframework.model.utils;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobChecker;
import org.globsframework.model.GlobList;
import org.globsframework.model.Key;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.utils.TestUtils;

public class GlobUtilsTest extends TestCase {
  private GlobChecker checker = new GlobChecker();

  public void testDiff() throws Exception {
    Key k1 = Key.create(DummyObject.TYPE, 1);
    Key k2 = Key.create(DummyObject.TYPE, 2);
    Key k3 = Key.create(DummyObject.TYPE, 3);

    DefaultGlobRepository repository = new DefaultGlobRepository();
    Glob g1 = repository.create(k1);
    Glob g2 = repository.create(k2);
    Glob g3 = repository.create(k3);
    checkTwoWay(new GlobList(g1, g2, g3), new GlobList(g1, g2, g3));
    checkTwoWay(new GlobList(g1, g2), new GlobList(g1, g2, g3));
    checkTwoWay(new GlobList(), new GlobList(g1, g2, g3));
    checkTwoWay(new GlobList(), new GlobList());
    checkTwoWay(new GlobList(g1), new GlobList(g1, g2, g3));
    checkTwoWay(new GlobList(g1), new GlobList(g2, g1, g3));
    checkTwoWay(new GlobList(g1, g2, g3), new GlobList(g1, g2));
    checkTwoWay(new GlobList(g1, g2, g3), new GlobList(g1, g2, g3));
    checkTwoWay(new GlobList(g1, g3, g2), new GlobList(g1, g2, g3));
  }

  private void checkTwoWay(GlobList from, GlobList to) {
    check(from, to);
    check(to, from);
  }

  private void check(GlobList from, GlobList to) {
    final GlobList actual = new GlobList(from);
    GlobUtils.diff(from, to, new GlobUtils.DiffFunctor<Glob>() {
      public void add(Glob glob, int index) {
        actual.add(index, glob);
      }

      public void remove(int index) {
        actual.remove(index);
      }
    });
    TestUtils.assertEquals(to, actual);
  }
}
