package org.designup.picsou.bank.connectors.labanquepostale;

import junit.framework.TestCase;

public class LaBanquePostaleConnectorTest extends TestCase {

  public void testLabelExtraction() throws Exception {
    assertEquals("VIREMENT DE MMR ARRCO",
                 LaBanquePostaleConnector.extractLabel("VIREMENT DE MMR ARRCO"));

    assertEquals("VIREMENT DE MMR ARRCO",
                 LaBanquePostaleConnector.extractLabel("<span>\n" +
                                                       "  VIREMENT DE MMR ARRCO\n" +
                                                       "</span>\n"));

    assertEquals("VIREMENT DE MMR ARRCO",
                 LaBanquePostaleConnector.extractLabel("<span>\n" +
                                                       "  VIREMENT DE MMR ARRCO\n" +
                                                       "  <br/>\n" +
                                                       "  123455\n" +
                                                       "  <br/>\n" +
                                                       "  RRCO OMNIREP VRP REF : 00112233\n" +
                                                       "</span>\n"));

    assertEquals("VIREMENT DE MMR ARRCO",
                 LaBanquePostaleConnector.extractLabel("<span>\n" +
                                                       "  VIREMENT   DE <b>MMR</b> \tARRCO\n" +
                                                       "</span>\n"));

  }
}