package org.designup.picsou.functests.banks;

import org.designup.picsou.functests.QifImportTest;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;

import java.io.File;
import java.io.IOException;

public abstract class SpecificBankTestCase extends LoggedInFunctionalTestCase {
  protected static final String DIRECTORY = File.separator + "testfiles" + File.separator;

  protected String getFile(String fileNameToImport) throws IOException {
    String fileExtension = ".ofx";
    if (fileNameToImport.endsWith(".qif")) {
      fileExtension = ".qif";
    }
    String fileName = TestUtils.getFileName(this, fileExtension);
    Files.copyStreamTofile(QifImportTest.class.getResourceAsStream(DIRECTORY + fileNameToImport), fileName);
    return fileName;
  }
}
