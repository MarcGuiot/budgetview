package org.designup.picsou.gui.utils;

import junit.framework.TestCase;
import org.globsframework.model.impl.DefaultGlobRepository;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.Key;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.Dates;
import org.designup.picsou.model.*;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.PicsouInit;
import org.designup.picsou.client.ServerAccess;

public class DataCheckerActionTest extends TestCase {
  private DefaultGlobRepository repository;
  private DefaultDirectory directory;

  protected void setUp() throws Exception {
    super.setUp();
    TimeService.setCurrentDate(Dates.parse("2009/10/10"));
    repository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    directory = new DefaultDirectory();
    TimeService timeService = new TimeService();
    directory.add(TimeService.class, timeService);
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
    createMonth(currentMonths);
    DataCheckerAction action = new DataCheckerAction(repository, directory);
    StringBuilder builder = new StringBuilder();
    assertTrue(action.doCheck(builder));
    String result = builder.toString();
    for (int month : missingMonths) {
      assertTrue(Integer.toString(month), result.contains("Missing month " + month));
    }
    for (int month : currentMonths) {
      assertFalse(Integer.toString(month), result.contains("Missing month " + month));
    }
    builder = new StringBuilder();
    boolean check = action.doCheck(builder);
    assertFalse(builder.toString(), check);
  }

  private void createMonth(final int... months) {
    for (int month : months) {
      repository.create(Key.create(Month.TYPE, month));
    }
  }

  public void testErrorOnSeriesBudget() throws Exception {
    createMonth(200906,200907,200908,200909,200910,200911,200912,201001,201002);
    repository.create(Series.TYPE, value(Series.NAME, "series name"),
                      value(Series.BUDGET_AREA, 0),
                      value(Series.FIRST_MONTH, 200906),
                      value(Series.LAST_MONTH, 201001),
                      value(Series.DAY, 1));
    doCheck("Adding SeriesBudget ");
  }

  public void testMissingSeries() throws Exception {
    createMonth(200906, 200907, 200908, 200909, 200910);
    repository.create(SeriesBudget.TYPE, value(SeriesBudget.ID, 123),
                      value(SeriesBudget.MONTH, 200907),
                      value(SeriesBudget.DAY, 1),
                      value(SeriesBudget.SERIES, 1233),
                      value(SeriesBudget.AMOUNT, 9.));
    doCheck("Missing series 1233");
  }

  public void testBadEndOfSeries() throws Exception {
    createMonth(200906, 200907, 200908, 200909, 200910);
    repository.create(Series.TYPE, value(Series.ID, 123),
                      value(Series.NAME, "telecom"),
                      value(Series.BUDGET_AREA, BudgetArea.ENVELOPES.getId()),
                      value(Series.FIRST_MONTH, 200905),
                      value(Series.LAST_MONTH, 200908));

    createTransaction(200909);

    createSeriesBudget(200907);
    doCheck("Adding SeriesBudget for");
  }

  public void testErrorOnSeriesBudgetBefore() throws Exception {
    createMonth(200906, 200907, 200908, 200909, 200910);
    repository.create(Series.TYPE, value(Series.ID, 123),
                      value(Series.NAME, "telecom"),
                      value(Series.BUDGET_AREA, BudgetArea.ENVELOPES.getId()),
                      value(Series.FIRST_MONTH, 200905),
                      value(Series.LAST_MONTH, 200908));

    createSeriesBudget(200905);
    createSeriesBudget(200911);
    doCheck("Deleting SeriesBudget", "Adding SeriesBudget");
  }

  public void testMissingSeriesBudgetAndMonth() throws Exception {
    createMonth(200906, 200907, 200908, 200910);
    repository.create(Series.TYPE, value(Series.ID, 123),
                      value(Series.NAME, "telecom"),
                      value(Series.BUDGET_AREA, BudgetArea.ENVELOPES.getId()),
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
                      value(SeriesBudget.AMOUNT, 9.));
  }

  private void createTransaction(final int monthId) {
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
                      value(Transaction.ACCOUNT, -1),
                      value(Transaction.TRANSACTION_TYPE, TransactionType.DEPOSIT.getId()));
  }

  private void doCheck(final String... expectedError) {
    PicsouInit.initTriggerRepository(ServerAccess.NULL, directory, repository);
    DataCheckerAction checkerAction = new DataCheckerAction(repository, directory);
    StringBuilder builder = new StringBuilder();
    assertTrue(checkerAction.doCheck(builder));
    String text = builder.toString();
    for (String error : expectedError) {
      assertTrue(text, text.contains(error));
    }
    StringBuilder output = new StringBuilder();
    boolean result = checkerAction.doCheck(output);
    assertFalse(output.toString(), result);
  }
}
