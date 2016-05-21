package com.budgetview.functests.banks;

import com.budgetview.functests.specificbanks.SpecificBankTestCase;
import org.globsframework.xml.XmlTestUtils;
import org.uispec4j.Clipboard;

public class BankFormatExportTest extends SpecificBankTestCase {

  public void testQif() throws Exception {
    callFailIfClipBoardDisable();
    operations.importQifFile(getFile("sg1.qif"), SOCIETE_GENERALE, 0.);
    timeline.selectAll();

    views.selectCategorization();
    categorization.selectTransactions(
      "REL. SACLAY",
      "STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS");

    categorization.copyBankFormatToClipboard();

    XmlTestUtils.assertEquals(
      "<bankFormat>\n" +
      "  <qifEntry originalLabel='CARTE 4973019606945793 22/04/06 REL. SACLAY' " +
      "            date='20060422' bankId='100' bankType=''" +
      "            m='CARTE 4973019606945793 22/04/06 REL. SACLAY' " +
      "            p='CARTE 4973019606945793'/>\n" +
      "  <qifEntry originalLabel='FAC.FRANCE 4561409787231717 20/04/06 STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS'" +
      "            date='20060420'  bankId='100' bankType=''" +
      "            m='FAC.FRANCE 4561409787231717 20/04/06 STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS' " +
      "            p='FAC.FRANCE 4561409'/>\n" +
      "  <bank bankId='4' id='100'/>" +
      "</bankFormat>",
      Clipboard.getContentAsText());
  }

  public void testOfx() throws Exception {
    callFailIfClipBoardDisable();
    operations.importOfxFile(getFile("banque_pop.ofx"));
    timeline.selectAll();

    views.selectCategorization();
    categorization.selectTransactions(
      "CHEQUE N°0004228",
      "RETRAIT GAB LONS SULLY 2 *123123123 A 10:24",
      "REMISE DE 1 CHEQUE");

    categorization.copyBankFormatToClipboard();

    XmlTestUtils.assertEquals(
      "<bankFormat>\n" +
      "  <ofxEntry originalLabel='CHEQUE N.0004228'" +
      "            date='20090211' bankId='100' bankType='DEBIT' " +
      "            name='CHEQUE'" +
      "            memo='001019640927' " +
      "            checkNum='0004228'/>\n" +
      "  <ofxEntry originalLabel='REMISE DE 1 CHEQUE'" +
      "            date='20090210'  bankId='100' bankType='CREDIT' " +
      "            name='DE 1 CHEQUE(S)' " +
      "            checkNum='9275893'/>\n" +
      "  <ofxEntry originalLabel='RET DAB GAB LONS SULLY 2 CARTE *123123123 RETRAIT LE 09/02/2009 A 10:24'" +
      "            date='20090209'  bankId='100' bankType='DEBIT' " +
      "            name='RET DAB GAB LONS SULLY 2'\n" +
      "            memo='CARTE *123123123 RETRAIT LE 09/02/2009 A 10:24' " +
      "            checkNum='2LV9570' />\n" +
      "  <bank bankEntityId='10807' bankEntityLabel='10807' bankId='12' id='100'/>" +
      "</bankFormat>",
      Clipboard.getContentAsText());
  }
}
