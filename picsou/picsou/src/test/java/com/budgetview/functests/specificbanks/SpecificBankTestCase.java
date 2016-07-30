package com.budgetview.functests.specificbanks;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.importexport.QifImportTest;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

public abstract class SpecificBankTestCase extends LoggedInFunctionalTestCase {
  protected static final String DIRECTORY = File.separator + "testfiles" + File.separator;

  protected String getFile(String fileNameToImport) throws IOException {
    return getFile(fileNameToImport, this);
  }

  public static String getFile(String fileNameToImport, TestCase test) throws IOException {
    String fileExtension = ".ofx";
    if (fileNameToImport.endsWith(".qif")) {
      fileExtension = ".qif";
    }
    if (fileNameToImport.endsWith(".csv")){
      fileExtension = ".csv";
    }
    String fileName = TestUtils.getFileName(test, fileExtension);
    InputStream stream = QifImportTest.class.getResourceAsStream(DIRECTORY + fileNameToImport);
    if (stream == null) {
      throw new InvalidParameter("File '" + fileNameToImport + "' not found");
    }
    Files.copyStreamTofile(stream, fileName);
    return fileName;
  }
}
