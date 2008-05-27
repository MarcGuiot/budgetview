package org.designup.picsou.functests.utils;

import org.crossbowlabs.globs.utils.Dates;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.utils.TestUtils;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.importer.qif.QifParser;

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

  public QifBuilder addTransaction(String label, double amount, String yyyyMMdd) throws IOException {
    writer.write("D");
    writer.write(QifParser.QIF_DATE_FORMAT.format(Dates.parse(yyyyMMdd)));
    writer.write(Strings.LINE_SEPARATOR);
    writer.write("T");
    writer.write(Double.toString(amount));
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

  public void load(Double balance) {
    operations.importQifFile(balance, fileName);
  }
}
