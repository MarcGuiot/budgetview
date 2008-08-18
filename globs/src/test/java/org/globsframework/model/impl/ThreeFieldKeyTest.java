package org.globsframework.model.impl;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObjectWithTripleKey;

public class ThreeFieldKeyTest extends TestCase {
  public void test() throws Exception {

    ThreeFieldKey k1 = new ThreeFieldKey(DummyObjectWithTripleKey.ID1, 1,
                                         DummyObjectWithTripleKey.ID2, 2,
                                         DummyObjectWithTripleKey.ID3, 3);
    ThreeFieldKey k2 = new ThreeFieldKey(DummyObjectWithTripleKey.ID2, 2,
                                         DummyObjectWithTripleKey.ID1, 1,
                                         DummyObjectWithTripleKey.ID3, 3);
    assertEquals(k1, k2);
    assertTrue(k1.hashCode() == k2.hashCode());
  }
}