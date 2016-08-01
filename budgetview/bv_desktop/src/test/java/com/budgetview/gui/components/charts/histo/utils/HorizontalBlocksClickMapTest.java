package com.budgetview.gui.components.charts.histo.utils;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Key;

public class HorizontalBlocksClickMapTest extends TestCase {

  private Key key1 = Key.create(DummyObject.TYPE, 1);
  private Key key2 = Key.create(DummyObject.TYPE, 2);
  private Key key3 = Key.create(DummyObject.TYPE, 3);

  public void test() throws Exception {
    HorizontalBlocksClickMap clickMap = new HorizontalBlocksClickMap();

    clickMap.reset(10, 50);
    clickMap.add(key1, 10);
    clickMap.add(key2, 20);
    clickMap.add(key3, 30);
    clickMap.complete(40);

    assertNull(clickMap.getKey(15,0));
    assertNull(clickMap.getKey(15,60));
    assertNull(clickMap.getKey(5,30));
    assertNull(clickMap.getKey(45,30));

    assertEquals(key1, clickMap.getKey(10, 10));
    assertEquals(key1, clickMap.getKey(15, 25));
    assertEquals(key1, clickMap.getKey(19, 50));

    assertEquals(key2, clickMap.getKey(20, 10));
    assertEquals(key2, clickMap.getKey(25, 25));
    assertEquals(key2, clickMap.getKey(29, 50));

    assertEquals(key3, clickMap.getKey(30, 10));
    assertEquals(key3, clickMap.getKey(35, 25));
    assertEquals(key3, clickMap.getKey(39, 50));
  }
}
