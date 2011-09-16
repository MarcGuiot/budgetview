package org.designup.picsou.importer.ofx;

import junit.framework.TestCase;

import java.io.StringWriter;
import java.io.StringReader;
import java.util.List;

import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.GlobList;
import org.designup.picsou.model.RealAccount;

public class OfxConnectionTest extends TestCase {

  public void test() throws Exception {
    StringWriter stringWriter = new StringWriter();
    OfxWriter writer = new OfxWriter(stringWriter);
    writer.writeQuery("user", "password", "0", "0");
    String request = stringWriter.toString()
      .replaceAll("NEWFILEUID:.*\n", "NEWFILEUID:123\n")
      .replaceAll("<TRNUID>.*\n", "<TRNUID>321\n");
    assertEquals("OFXHEADER:100\n" +
                 "DATA:OFXSGML\n" +
                 "VERSION:102\n" +
                 "SECURITY:NONE\n" +
                 "ENCODING:USASCII\n" +
                 "CHARSET:1252\n" +
                 "COMPRESSION:NONE\n" +
                 "OLDFILEUID:NONE\n" +
                 "NEWFILEUID:123\n" +
                 "\n" +
                 "<OFX>\n" +
                 "<SIGNONMSGSRQV1>\n" +
                 "<SONRQ>\n" +
                 "<DTCLIENT>20110704000000\n" +
                 "<USERID>user\n" +
                 "<USERPASS>password\n" +
                 "<LANGUAGE>ENG<FI>\n" +
                 "<ORG>0\n" +
                 "<FID>0\n" +
                 "</FI>\n" +
                 "<APPID>Money\n" +
                 "<APPVER>1600\n" +
                 "</SONRQ>\n" +
                 "</SIGNONMSGSRQV1>\n" +
                 "<SIGNUPMSGSRQV1>\n" +
                 "<ACCTINFOTRNRQ>\n" +
                 "<TRNUID>321\n" +
                 "<CLTCOOKIE>1\n" +
                 "<ACCTINFORQ>\n" +
                 "<DTACCTUP>20110830000000\n" +
                 "</ACCTINFORQ>\n" +
                 "</ACCTINFOTRNRQ>\n" +
                 "</SIGNUPMSGSRQV1>\n" +
                 "</OFX>", request);
  }

  public void testParseResult() throws Exception {
    String str = "<OFX><SIGNONMSGSRSV1><SONRS><STATUS><CODE>0<SEVERITY>INFO</STATUS>" +
                 "<DTSERVER>20110807114113<LANGUAGE>ENG</SONRS></SIGNONMSGSRSV1>" +
                 "<SIGNUPMSGSRSV1><ACCTINFOTRNRS><TRNUID>0517cad5-a984-447c-b11d-84773eaa7754" +
                 "<STATUS><CODE>0<SEVERITY>INFO</STATUS><CLTCOOKIE>1<ACCTINFORS><DTACCTUP>20110807" +
                 "<ACCTINFO><DESC>MME XXXX<BANKACCTINFO><BANKACCTFROM><BANKID>20041<BRANCHID>01014<ACCTID>12345678" +
                 "<ACCTTYPE>CHECKING<ACCTKEY>01</BANKACCTFROM><SUPTXDL>Y<XFERSRC>N<XFERDEST>N<SVCSTATUS>ACTIVE</BANKACCTINFO></ACCTINFO>" +
                 "<ACCTINFO><DESC>MME XXXXX<BANKACCTINFO><BANKACCTFROM><BANKID>10011<BRANCHID>00020<ACCTID>54321<ACCTTYPE>SAVINGS<ACCTKEY>04" +
                 "</BANKACCTFROM><SUPTXDL>Y<XFERSRC>N<XFERDEST>N<SVCSTATUS>ACTIVE" +
                 "</BANKACCTINFO></ACCTINFO></ACCTINFORS></ACCTINFOTRNRS></SIGNUPMSGSRSV1></OFX>";
    OfxParser parser = new OfxParser();
    DefaultGlobRepository repository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    AccountInfoOfxFunctor accountInfoOfxFunctor = new AccountInfoOfxFunctor();
    parser.parse(new StringReader(str), accountInfoOfxFunctor);
    List<OfxConnection.AccountInfo> accounts = accountInfoOfxFunctor.getAccounts();
    assertEquals(2, accounts.size());
    assertEquals("12345678", accounts.get(0).number);
    assertEquals("54321", accounts.get(1).number);
  }
}
