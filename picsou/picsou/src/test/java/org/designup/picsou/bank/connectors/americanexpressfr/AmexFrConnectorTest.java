package org.designup.picsou.bank.connectors.americanexpressfr;

import junit.framework.TestCase;

public class AmexFrConnectorTest extends TestCase {
  public void test() throws Exception {
    assertEquals("PRELEVEMENT AUTOMATIQUE ENREGISTRE-MERCI",
                 AmexFrConnector.cleanupLabel("<td class=\"desc\" style=\"padding-left:5px;text-align:left;\">\n" +
                                              "  <a id=\"TestHold\" href=\"javascript:togLayer('CardRoc00')\">\n" +
                                              "    <span class=\"plusImage\" id=\"imgCardRoc00\" title=\"Afficher les détails\">\n" +
                                              "       \n" +
                                              "    </span>\n" +
                                              "  </a>\n" +
                                              "  \n" +
                                              "\n" +
                                              "                                                            \n" +
                                              "                                                            PRELEVEMENT AUTOMATIQUE ENREGISTRE-MERCI\n" +
                                              "\n" +
                                              "                                                            \n" +
                                              "                                                                                    \n" +
                                              "  <div id=\"CardRoc00\" class=\"hiddenROC hiddenROCalign\">\n" +
                                              "    \n" +
                                              "                                            Enregistrée le : 15 oct. \n" +
                                              "                                            \n" +
                                              "  </div>\n" +
                                              "</td>"));
  }
}
