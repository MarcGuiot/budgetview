package com.budgetview.analytics.utils;

import junit.framework.TestCase;
import org.joda.time.DateTime;

public class WeeksTest extends TestCase {

  public void testPrevious() throws Exception {
    assertEquals(201151, Weeks.previous(201152));
    assertEquals(201152, Weeks.previous(201201));
  }

  public void testNext() throws Exception {
    assertEquals(201152, Weeks.next(201151));
    assertEquals(201201, Weeks.next(201152));
  }

  public void testFirstDay() throws Exception {
    DateTime last = new DateTime(Weeks.getFirstDay(201206));
    assertEquals(2, last.getMonthOfYear());
    assertEquals(6, last.getDayOfMonth());
    assertEquals(2012, last.getYear());
  }

  public void testLastDay() throws Exception {
    DateTime last = new DateTime(Weeks.getLastDay(201206));
    assertEquals(2, last.getMonthOfYear());
    assertEquals(12, last.getDayOfMonth());
    assertEquals(2012, last.getYear());
  }
}
