package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.checkers.SendImportedFileChecker;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

import java.io.ByteArrayInputStream;

public class SendImportedFileTest extends LoggedInFunctionalTestCase {

  public void testShow() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2009/01/01", -29.00, "Free Telecom")
      .addTransaction("2009/02/02", -29.00, "Free Telecom")
      .addTransaction("2009/03/01", -29.00, "Free Telecom")
      .addTransaction("2009/04/06", -29.00, "Free Telecom")
      .load();

    Ref <String> fileContent = new Ref<String>();
    SendImportedFileChecker importedFileChecker = operations.openSendImportedFile();
    importedFileChecker.checkChoice("2008/08/31:100:sendimportedfiletest_testshow_0.ofx")
      .select("2008/08/31:100:sendimportedfiletest_testshow_0.ofx")
      .checkContentContain("OFX")
      .getContent(fileContent)
      .close();
    String s = TestUtils.getFileName(this);
    Files.copyStreamTofile(new ByteArrayInputStream(fileContent.get().getBytes("UTF-8")), s);
    operations.importOfxFile(s);
  }

  public void testCleanAfter5() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2009/04/06", -29.00, "Free Telecom")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2009/05/06", -29.00, "Free Telecom")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2009/06/06", -29.00, "Free Telecom")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2009/07/06", -29.00, "Free Telecom")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2009/08/06", -29.00, "Free Telecom")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2009/09/06", -29.00, "Free Telecom")
      .load();

    SendImportedFileChecker importedFileChecker = operations.openSendImportedFile();
    importedFileChecker.checkChoice("2008/08/31:101:sendimportedfiletest_testcleanafter5_1.ofx", 
                                    "2008/08/31:102:sendimportedfiletest_testcleanafter5_2.ofx",
                                    "2008/08/31:103:sendimportedfiletest_testcleanafter5_3.ofx",
                                    "2008/08/31:104:sendimportedfiletest_testcleanafter5_4.ofx",
                                    "2008/08/31:105:sendimportedfiletest_testcleanafter5_5.ofx")
      .close();
  }
}
