package com.budgetview.analytics.checker;

import com.budgetview.analytics.Analytics;
import com.budgetview.analytics.model.User;
import com.budgetview.analytics.model.UserProgressInfoEntry;
import com.budgetview.analytics.model.WeekStats;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.designup.picsou.functests.importexport.QifImportTest;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryChecker;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.io.*;
import java.util.Date;

public class AnalyticsChecker {
  private TestCase test;
  private GlobRepository repository;
  private GlobRepositoryChecker checker;

  protected static final String DIRECTORY = File.separator + "testfiles" + File.separator;

  public AnalyticsChecker(TestCase test, GlobRepository repository) {
    this.test = test;
    this.repository = repository;
    this.checker = new GlobRepositoryChecker(repository);
  }

  public void load(String fileName) {
    Analytics.run(getReader(fileName), repository);
  }

  public void checkUser(String email, FieldValue... values) {
    doCheck(User.TYPE, User.EMAIL, email, values);
  }

  public void checkUseInfo(Date date, FieldValue... values) {
    doCheck(UserProgressInfoEntry.TYPE, UserProgressInfoEntry.DATE, date, values);
  }

  public void checkWeekPerf(int weekId, FieldValue... values) {
    doCheck(WeekStats.TYPE, WeekStats.ID, weekId, values);
  }

  private void doCheck(GlobType type, Field field, Object value, FieldValue[] values) {
    Glob user = checker.doFindUnique(field, value);
    if (user == null) {
      GlobPrinter.print(repository, type);
      Assert.fail("No " + type.getName() + " found with value: " + value);
    }
    checker.checkFields(user, values);
  }

  private InputStreamReader getReader(String fileNameToImport) {
    InputStream stream = QifImportTest.class.getResourceAsStream(DIRECTORY + fileNameToImport);
    if (stream == null) {
      throw new InvalidParameter("File '" + fileNameToImport + "' not found");
    }
    return new InputStreamReader(stream);
  }

  public DummyServerLogBuilder createLog() throws IOException {
    return new DummyServerLogBuilder(TestUtils.getFileName(test));
  }

  public class DummyServerLogBuilder {
    private FileWriter writer;
    private String fileName;

    private DummyServerLogBuilder(String fileName) throws IOException {
      this.fileName = fileName;
      this.writer = new FileWriter(fileName);
    }

    public DummyServerLogBuilder logNewAnonymous(String date, String repoId) throws IOException {
      writeLogLine("INFO " + date + " 01:28:32,306 - thread 120 msg : new_anonymous ip = 82.225.65.17 id =" + repoId);
      return this;
    }

    public DummyServerLogBuilder logKnownAnonymous(String date, String repoId) throws IOException {
      writeLogLine("INFO " + date + " 16:41:44,864 - thread 28 msg : known_anonymous ip = 2.6.151.71 id =" + repoId + " access_count = 31");
      return this;
    }

    public DummyServerLogBuilder logOkForMail(String date, String address, String repoId, int count) throws IOException {
      writeLogLine("INFO " + date + " 10:17:16,171 - thread 16 msg : compute_license ip = 88.164.73.52 mail = " + address + " count = 151 id = " + repoId + " code = 9005");
      writeLogLine("INFO " + date + " 16:24:05,641 - thread 16 msg : ok_for mail = " + address + "count = " + count);
      return this;
    }

    public DummyServerLogBuilder logOldUseInfo(String date, int count,
                                               boolean initialStepsCompleted,
                                               boolean g,
                                               boolean i,
                                               boolean j,
                                               boolean k,
                                               boolean l,
                                               boolean m) throws IOException {
      writeLogLine("INFO " + date + " 12:24:31,096 - use info = use: " + count + ", " +
                   "initialStepsCompleted: " + initialStepsCompleted + ", " +
                   "g: " + g + ", " +
                   "i: " + i + " , " +
                   "j: " + j + " , " +
                   "k: " + k + " , " +
                   "l: " + l + " , " +
                   "m: " + m + " ");
      return this;
    }

    public DummyServerLogBuilder logUseInfo(String date, int count,
                                            boolean initialStepsCompleted,
                                            boolean importStarted,
                                            boolean categorizationSelectionDone,
                                            boolean categorizationAreaSelectionDone,
                                            boolean firstCategorizationDone,
                                            boolean categorizationSkipped,
                                            boolean gotoBudgetShown) throws IOException {
      writeLogLine("INFO " + date + " 12:24:31,096 - use info = use: " + count + ", " +
                   "initialStepsCompleted: " + initialStepsCompleted + ", " +
                   "importStarted: " + importStarted + ", " +
                   "categorizationSelectionDone: " + categorizationSelectionDone + " , " +
                   "categorizationAreaSelectionDone: " + categorizationAreaSelectionDone + " , " +
                   "firstCategorizationDone: " + firstCategorizationDone + " , " +
                   "categorizationSkipped: " + categorizationSkipped + " , " +
                   "gotoBudgetShown: " + gotoBudgetShown + " ");
      return this;
    }

    public DummyServerLogBuilder logPurchase(String date, String email) throws IOException {
      writeLogLine("INFO " + date + " 10:03:56,065 - receive new User  :");
      writeLogLine("INFO " + date + " 10:03:56,072 - NewUser : mail : '" + email);
      writeLogLine("INFO " + date + " 10:03:56,105 - item_number=''; residence_country='FR'; shipping_method='Default'; shipping_discount='0.00'; insurance_amount='0.00'; verify_sign='AiKZhEEPLJjSIccz.2M.tbyW5YFwAogOwwn6hFr2r7eNMOUys8ifgwbo'; payment_status='Completed'; business='paypal@mybudgetview.fr'; protection_eligibility='Ineligible'; transaction_subject='Code d'activation BudgetView'; payer_id='8RUUNCADFXSWW'; first_name='Bernard'; shipping='0.00'; payer_email='" + email + "'; mc_fee='1.27'; btn_id='20331125'; txn_id='9WF31892AN672923R'; receiver_email='paypal@mybudgetview.fr'; quantity='1'; notify_version='3.4'; txn_type='web_accept'; mc_gross='29.90'; payer_status='unverified'; mc_currency='EUR'; custom=''; payment_date='00:16:57 Sep 25, 2011 PDT'; payment_fee=''; charset='UTF-8'; payment_gross=''; ipn_track_id='rL229rvcmQ2BGs7vDxFjVA'; discount='0.00'; tax='4.90'; handling_amount='0.00'; item_name='Code d'activation BudgetView'; last_name='Aiglehoux'; payment_type='instant'; receiver_id='RQLKPV2F4ZR74';");
      writeLogLine("INFO " + date + " 10:03:57,259 - NewUser : mail : '" + email + " VERIFIED");
      writeLogLine("INFO " + date + " 10:03:57,302 - NewUser : ok  for " + email + " code is 1594");
      writeLogLine("INFO " + date + " 10:03:57,425 - mail sent : " + email + "  Votre code d'activation BudgetView");
      writeLogLine("INFO " + date + " 10:03:57,433 - mail sent : support@mybudgetview.fr  New User");
      return this;
    }

    public DummyServerLogBuilder logUserEvaluation(String date, boolean value) throws IOException {
      writeLogLine("INFO " + date + " 14:31:32,217 - mail sent : support@mybudgetview.fr  User evaluation: " + Strings.toYesNo(value));
      writeLogLine("title User evaluation: " + Strings.toYesNo(value));
      return this;
    }

    private void writeLogLine(String line) throws IOException {
      writer.write(line);
      writer.write('\n');
    }

    public void load() throws IOException {
      writer.close();
      FileReader reader = new FileReader(fileName);
      Analytics.run(reader, repository);
      reader.close();
    }
  }
}
