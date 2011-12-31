package com.budgetview.analytics;

import com.budgetview.analytics.model.User;
import junit.framework.TestCase;
import org.designup.picsou.functests.QifImportTest;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.globsframework.model.FieldValue.value;

public class AnalyticsTest extends TestCase {
  protected static final String DIRECTORY = File.separator + "testfiles" + File.separator;
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

  private GlobRepository repository;
  private GlobRepositoryChecker checker;

  public void setUp() throws Exception {
    repository = GlobRepositoryBuilder.createEmpty();
    checker = new GlobRepositoryChecker(repository);
  }

  public void test() throws Exception {

    load("log1.txt");

    Glob bernard = checker.findUnique(User.EMAIL, "bernard@wanadoo.fr");
    GlobPrinter.print(bernard);
    checker.checkFields(bernard,
                        value(User.FIRST_DATE, parseDate("20110923")),
                        value(User.LAST_DATE, parseDate("20111007")),
                        value(User.PURCHASE_DATE, parseDate("20110925")),
                        value(User.PING_COUNT, 8),
                        value(User.PREVIOUS_USER, false));
  }

  private Date parseDate(String text) throws ParseException {
    return DATE_FORMAT.parse(text);
  }

  private void load(String fileName) {
    Analytics.run(getReader(fileName), repository);
  }

  private InputStreamReader getReader(String fileNameToImport) {
    InputStream stream = QifImportTest.class.getResourceAsStream(DIRECTORY + fileNameToImport);
    if (stream == null) {
      throw new InvalidParameter("File '" + fileNameToImport + "' not found");
    }
    return new InputStreamReader(stream);
  }
}
