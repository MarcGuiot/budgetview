package org.designup.picsou.exporter.ofx;

import org.designup.picsou.utils.PicsouTestCase;

import java.io.IOException;
import java.io.StringWriter;

public class OfxExporterTest extends PicsouTestCase {
  
  public void test() throws Exception {
    checker.parse(
      repository,
      "<account id='-1' position='12345' positionDate='2006/07/02'/>" +
      "" +
      "<bankEntity id='30066'>" +
      "  <account number='00012312345' id='2' branchId='10674'" +
      "           position='1789.75' positionDate='2006/07/03'/>" +
      "  <account number='4976005004123456' id='3'" +
      "           position='-683.25' positionDate='2006/07/04' isCardAccount='true'/>" +
      "</bankEntity>" +
      "" +
      "<transaction id='1' month='200601' day='21'  bankMonth='200601' bankDay='24' amount='-1.1' " +
      "             originalLabel='label1' account='2'/>" +
      "<transaction id='2' month='200602' day='22' bankMonth='200602' bankDay='22' amount='-1.2' " +
      "             originalLabel='label2' account='2'" +
      "             note='my note'/>" +
      "<transaction id='3' month='200603' day='23' bankMonth='200603' bankDay='23' amount='-1.3' " +
      "             originalLabel='card3' transactionTypeName='credit_card' account='3'/>" +
      "<transaction id='4' month='200604' day='19' bankMonth='200604' bankDay='19' amount='-45' " +
      "             originalLabel='card4' transactionTypeName='credit_card' account='3'/>" +
      "" +
      "<transaction id='5' planned='true' month='200605' day='19' bankMonth='200605' bankDay='19' amount='-45' " +
      "             originalLabel='Planned: card4' transactionTypeName='credit_card' account='3'/>" +
      "" +
      "<transaction id='6' mirror='true' month='200605' day='19' bankMonth='200605' bankDay='19' amount='-45' " +
      "             originalLabel='Mirror: card4' transactionTypeName='credit_card' account='3'/>" +
      "");

    checkExport(true,
                "<OFX>\n" +
                "  <SIGNONMSGSRSV1>\n" +
                "    <SONRS>\n" +
                "      <STATUS>\n" +
                "        <CODE>0\n" +
                "        <SEVERITY>INFO\n" +
                "      </STATUS>\n" +
                "      <DTSERVER>20060716000000\n" +
                "      <LANGUAGE>FRA\n" +
                "    </SONRS>\n" +
                "  </SIGNONMSGSRSV1>\n" +
                "  <BANKMSGSRSV1>\n" +
                "    <STMTTRNRS>\n" +
                "      <TRNUID>20060716000000\n" +
                "      <STATUS>\n" +
                "        <CODE>0\n" +
                "        <SEVERITY>INFO\n" +
                "      </STATUS>\n" +
                "      <STMTRS>\n" +
                "        <CURDEF>EUR\n" +
                "        <BANKACCTFROM>\n" +
                "          <BANKID>30066\n" +
                "          <BRANCHID>10674\n" +
                "          <ACCTID>00012312345\n" +
                "          <ACCTTYPE>CHECKING\n" +
                "        </BANKACCTFROM>\n" +
                "        <BANKTRANLIST>\n" +
                "          <DTSTART>20060131000000\n" +
                "          <DTEND>20060203000000\n" +
                "          <STMTTRN>\n" +
                "            <TRNTYPE>DEBIT\n" +
                "            <DTPOSTED>20060124\n" +
                "            <DTUSER>20060121\n" +
                "            <TRNAMT>-1.10\n" +
                "            <FITID>PICSOU1\n" +
                "            <NAME>label1\n" +
                "          </STMTTRN>\n" +
                "          <STMTTRN>\n" +
                "            <TRNTYPE>DEBIT\n" +
                "            <DTPOSTED>20060222\n" +
                "            <DTUSER>20060222\n" +
                "            <TRNAMT>-1.20\n" +
                "            <FITID>PICSOU2\n" +
                "            <NAME>label2\n" +
                "            <NOTE>my note\n" +
                "          </STMTTRN>\n" +
                "        </BANKTRANLIST>\n" +
                "        <LEDGERBAL>\n" +
                "          <BALAMT>1789.75\n" +
                "          <DTASOF>20060703000000\n" +
                "        </LEDGERBAL>\n" +
                "        <AVAILBAL>\n" +
                "          <BALAMT>0.0\n" +
                "          <DTASOF>20060704000000\n" +
                "        </AVAILBAL>\n" +
                "      </CCSTMTRS>\n" +
                "    </CCSTMTTRNRS>\n" +
                "  </BANKMSGSRSV1>\n" +
                "  <CREDITCARDMSGSRSV1>\n" +
                "   <CCSTMTTRNRS>\n" +
                "    <TRNUID>20060716000000\n" +
                "    <STATUS>\n" +
                "     <CODE>0\n" +
                "     <SEVERITY>INFO\n" +
                "    </STATUS>\n" +
                "    <CCSTMTRS>\n" +
                "     <CURDEF>EUR\n" +
                "     <CCACCTFROM>\n" +
                "      <ACCTID>4976005004123456\n" +
                "     </CCACCTFROM>\n" +
                "     <BANKTRANLIST>\n" +
                "      <DTSTART>20060521000000\n" +
                "      <DTEND>20060711000000\n" +
                "          <STMTTRN>\n" +
                "            <TRNTYPE>DEBIT\n" +
                "            <DTPOSTED>20060323\n" +
                "            <DTUSER>20060323\n" +
                "            <TRNAMT>-1.30\n" +
                "            <FITID>PICSOU3\n" +
                "            <NAME>card3\n" +
                "          </STMTTRN>\n" +
                "          <STMTTRN>\n" +
                "            <TRNTYPE>DEBIT\n" +
                "            <DTPOSTED>20060419\n" +
                "            <DTUSER>20060419\n" +
                "            <TRNAMT>-45.00\n" +
                "            <FITID>PICSOU4\n" +
                "            <NAME>card4\n" +
                "          </STMTTRN>\n" +
                "     </BANKTRANLIST>\n" +
                "     <LEDGERBAL>\n" +
                "      <BALAMT>-683.25\n" +
                "      <DTASOF>20060704000000\n" +
                "     </LEDGERBAL>\n" +
                "     <AVAILBAL>\n" +
                "      <BALAMT>0.0\n" +
                "      <DTASOF>20060704000000\n" +
                "     </AVAILBAL>\n" +
                "    </CCSTMTRS>\n" +
                "   </CCSTMTTRNRS>\n" +
                "  </CREDITCARDMSGSRSV1>\n" +
                "</OFX>");

    checkExport(false,
                "<OFX>\n" +
                "  <SIGNONMSGSRSV1>\n" +
                "    <SONRS>\n" +
                "      <STATUS>\n" +
                "        <CODE>0\n" +
                "        <SEVERITY>INFO\n" +
                "      </STATUS>\n" +
                "      <DTSERVER>20060716000000\n" +
                "      <LANGUAGE>FRA\n" +
                "    </SONRS>\n" +
                "  </SIGNONMSGSRSV1>\n" +
                "  <BANKMSGSRSV1>\n" +
                "    <STMTTRNRS>\n" +
                "      <TRNUID>20060716000000\n" +
                "      <STATUS>\n" +
                "        <CODE>0\n" +
                "        <SEVERITY>INFO\n" +
                "      </STATUS>\n" +
                "      <STMTRS>\n" +
                "        <CURDEF>EUR\n" +
                "        <BANKACCTFROM>\n" +
                "          <BANKID>30066\n" +
                "          <BRANCHID>10674\n" +
                "          <ACCTID>00012312345\n" +
                "          <ACCTTYPE>CHECKING\n" +
                "        </BANKACCTFROM>\n" +
                "        <BANKTRANLIST>\n" +
                "          <DTSTART>20060131000000\n" +
                "          <DTEND>20060203000000\n" +
                "          <STMTTRN>\n" +
                "            <TRNTYPE>DEBIT\n" +
                "            <DTPOSTED>20060124\n" +
                "            <DTUSER>20060121\n" +
                "            <TRNAMT>-1.10\n" +
                "            <FITID>PICSOU1\n" +
                "            <NAME>label1\n" +
                "          </STMTTRN>\n" +
                "          <STMTTRN>\n" +
                "            <TRNTYPE>DEBIT\n" +
                "            <DTPOSTED>20060222\n" +
                "            <DTUSER>20060222\n" +
                "            <TRNAMT>-1.20\n" +
                "            <FITID>PICSOU2\n" +
                "            <NAME>label2\n" +
                "          </STMTTRN>\n" +
                "        </BANKTRANLIST>\n" +
                "        <LEDGERBAL>\n" +
                "          <BALAMT>1789.75\n" +
                "          <DTASOF>20060703000000\n" +
                "        </LEDGERBAL>\n" +
                "        <AVAILBAL>\n" +
                "          <BALAMT>0.0\n" +
                "          <DTASOF>20060704000000\n" +
                "        </AVAILBAL>\n" +
                "      </CCSTMTRS>\n" +
                "    </CCSTMTTRNRS>\n" +
                "  </BANKMSGSRSV1>\n" +
                "  <CREDITCARDMSGSRSV1>\n" +
                "   <CCSTMTTRNRS>\n" +
                "    <TRNUID>20060716000000\n" +
                "    <STATUS>\n" +
                "     <CODE>0\n" +
                "     <SEVERITY>INFO\n" +
                "    </STATUS>\n" +
                "    <CCSTMTRS>\n" +
                "     <CURDEF>EUR\n" +
                "     <CCACCTFROM>\n" +
                "      <ACCTID>4976005004123456\n" +
                "     </CCACCTFROM>\n" +
                "     <BANKTRANLIST>\n" +
                "      <DTSTART>20060521000000\n" +
                "      <DTEND>20060711000000\n" +
                "          <STMTTRN>\n" +
                "            <TRNTYPE>DEBIT\n" +
                "            <DTPOSTED>20060323\n" +
                "            <DTUSER>20060323\n" +
                "            <TRNAMT>-1.30\n" +
                "            <FITID>PICSOU3\n" +
                "            <NAME>card3\n" +
                "          </STMTTRN>\n" +
                "          <STMTTRN>\n" +
                "            <TRNTYPE>DEBIT\n" +
                "            <DTPOSTED>20060419\n" +
                "            <DTUSER>20060419\n" +
                "            <TRNAMT>-45.00\n" +
                "            <FITID>PICSOU4\n" +
                "            <NAME>card4\n" +
                "          </STMTTRN>\n" +
                "     </BANKTRANLIST>\n" +
                "     <LEDGERBAL>\n" +
                "      <BALAMT>-683.25\n" +
                "      <DTASOF>20060704000000\n" +
                "     </LEDGERBAL>\n" +
                "     <AVAILBAL>\n" +
                "      <BALAMT>0.0\n" +
                "      <DTASOF>20060704000000\n" +
                "     </AVAILBAL>\n" +
                "    </CCSTMTRS>\n" +
                "   </CCSTMTTRNRS>\n" +
                "  </CREDITCARDMSGSRSV1>\n" +
                "</OFX>");

  }

  private void checkExport(boolean exportCustomFields, String expected) throws IOException {
    StringWriter writer = new StringWriter();
    OfxExporter.write(repository, writer, exportCustomFields);
    writer.close();
    assertEquals(
      expected,
      writer.toString());
  }
}
