package org.designup.picsou.importer.utils;

import junit.framework.TestCase;
import org.globsframework.utils.Dates;
import org.globsframework.utils.TestUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateFormatAnalyzerTest extends TestCase {
  private DateFormatAnalyzer analyzer = new DateFormatAnalyzer(Dates.parse("2008/06/11"));

  public void testYyyyMmDd() throws Exception {
    check("yy/MM/dd", "2006/04/20");
    check("yy/MM/dd", "99/04/03");
  }

  public void testOfx() throws Exception {
    check("yyyyMMdd", "20060410");
  }

  public void testYearIsLessThanCurrentPlusOne() throws Exception {
    check("yy/MM/dd", "06/04/10");
    check("yy/MM/dd", "06/04/10");
  }

  public void testOnlyYearCanBeOnFourDigits() throws Exception {
    check("yy/MM/dd", "2001/04/10");
    check("dd/MM/yy", "13/11/2001");
  }

  public void testDdMmYyyy() throws Exception {
    check("dd/MM/yy", "20/04/2006");
    check("dd/MM/yy", "22/07/2006");
    check("dd/MM/yy", "20/03/99");
    check("dd/MM/yy", "24/07/06");
  }

  public void testMmDdYyyy() throws Exception {
    check("MM/dd/yy", "11/15/08");
    check("MM/dd/yy", "05/20/08");
  }

  public void testUndecidable() throws Exception {
    checkUndecidable(new String[]{"dd/MM/yy", "MM/dd/yy", "yy/MM/dd"}, "01/01/01");
    checkUndecidable(new String[]{"dd/MM/yy", "MM/dd/yy", "yy/MM/dd"}, "02/03/04");
    checkUndecidable(new String[]{"dd/MM/yy", "MM/dd/yy"}, "04/10/2001");
  }

  private void check(String expected, String date) throws ParseException {
    check(date, expected, '/');
    check(date, expected, '-');
    check(date, expected, '.');
  }

  private void check(String dateToCheck, String expected, char separator) throws ParseException {
    String date = dateToCheck.replace('/', separator);
    Set<String> list = new HashSet<String>(Arrays.asList(date));
    List<String> findFormat = analyzer.parse(list);
    TestUtils.assertEquals(Collections.singletonList(expected.replace('/', separator)), findFormat);
    assertEquals(new SimpleDateFormat(findFormat.get(0)).parse(date),
                 new SimpleDateFormat(expected).parse(dateToCheck));
  }

  private void checkUndecidable(String[] expected, String date) {
    TestUtils.assertSetEquals(Arrays.asList(expected), analyzer.parse(Collections.singleton(date)));
  }
}
