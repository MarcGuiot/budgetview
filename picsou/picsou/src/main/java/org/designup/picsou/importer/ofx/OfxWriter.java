package org.designup.picsou.importer.ofx;

import org.globsframework.model.format.Formats;
import org.globsframework.utils.Strings;

import java.io.IOException;
import java.io.Writer;
import java.util.UUID;

public class OfxWriter {
  private Writer writer;

  public OfxWriter(Writer writer) {
    this.writer = writer;
  }

  public void writeHeader() {
    write(getFileHeader());
  }

  public void writeFooter() {
    write(getFileFooter());
    try {
      writer.flush();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void writeLoadOp(String fromDate, String currentDate, String user, String password, final String org,
                          final String fid, final String bankId,
                          final String accountNumber, final String accountType){
    writeHeader(user, password, org, fid, currentDate);

    String str;
    if (bankId == null) {
      str = "<CREDITCARDMSGSRQV1>\n" +
            "<CCSTMTTRNRQ>\n" +
            "<TRNUID>" + UUID.randomUUID().toString()+ "\n" +
            "<CLTCOOKIE>1\n" +
            "<CCSTMTRQ>\n" +
            "<CCACCTFROM>\n" +
            "<ACCTID>" + accountNumber + "\n" +
            "</CCACCTFROM>\n" +
            "<INCTRAN>\n" +
            "<DTSTART>" + fromDate + "\n" +
            "<INCLUDE>Y\n" +
            "</INCTRAN>\n" +
            "</CCSTMTRQ>\n" +
            "</CCSTMTTRNRQ>\n" +
            "</CREDITCARDMSGSRQV1>\n" +
            "</OFX>";
    }
    else {
      str = "<BANKMSGSRQV1>\n" +
                   "<STMTTRNRQ>\n" +
                   "<TRNUID>" + UUID.randomUUID().toString()+ "\n" +
                   "<CLTCOOKIE>1\n" +
                   "<STMTRQ>\n" +
                   "<BANKACCTFROM>\n" +
                   "<BANKID>" + bankId + "\n" +
                   "<ACCTID>" + accountNumber + "\n" +
                   "<ACCTTYPE>" + (accountType == null ? "" : accountType) + "\n" +
                   "</BANKACCTFROM>\n" +
                   "<INCTRAN>\n" +
                   "<DTSTART>" + fromDate + "\n" +
                   "<INCLUDE>Y\n" +
                   "</INCTRAN>\n" +
                   "</STMTRQ>\n" +
                   "</STMTTRNRQ>\n" +
                   "</BANKMSGSRQV1>\n" +
                   "</OFX>";
    }
    write(str);
  }

  public void writeQuery(String user, String password, String currentDate, final String org, final String fid){
      writeHeader(user, password, org, fid, currentDate);
      write(
      "<SIGNUPMSGSRQV1>\n" +
      "<ACCTINFOTRNRQ>\n" +
      "<TRNUID>" + UUID.randomUUID().toString() + "\n" +
      "<CLTCOOKIE>1\n" +
      "<ACCTINFORQ>\n" +
      "<DTACCTUP>" + currentDate + "\n" +
      "</ACCTINFORQ>\n" +
      "</ACCTINFOTRNRQ>\n" +
      "</SIGNUPMSGSRQV1>\n" +
      "</OFX>");
  }

  private void writeHeader(String user, String password, String org, String fid, String currentDate) {
//    String applicationId = "Money"; //many institutions just won't work with an unrecognized app id...
//    String applicationVersion = "1600"; //many institutions just won't work with an unrecognized app id...
    String applicationId = "QWIN"; //many institutions just won't work with an unrecognized app id...
    String applicationVersion = "1800"; //many institutions just won't work with an unrecognized app id...
    write("OFXHEADER:100\n" +
    "DATA:OFXSGML\n" +
    "VERSION:102\n" +
    "SECURITY:NONE\n" +
    "ENCODING:USASCII\n" +
    "CHARSET:1252\n" +
    "COMPRESSION:NONE\n" +
    "OLDFILEUID:NONE\n" +
    "NEWFILEUID:" +  UUID.randomUUID().toString() + "\n\n" +
    "<OFX>\n" +
    "<SIGNONMSGSRQV1>\n" +
    "<SONRQ>\n" +
    "<DTCLIENT>" + currentDate + "\n" +
    "<USERID>" + user + "\n" +
    "<USERPASS>" + password + "\n" +
    "<LANGUAGE>ENG" +
    "<FI>\n" +
    "<ORG>" + org + "\n" +
    "<FID>" + fid + "\n" +
    "</FI>\n" +
    "<APPID>" + applicationId  +"\n" +
    "<APPVER>"  + applicationVersion  +"\n" +
    "</SONRQ>\n" +
    "</SIGNONMSGSRQV1>\n");
  }

  public void writeBankMsgHeader(String bankId, Integer branchId, String accountNumber) {
    write(
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
      "          <BANKID>" + bankId + "\n" +
      "          <BRANCHID>" + Strings.toString(branchId) + "\n" +
      "          <ACCTID>" + Strings.toString(accountNumber) + "\n" +
      "          <ACCTTYPE>CHECKING\n" +
      "        </BANKACCTFROM>\n" +
      "        <BANKTRANLIST>\n" +
      "          <DTSTART>20060131000000\n" +
      "          <DTEND>20060203000000\n");
  }

  public void writeBankMsgFooter(Double balance, String date) {
    write(
      "        </BANKTRANLIST>\n" +
      "        <LEDGERBAL>\n" +
      "          <BALAMT>" + (balance != null ? Formats.DEFAULT_DECIMAL_FORMAT.format(balance) : "0.00") + "\n" +
      "          <DTASOF>" + date + "\n" +
      "        </LEDGERBAL>\n" +
      "        <AVAILBAL>\n" +
      "          <BALAMT>0.0\n" +
      "          <DTASOF>20060704000000\n" +
      "        </AVAILBAL>\n" +
      "      </CCSTMTRS>\n" +
      "    </CCSTMTTRNRS>\n" +
      "  </BANKMSGSRSV1>\n");
  }

  public void writeCardMsgHeader(String accountNumber) {
    write(
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
      "      <ACCTID>" + accountNumber + "\n" +
      "     </CCACCTFROM>\n" +
      "     <BANKTRANLIST>\n" +
      "      <DTSTART>20060521000000\n" +
      "      <DTEND>20060711000000\n");
  }

  public void writeCardMsgFooter(Double accountBalance, String updateDate) {
    write(
      "     </BANKTRANLIST>\n" +
      "     <LEDGERBAL>\n" +
      "      <BALAMT>" + Formats.DEFAULT_DECIMAL_FORMAT.format(accountBalance) + "\n" +
      "      <DTASOF>" + updateDate + "\n" +
      "     </LEDGERBAL>\n" +
      "     <AVAILBAL>\n" +
      "      <BALAMT>0.0\n" +
      "      <DTASOF>20060704000000\n" +
      "     </AVAILBAL>\n" +
      "    </CCSTMTRS>\n" +
      "   </CCSTMTTRNRS>\n" +
      "  </CREDITCARDMSGSRSV1>\n");
  }

  private String getFileFooter() {
    return "</OFX>";
  }

  private String getFileHeader() {
    return
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
      "  </SIGNONMSGSRSV1>\n";
  }

  public OfxTransactionWriter startTransaction(String stringifiedUserDate,
                                               String stringifiedBankDate,
                                               Double amount,
                                               Integer transactionId,
                                               String label) {
    write("          <STMTTRN>\n" +
          "            <TRNTYPE>DEBIT\n" +
          "            <DTPOSTED>" + stringifiedBankDate + "\n" +
          "            <DTUSER>" + stringifiedUserDate + "\n" +
          "            <TRNAMT>" + Formats.DEFAULT_DECIMAL_FORMAT.format(amount) + "\n" +
          "            <FITID>PICSOU" + transactionId + "\n" +
          "            <NAME>" + label + "\n");
    return new OfxTransactionWriter();
  }

  private void write(String text) {
    try {
      writer.write(text);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public class OfxTransactionWriter {
    public OfxTransactionWriter add(String tag, String value) {
      if (!Strings.isNullOrEmpty(value)) {
        write("            <" + tag.toUpperCase() + ">" + value + "\n");
      }
      return this;
    }

    public void end() {
      write("          </STMTTRN>\n");
    }
  }
}
