package com.budgetview.functests.general;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.TransactionType;
import org.globsframework.utils.TestUtils;

public class SendImportedFileTest extends LoggedInFunctionalTestCase {

  public void testShow() throws Exception {
    operations.checkSendImportedFileDisabled();

    OfxBuilder.init(this)
      .addBankAccount("10101010", 1000.00, "2009/04/06")
      .addTransaction("2009/01/01", -29.00, "Free Telecom")
      .addTransaction("2009/02/02", -29.00, "FNAC")
      .addTransaction("2009/03/01", -125.00, "AUCHAN")
      .addTransaction("2009/04/05", -62.00, "EDF")
      .addTransaction("2009/04/06", 1111.00, "WorldCo")
      .load();

    String fileName = TestUtils.getFileName(this);
    operations.openSendImportedFile()
      .checkChoices("2008/08/31 - sendimportedfiletest_testshow_0.ofx")
      .select("2008/08/31 - sendimportedfiletest_testshow_0.ofx")
      .checkMessageContains("<STMTTRN>\n" +
                            "<TRNTYPE>DEBIT\n" +
                            "<DTPOSTED>20090202\n" +
                            "<DTUSER>20090202\n" +
                            "<TRNAMT>1000000\n" +
                            "<FITID>PICSOU101\n" +
                            "<NAME>lmno\n" +
                            "</STMTTRN>")
      .toggleObfuscate()
      .checkMessageContains("<STMTTRN>\n" +
                            "<TRNTYPE>DEBIT\n" +
                            "<DTPOSTED>20090202\n" +
                            "<DTUSER>20090202\n" +
                            "<TRNAMT>-29.00\n" +
                            "<FITID>PICSOU101\n" +
                            "<NAME>FNAC\n" +
                            "</STMTTRN>")
      .toggleObfuscate()
      .checkMessageContains("<STMTTRN>\n" +
                            "<TRNTYPE>DEBIT\n" +
                            "<DTPOSTED>20090202\n" +
                            "<DTUSER>20090202\n" +
                            "<TRNAMT>1000000\n" +
                            "<FITID>PICSOU101\n" +
                            "<NAME>lmno\n" +
                            "</STMTTRN>")
      .saveContentToFile(fileName)
      .close();

    mainAccounts.edit("Account n. 10101010").delete();
    timeline.selectAll();
    transactions.checkEmpty();

    operations.importOfxFile(fileName);
    timeline.selectAll();
    transactions.initContent()
      .add("06/04/2009", TransactionType.VIREMENT, "YZABCDE", "", 1000003.00)
      .add("05/04/2009", TransactionType.VIREMENT, "VWX", "", 1000002.00)
      .add("01/03/2009", TransactionType.VIREMENT, "PQRSTU", "", 1000001.00)
      .add("02/02/2009", TransactionType.VIREMENT, "LMNO", "", 1000000.00)
      .add("01/01/2009", TransactionType.VIREMENT, "ABCD EFGHIJK", "", 1000000.00)
      .check();
  }

  public void testCleanAfter5() throws Exception {
    for (int month = 1; month <= 9; month++) {
      OfxBuilder.init(this)
        .addTransaction("2009/0" + month + "/06", -29.00, "Free Telecom")
        .load();
    }

    operations.openSendImportedFile()
      .checkChoices("2008/08/31 - sendimportedfiletest_testcleanafter5_8.ofx",
                    "2008/08/31 - sendimportedfiletest_testcleanafter5_7.ofx",
                    "2008/08/31 - sendimportedfiletest_testcleanafter5_6.ofx",
                    "2008/08/31 - sendimportedfiletest_testcleanafter5_5.ofx",
                    "2008/08/31 - sendimportedfiletest_testcleanafter5_4.ofx",
                    "2008/08/31 - sendimportedfiletest_testcleanafter5_3.ofx")
      .close();
  }
}
