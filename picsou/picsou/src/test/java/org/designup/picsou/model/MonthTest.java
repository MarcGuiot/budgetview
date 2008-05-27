package org.designup.picsou.model;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class MonthTest extends TestCase {
  public void test() throws Exception {
    assertEquals(2006, Month.toYear(200601));
    assertEquals(1, Month.toMonth(200601));
  }

  public void testNormalize() throws Exception {
    assertEquals(200601, Month.normalize(200601));
    assertEquals(200612, Month.normalize(200612));
    assertEquals(200701, Month.normalize(200613));
    assertEquals(200801, Month.normalize(200625));
  }

  public void testNext() throws Exception {
    assertEquals(200602, Month.next(200601));
    assertEquals(200512, Month.next(200511));
    assertEquals(200701, Month.next(200612));
  }

  public void testPrevious() throws Exception {
    assertEquals(200601, Month.previous(200602));
    assertEquals(200511, Month.previous(200512));
    assertEquals(200612, Month.previous(200701));
  }

  public void testRange() throws Exception {
    Iterator<Integer> iter = Month.range(200611, 200702).iterator();
    assertEquals(200611, iter.next().intValue());
    assertEquals(200612, iter.next().intValue());
    assertEquals(200701, iter.next().intValue());
    assertEquals(200702, iter.next().intValue());
    assertFalse(iter.hasNext());
  }

  public void testRangeOutOfBonds() throws Exception {
    Iterator<Integer> iter = Month.range(200611, 200611).iterator();
    assertEquals(200611, iter.next().intValue());
    assertFalse(iter.hasNext());
    try {
      iter.next();
      fail();
    }
    catch (NoSuchElementException e) {
    }
  }
}
