package org.designup.picsou.importer.ofx;

import junit.framework.TestCase;
import org.designup.picsou.gui.time.TimeService;
import org.globsframework.utils.Dates;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class OfxParserTest extends TestCase {
  public static final String TEXT =
    "OFXHEADER:100\n" +
    "DATA:OFXSGML\n" +
    "VERSION:102\n" +
    "SECURITY:NONE\n" +
    "ENCODING:USASCII\n" +
    "CHARSET:1252\n" +
    "COMPRESSION:NONE\n" +
    "OLDFILEUID:NONE\n" +
    "NEWFILEUID:NONE\n" +
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
    "          <ACCTID>00010063701\n" +
    "          <ACCTTYPE>CHECKING\n" +
    "        </BANKACCTFROM>\n" +
    "        <BANKTRANLIST>\n" +
    "          <DTSTART>20060131000000\n" +
    "          <DTEND>20060203000000\n" +
    "          <STMTTRN>\n" +
    "            <TRNTYPE>DEBIT\n" +
    "            <DTPOSTED>20060131\n" +
    "            <DTUSER>20060131\n" +
    "            <TRNAMT>-21.53\n" +
    "            <FITID>LOIB4G3LLF\n" +
    "            <NAME>DROITS DE GARDE 1 SEM. 2006 3006\n" +
    "          </STMTTRN>\n" +
    "        </BANKTRANLIST>\n" +
    "        <LEDGERBAL>\n" +
    "          <BALAMT>-683.25\n" +
    "          <DTASOF>20060704000000\n" +
    "        </LEDGERBAL>\n" +
    "        <AVAILBAL>\n" +
    "          <BALAMT>0.0\n" +
    "          <DTASOF>20060704000000\n" +
    "        </AVAILBAL>\n" +
    "      </CCSTMTRS>\n" +
    "    </CCSTMTTRNRS>\n" +
    "  </CREDITCARDMSGSRSV1>\n" +
    "</OFX>\n";

  private static final String TEXT_WITH_NO_HEADER =
    "<OFX>\n" +
    "  <SIGNONMSGSRSV1>\n" +
    "    <SONRS>\n" +
    "      <STATUS>\n" +
    "        <CODE>0\n" +
    "        <SEVERITY>INFO\n" +
    "      </STATUS>\n" +
    "      <DTSERVER>20060815180446\n" +
    "      <LANGUAGE>FRA\n" +
    "    </SONRS>\n" +
    "  </SIGNONMSGSRSV1>\n" +
    "</OFX>\n";

  public void testStandardCase() throws Exception {
    checkParsing(TEXT);
  }

  public void testWithIntAccount() throws Exception {
    checkParsing("INTACCOUNT:1\n" +
                 "OFXHEADER:100\n" +
                 "DATA:OFX\n" +
                 "SGMLVERSION:102\n" +
                 "SECURITY:NONE\n" +
                 "ENCODING:USASCII\n" +
                 "CHARSET:1252\n" +
                 "COMPRESSION:NONE\n" +
                 "OLDFILEUID:NONE\n" +
                 "NEWFILEUID:NONE\n" +
                 "<OFX>\n" +
                 "  <SIGNONMSGSRSV1>\n" +
                 "    <SONRS>\n" +
                 "      <STATUS>\n" +
                 "        <CODE>2000\n" +
                 "        <SEVERITY>ERROR\n" +
                 "      </STATUS>\n" +
                 "      <DTSERVER>20110808155841\n" +
                 "      <LANGUAGE>ENG\n" +
                 "    </SONRS>\n" +
                 "  </SIGNONMSGSRSV1>\n" +
                 "  <BANKMSGSRSV1>\n" +
                 "    <STMTTRNRS>\n" +
                 "      <TRNUID>20110807140944.000\n" +
                 "      <STATUS>\n" +
                 "        <CODE>2000\n" +
                 "        <SEVERITY>ERROR\n" +
                 "      </STATUS>\n" +
                 "    </STMTTRNRS>\n" +
                 "  </BANKMSGSRSV1>\n" +
                 "</OFX>\n");
  }

  public void testWithNoHeader() throws Exception {
    checkParsing(TEXT_WITH_NO_HEADER);
  }

  public void testOneLine() throws Exception {
    String text = TEXT_WITH_NO_HEADER.replace("\n", "");
    StringWriter writer = new StringWriter();
    OfxBeautifier functor = new OfxBeautifier(writer);
    OfxParser parser = new OfxParser();
    parser.parse(new StringReader(text), functor);
    assertEquals(TEXT_WITH_NO_HEADER, writer.toString());
  }

  private void checkParsing(String text) throws IOException {
    StringWriter writer = new StringWriter();
    OfxBeautifier functor = new OfxBeautifier(writer);
    OfxParser parser = new OfxParser();
    parser.parse(new StringReader(text), functor);
    assertEquals(text, writer.toString());
  }
}
