package org.designup.picsou.bank.connectors.utils;

import junit.framework.TestCase;

public class WebTracesTest extends TestCase {

  public void testAnonymize() throws Exception {
    assertEquals("", WebTraces.anonymize(""));

    assertEquals("-99.99", WebTraces.anonymize("-1.23"));
    assertEquals("99.99", WebTraces.anonymize("123.45"));
    assertEquals("99.99", WebTraces.anonymize("1.123,45"));
    assertEquals("99.99", WebTraces.anonymize("1,123.45"));

    assertEquals("aa1bb", WebTraces.anonymize("aa1bb"));
    assertEquals("aa12bb", WebTraces.anonymize("aa12bb"));
    assertEquals("aa123bb", WebTraces.anonymize("aa123bb"));
    assertEquals("aa999bb", WebTraces.anonymize("aa1234bb"));
    assertEquals("aa999bb567bb999c", WebTraces.anonymize("aa1234bb567bb890123456c"));
  }
}