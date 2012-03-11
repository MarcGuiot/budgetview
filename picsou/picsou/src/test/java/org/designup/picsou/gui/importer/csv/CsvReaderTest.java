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
}
