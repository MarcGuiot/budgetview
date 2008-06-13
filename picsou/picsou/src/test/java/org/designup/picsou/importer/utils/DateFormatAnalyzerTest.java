package org.designup.picsou.importer.utils;

import junit.framework.TestCase;
import org.crossbowlabs.globs.utils.Dates;
import org.crossbowlabs.globs.utils.TestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
  }

  public void testUndecidable() throws Exception {
    checkUndecidable(new String[]{"dd/MM/yy", "MM/dd/yy", "yy/MM/dd"}, "01/01/01");
    checkUndecidable(new String[]{"dd/MM/yy", "MM/dd/yy", "yy/MM/dd"}, "02/03/04");
    checkUndecidable(new String[]{"dd/MM/yy", "MM/dd/yy"}, "04/10/2001");
    checkUndecidable(new String[]{"yy/MM/dd", "MM/dd/yy"}, "06/13/08");
  }

  private void check(String expected, String date) {
    check(date, expected, '/');
    check(date, expected, '-');
    check(date, expected, '.');
  }

  private void check(String date, String expected, char separator) {
    date = date.replace('/', separator);
    Set<String> list = new HashSet<String>(Arrays.asList(date));
    TestUtils.assertEquals(Collections.singletonList(expected), analyzer.parse(list));
  }

  private void checkUndecidable(String[] expected, String date) {
    TestUtils.assertSetEquals(Arrays.asList(expected), analyzer.parse(Collections.singleton(date)));
  }
}
