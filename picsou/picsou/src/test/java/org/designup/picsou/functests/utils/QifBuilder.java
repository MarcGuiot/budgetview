package org.designup.picsou.functests.utils;

import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TestUtils;

import java.io.FileWriter;
import java.io.IOException;

public class QifBuilder {
  private String fileName;
  private OperationChecker operations;
  private FileWriter writer;

  public static QifBuilder init(LoggedInFunctionalTestCase testCase) throws Exception {
    return new QifBuilder(TestUtils.getFileName(testCase, ".qif"), testCase.getOperations());
  }

  private QifBuilder(String fileName, OperationChecker operations) throws Exception {
    this.operations = operations;
    this.fileName = fileName;
    writer = new FileWriter(fileName);
    writer.write("!Type:Bank");
    writer.write(Strings.LINE_SEPARATOR);
  }

  public QifBuilder addTransaction(String yyyyMMdd, double amount, String label) throws IOException {
    writer.write("D");
    writer.write(yyyyMMdd);
    writer.write(Strings.LINE_SEPARATOR);
    writer.write("T");
    writer.write(PicsouDescriptionService.DECIMAL_FORMAT.format(amount));
    writer.write(Strings.LINE_SEPARATOR);
    writer.write("N");
    writer.write(Strings.LINE_SEPARATOR);
    writer.write("P" + label);
    writer.write(Strings.LINE_SEPARATOR);
    writer.write("M" + label);
    writer.write(Strings.LINE_SEPARATOR);
    writer.write("^");
    writer.write(Strings.LINE_SEPARATOR);
    return this;
  }

  public String save() throws IOException {
    writer.close();
    return fileName;
  }

  public void load(Double balance) {
    operations.importQifFile(fileName, "Société Générale");
  }
}
