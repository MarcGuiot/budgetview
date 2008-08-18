package org.globsframework.model.impl;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObjectWithCompositeKey;

public class TwoFieldKeyTest extends TestCase {
  public void test() throws Exception {

    TwoFieldKey k1 = new TwoFieldKey(DummyObjectWithCompositeKey.ID1, 1,
                                     DummyObjectWithCompositeKey.ID2, 2);
    TwoFieldKey k2 = new TwoFieldKey(DummyObjectWithCompositeKey.ID2, 2,
                                     DummyObjectWithCompositeKey.ID1, 1);
    assertEquals(k1, k2);
    assertTrue(k1.hashCode() == k2.hashCode());
  }
}
