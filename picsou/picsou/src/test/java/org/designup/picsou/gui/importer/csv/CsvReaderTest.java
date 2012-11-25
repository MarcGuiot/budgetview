package org.designup.picsou.gui.importer.csv;

import junit.framework.TestCase;
import org.designup.picsou.importer.csv.CsvReader;
import org.globsframework.utils.TestUtils;

public class CsvReaderTest extends TestCase {

  public void testName() throws Exception {
    check(";az;RE", CsvSeparator.SEMICOLON, "", "az", "RE");
    check("az;RE", CsvSeparator.SEMICOLON, "az", "RE");
    check("az;RE;", CsvSeparator.SEMICOLON, "az", "RE", "");
  }

  private void check(final String line, CsvSeparator separator, String ...expected) {
    TestUtils.assertEquals(CsvReader.parseLine(line, separator), expected);
  }

  public void testDate() throws Exception {
    checkIsADate("30.24.1993");
    checkIsADate("30-24-1993");
    checkIsADate("30/24/1993");
    checkIsADate("30/24/93");
    checkIsADate("1000/12/01");
    checkIsNotADate("1/1/1");
    checkIsNotADate("1/1");
    checkIsNotADate("DFG");
    checkIsNotADate("345");
  }

  public void testNumber() throws Exception {
    checkIsANumber("34.3");
    checkIsANumber("4.3");
    checkIsANumber("4.3");
    checkIsANumber("3 4345");
    checkIsNotANumber("3sT r");
    checkIsNotANumber("sT 34r");
    checkIsNotANumber("sT34");
  }

  private void checkIsADate(final String fgd) {
    assertTrue(CsvReader.getTextType(fgd) == CsvReader.TextType.DATE);
  }

  private void checkIsNotADate(final String fgd) {
    assertTrue(CsvReader.getTextType(fgd) != CsvReader.TextType.DATE);
  }

  private void checkIsANumber(final String fgd) {
    assertTrue(CsvReader.getTextType(fgd) == CsvReader.TextType.NUMBER);
  }

  private void checkIsNotANumber(final String fgd) {
    assertTrue(CsvReader.getTextType(fgd) != CsvReader.TextType.NUMBER);
  }
}
