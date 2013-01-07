package org.designup.picsou.bank.connectors.bnp;

import junit.framework.TestCase;

public class BnpConnectorTest extends TestCase {

  public void testNumberExtraction() throws Exception {
    assertEquals("012345 000001234 89", BnpConnector.extractNumber("<td valign=\"top\">\n" +
                                                                   "number \n<br>012345&nbsp;000001234&nbsp;89\n</td>"));

    assertEquals("012345 000001234 89", BnpConnector.extractNumber("<td valign=\"top\">" +
                                                                   "number<br/>012345&nbsp;000001234&nbsp;89</td>"));
  }
}