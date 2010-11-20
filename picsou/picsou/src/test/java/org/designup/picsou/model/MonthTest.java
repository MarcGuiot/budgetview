package org.designup.picsou.model;

import junit.framework.TestCase;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.Dates;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Date;

public class MonthTest extends TestCase {
  public void test() throws Exception {
    assertEquals(2006, Month.toYear(200601));
    assertEquals(1, Month.toMonth(200601));
  }

  public void testNext() throws Exception {
    assertEquals(200602, Month.next(200601));
    assertEquals(200512, Month.next(200511));
    assertEquals(200701, Month.next(200612));

    assertEquals(200703, Month.next(200610, 5));
    assertEquals(200712, Month.next(200612, 12));
    assertEquals(200901, Month.next(200612, 25));
  }

  public void testPrevious() throws Exception {
    assertEquals(200601, Month.previous(200602));
    assertEquals(200511, Month.previous(200512));
    assertEquals(200612, Month.previous(200701));

    assertEquals(200910, Month.previous(201001, 3));
    assertEquals(200810, Month.previous(201001, 15));
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

  public void testToInt() throws Exception {
    assertEquals(20080618, Month.toInt(200806, 18));
    assertEquals(200806, Month.intToMonthId(20080618));
    assertEquals(18, Month.intToDay(20080618));
  }

  public void testCreateMonth() throws Exception {
    TestUtils.assertEquals(Month.createMonths(200807, 200810), 200807, 200808, 200809, 200810);
  }

  public void testCreateMonthWithFirst() throws Exception {
    TestUtils.assertEquals(Month.createCountMonthsWithFirst(200807, 3), 200807, 200808, 200809, 200810);
  }

  public void testDistance() throws Exception {
    assertEquals(4, Month.distance(200811, 200903));
    assertEquals(13, Month.distance(200811, 200912));
    assertEquals(-4, Month.distance(200903, 200811));
    assertEquals(-13, Month.distance(200912, 200811));
  }

  public void testaddDurationMonth() throws Exception {
    shift("2010/11/10", "2010/12/26");
    shift("2010/12/10", "2011/01/25");
  }

  private void shift(final String from, final String to) {
    Date date = Month.addDurationMonth(Dates.parse(from));
    assertEquals(Dates.parse(to), date);
  }

}
