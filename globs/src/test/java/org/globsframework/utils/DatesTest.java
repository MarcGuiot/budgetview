package org.globsframework.utils;

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;

public class DatesTest extends TestCase {

  public void testParse() throws Exception {
    String dateAsAsString = "2002/10/25";
    Date date = Dates.parse(dateAsAsString);
    assertEquals(dateAsAsString, Dates.DEFAULT_DATE_FORMAT.format(date));
  }

  public void testParseTimestamp() throws Exception {
    String dateAsAsString = "2002/10/25 00:50:12";
    Date date = Dates.parseTimestamp(dateAsAsString);
    assertEquals(dateAsAsString, Dates.DEFAULT_TIMESTAMP_FORMAT.format(date));
  }

  public void testMillisBetween() throws Exception {
    assertEquals(5000, Dates.millisBetween(Dates.parseTimestamp("2002/10/03 12:34:20"),
                                           Dates.parseTimestamp("2002/10/03 12:34:25")));
    assertEquals(5000, Dates.millisBetween(toCalendar(Dates.parseTimestamp("2002/10/03 12:34:20")),
                                           toCalendar(Dates.parseTimestamp("2002/10/03 12:34:25"))));
  }

  public void testNear() throws Exception {
    assertTrue(Dates.isNear(Dates.parseTimestamp("2002/10/03 12:34:20"),
                            Dates.parseTimestamp("2002/10/03 12:34:25"),
                            5001));

    assertFalse(Dates.isNear(Dates.parseTimestamp("2002/10/03 12:34:20"),
                             Dates.parseTimestamp("2002/10/03 12:34:25"),
                             4999));

    assertTrue(Dates.isNear(toCalendar(Dates.parseTimestamp("2002/10/03 12:34:20")),
                            toCalendar(Dates.parseTimestamp("2002/10/03 12:34:25")),
                            5001));

    assertFalse(Dates.isNear(toCalendar(Dates.parseTimestamp("2002/10/03 12:34:20")),
                             toCalendar(Dates.parseTimestamp("2002/10/03 12:34:25")),
                             4999));
  }

  private Calendar toCalendar(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    return calendar;
  }
}
