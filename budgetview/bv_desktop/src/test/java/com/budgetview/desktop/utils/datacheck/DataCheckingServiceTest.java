package com.budgetview.desktop.utils.datacheck;

import com.budgetview.model.*;
import junit.framework.TestCase;
import com.budgetview.client.DataAccess;
import com.budgetview.desktop.PicsouInit;
import com.budgetview.desktop.description.PicsouDescriptionService;
import com.budgetview.desktop.time.TimeService;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.Dates;
import org.globsframework.utils.directory.DefaultDirectory;

import static org.globsframework.model.FieldValue.value;

public class DataCheckingServiceTest extends TestCase {
  private DefaultGlobRepository repository;
  private DefaultDirectory directory;

  protected void setUp() throws Exception {
    super.setUp();
    TimeService.setCurrentDate(Dates.parse("2009/10/10"));
    repository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    directory = new DefaultDirectory();
    TimeService timeService = new TimeService();
    directory.add(TimeService.class, timeService);
    directory.add(DescriptionService.class, new PicsouDescriptionService());
    repository.create(CurrentMonth.KEY,
                      value(CurrentMonth.LAST_TRANSACTION_MONTH, timeService.getCurrentMonthId()),
                      value(CurrentMonth.LAST_TRANSACTION_DAY, 1));
    repository.create(Account.MAIN_SUMMARY_KEY);
    repository.create(Account.ALL_SUMMARY_KEY);
    repository.create(Account.SAVINGS_SUMMARY_KEY);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    repository = null;
    directory = null;
  }

  public void testMissingMonth() throws Exception {
    checkMissingMonth(new int[]{200911},200910, 200912, 201001, 201002);
  }

  private void checkMissingMonth(int[] missingMonths, int... currentMonths) {
    createMonths(currentMonths);

    DataCheckingService action = new DataCheckingService(repository, directory);
    DataCheckReport report = new DataCheckReport();
    assertTrue(action.doCheck(report));
    String result = report.toString();
    for (int month : missingMonths) {
      assertTrue(Integer.toString(month), result.contains("Missing month " + month));
    }
    for (int month : currentMonths) {
      assertFalse(Integer.toString(month), result.contains("Missing month " + month));
    }
    report.clear();
    boolean check = action.doCheck(report);
    assertFalse(report.toString(), check);
  }

  private void createMonths(final int... months) {
    for (int month : months) {
      repository.create(Key.create(Month.TYPE, month));
    }
  }

  public void testErrorOnSeriesBudget() throws Exception {
    createMonths(200906,200907,200908,200909,200910,200911,200912,201001,201002);
    repository.create(Series.TYPE, value(Series.NAME, "series name"),
                      value(Series.BUDGET_AREA, 0),
                      value(Series.FIRST_MONTH, 200906),
                      value(Series.LAST_MONTH, 201001),
                      value(Series.DAY, 1));
    doCheck("Adding SeriesBudget ");
  }

  public void testMissingSeries() throws Exception {
    createMonths(200906, 200907, 200908, 200909, 200910);
    repository.create(SeriesBudget.TYPE, value(SeriesBudget.ID, 123),
                      value(SeriesBudget.MONTH, 200907),
                      value(SeriesBudget.DAY, 1),
                      value(SeriesBudget.SERIES, 1233),
                      value(SeriesBudget.PLANNED_AMOUNT, 9.));
    doCheck("Missing series 1233");
  }

  public void testBadEndOfSeries() throws Exception {
    createMonths(200906, 200907, 200908, 200909, 200910);
    repository.create(Series.TYPE, value(Series.ID, 123),
                      value(Series.NAME, "telecom"),
                      value(Series.BUDGET_AREA, BudgetArea.VARIABLE.getId()),
                      value(Series.FIRST_MONTH, 200905),
                      value(Series.LAST_MONTH, 200908));

    createTransaction(200909);

    createSeriesBudget(200907);
    doCheck("Adding SeriesBudget for");
  }

  public void testErrorOnSeriesBudgetBefore() throws Exception {
    createMonths(200906, 200907, 200908, 200909, 200910);
    repository.create(Series.TYPE, value(Series.ID, 123),
                      value(Series.NAME, "telecom"),
                      value(Series.BUDGET_AREA, BudgetArea.VARIABLE.getId()),
                      value(Series.FIRST_MONTH, 200905),
                      value(Series.LAST_MONTH, 200908));

    createSeriesBudget(200905);
    createSeriesBudget(200911);
    doCheck("Deleting SeriesBudget", "Adding SeriesBudget");
  }

  public void testMissingSeriesBudgetAndMonth() throws Exception {
    createMonths(200906, 200907, 200908, 200910);
    repository.create(Series.TYPE, value(Series.ID, 123),
                      value(Series.NAME, "telecom"),
                      value(Series.BUDGET_AREA, BudgetArea.VARIABLE.getId()),
                      value(Series.FIRST_MONTH, 200905),
                      value(Series.LAST_MONTH, 2009010));

    createSeriesBudget(200905);
    createSeriesBudget(200911);
    doCheck("Deleting SeriesBudget", "Adding SeriesBudget", "Missing month 200909");
  }

  private void createSeriesBudget(final int monthId) {
    repository.create(SeriesBudget.TYPE,
                      value(SeriesBudget.MONTH, monthId),
                      value(SeriesBudget.DAY, 1),
                      value(SeriesBudget.ACTIVE, true),
                      value(SeriesBudget.SERIES, 123),
                      value(SeriesBudget.PLANNED_AMOUNT, 9.));
  }

  private void createTransaction(final int monthId) {
    repository.findOrCreate(Key.create(Account.TYPE, 1),
                            value(Account.NAME, "Account"),
                            value(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()));

    repository.create(Transaction.TYPE, value(Transaction.SERIES, 123),
                      value(Transaction.MONTH, monthId),
                      value(Transaction.DAY, 1),
                      value(Transaction.BUDGET_MONTH, monthId),
                      value(Transaction.BUDGET_DAY, 1),
                      value(Transaction.BANK_MONTH, monthId),
                      value(Transaction.BANK_DAY, 1),
                      value(Transaction.POSITION_MONTH, monthId),
                      value(Transaction.POSITION_DAY, 1),
                      value(Transaction.AMOUNT, 100.),
                      value(Transaction.ACCOUNT, 1),
                      value(Transaction.TRANSACTION_TYPE, TransactionType.DEPOSIT.getId()));
  }

  private void doCheck(final String... expectedError) {
    PicsouInit.initTriggers(DataAccess.NULL, directory, repository);
    DataCheckingService checkerAction = new DataCheckingService(repository, directory);
    DataCheckReport builder = new DataCheckReport();
    assertTrue(checkerAction.doCheck(builder));
    String text = builder.toString();
    for (String error : expectedError) {
      assertTrue(text, text.contains(error));
    }
    DataCheckReport output = new DataCheckReport();
    boolean result = checkerAction.doCheck(output);
    assertFalse(output.toString(), result);
  }
}
