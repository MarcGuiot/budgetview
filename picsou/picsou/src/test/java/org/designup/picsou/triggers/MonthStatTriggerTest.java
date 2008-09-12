package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.PicsouTestCase;
import org.globsframework.metamodel.Field;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Key;

public class MonthStatTriggerTest extends PicsouTestCase {
  public void testStandardCaseReset() throws Exception {
    checkResetOrChangeSet(true);
  }

  public void testStandartCaseChangeSet() throws Exception {
    checkResetOrChangeSet(false);
  }

  public void checkResetOrChangeSet(boolean reset) throws Exception {
    String input =
      "<series name='salary' budgetAreaName='income'/>" +
      "<series name='groceries' budgetAreaName='expensesEnvelope'/>" +
      "<account id='" + Account.SUMMARY_ACCOUNT_ID + "'/>" +
      "<month id='200605'/>" +
      "<month id='200606'/>" +
      "<month id='200607'/>" +
      "<month id='200608'/>" +
      "<transaction month='200605' day='1' amount='-20.0' categoryName='health'/>" +
      "<transaction month='200605' day='2' amount='-40.0' categoryName='health'/>" +
      "<transaction month='200605' day='2' amount='-400.0' categoryName='house'/>" +
      "<transaction month='200605' day='3' amount='600.0' categoryName='income'/>" +
      "" +
      "<transaction month='200606' day='1' amount='-100.0' categoryName='health'/>" +
      "<transaction month='200606' day='1' amount='20.0' categoryName='health'/>" +
      "<transaction month='200606' day='2' amount='-400.0' categoryName='house'/>" +
      "<transaction month='200606' day='3' amount='800.0' categoryName='income'/>" +
      "<transaction month='200606' day='31' amount='1500.0' categoryName='income' seriesName='salary' planned='true'/>" +
      "" +
      "<transaction month='200608' day='4' amount='10.0' categoryName='health'/>" +
      "<transaction month='200608' day='5' amount='-200.0' categoryName='health'/>" +
      "<transaction month='200608' day='6' amount='-410.0' categoryName='house'/>" +
      "<transaction month='200608' day='7' amount='700.0' categoryName='income'/>" +
      "<transaction month='200608' day='31' amount='900.0' categoryName='house' seriesName='groceries' planned='true'/>" +
      "<transaction month='200608' day='31' amount='1500.0' categoryName='income' seriesName='salary' planned='true'/>" +
      "";

    if (reset) {
      checker.parse(repository, input);
      updateStats();
    }
    else {
      MonthStatTrigger trigger = new MonthStatTrigger();
      repository.addChangeListener(trigger);
      repository.enterBulkDispatchingMode();
      checker.parse(repository, input);
      repository.completeBulkDispatchingMode();
    }
    init(MonthStat.TOTAL_SPENT)
      .setMonths(200605, 200606, 200607, 200608)
      .add(MasterCategory.HEALTH, 60.0, 100.0, 0.0, 200.0)
      .add(MasterCategory.HOUSE, 400.0, 400.0, 0.0, 410.0)
      .add(MasterCategory.ALL, 460.0, 500.0, 0.0, 610.0)
      .check();

    init(MonthStat.TOTAL_RECEIVED)
      .setMonths(200605, 200606, 200607, 200608)
      .add(MasterCategory.INCOME, 600.0, 800.0, 0.0, 700.0)
      .add(MasterCategory.HEALTH, 0.0, 20.0, 0.0, 10.0)
      .add(MasterCategory.ALL, 600.0, 820.0, 0.0, 710.0)
      .check();

  }

  public void testDeleteTransaction() throws Exception {
    checker.parse(repository,
                  "<month id='200605'/>" +
                  "<transaction id='1' month='200605' day='1' amount='-20.0'/>" +
                  "<transaction id='2' month='200605' day='10' amount='15.0' categoryName='food'/>" +
                  "<transaction id='3' month='200605' day='10' amount='35.0'/>" +
                  "");
    updateStats();
    MonthStatTrigger trigger = new MonthStatTrigger();
    repository.addChangeListener(trigger);
    repository.enterBulkDispatchingMode();
    repository.delete(Key.create(Transaction.TYPE, 3));
    repository.completeBulkDispatchingMode();

    init(MonthStat.TOTAL_SPENT)
      .setMonths(200605)
      .add(MasterCategory.NONE, 20.0)
      .add(MasterCategory.ALL, 20.0)
      .check();

    init(MonthStat.TOTAL_RECEIVED)
      .setMonths(200605)
      .add(MasterCategory.NONE, 0.0)
      .add(MasterCategory.FOOD, 15.0)
      .add(MasterCategory.ALL, 15.0)
      .check();
  }

  public void testDoesNothingWhenThereAreNoTransactions() throws Exception {
    updateStats();
    assertTrue(repository.getAll(MonthStat.TYPE).isEmpty());
  }

  public void testTransactionsWithNoCategoryAreStoredWithCategoryNoneReset() throws Exception {
    checkTransactionsWithNoCategoryAreStoredWithCategoryNone(true);
  }

  public void testTransactionsWithNoCategoryAreStoredWithCategoryNoneChangeSet() throws Exception {
    checkTransactionsWithNoCategoryAreStoredWithCategoryNone(false);
  }

  public void checkTransactionsWithNoCategoryAreStoredWithCategoryNone(boolean isReset) throws Exception {
    checker.parse(repository,
                  "<month id='200605'/>" +
                  "<transaction id='1' month='200605' day='1' amount='-20.0'/>" +
                  "<transaction id='2' month='200605' day='10' amount='15.0' categoryName='food'/>");
    if (isReset) {
      repository.update(Key.create(Transaction.TYPE, 2), Transaction.CATEGORY, null);
      updateStats();
    }
    else {
      updateStats();
      MonthStatTrigger trigger = new MonthStatTrigger();
      repository.addChangeListener(trigger);
      repository.enterBulkDispatchingMode();
      repository.update(Key.create(Transaction.TYPE, 2), Transaction.CATEGORY, null);
      repository.completeBulkDispatchingMode();
    }

    init(MonthStat.TOTAL_SPENT)
      .setMonths(200605)
      .add(MasterCategory.NONE, 20.0)
      .add(MasterCategory.ALL, 20.0)
      .check();

    init(MonthStat.TOTAL_RECEIVED)
      .setMonths(200605)
      .add(MasterCategory.NONE, 15.0)
      .add(MasterCategory.ALL, 15.0)
      .check();
  }

  public void testManagesSubCategoriesReset() throws Exception {
    checkManagesSubCategories(true);
  }

  public void testManagesSubCategoriesChangeSet() throws Exception {
    checkManagesSubCategories(false);
  }

  public void checkManagesSubCategories(boolean isReset) throws Exception {
    String input =
      "<account id='" + Account.SUMMARY_ACCOUNT_ID + "'/>" +
      "<month id='200605'/>" +
      "<month id='200606'/>" +
      "<month id='200607'/>" +
      "<month id='200608'/>" +
      "<category id='1000' name='doctor' innerName='doctor' masterName='health'/>" +
      "<category id='1001' name='pharma' innerName='pharma' masterName='health'/>" +
      "<category id='1002' name='reimbursements' innerName='reimbursements' masterName='health'/>" +
      "<transaction id='1' month='200605' day='1' amount='-10.0' categoryName='health'/>" +
      "<transaction id='2' month='200605' day='2' amount='-60.0' categoryName='reimbursements'/>" +
      "<transaction id='3' month='200605' day='2' amount='-50.0' categoryName='doctor'/>" +
      "<transaction id='4' month='200605' day='5' amount='90.0' categoryName='reimbursements'/>" +
      "" +
      "<transaction id='5' month='200606' day='1' amount='-60.0' categoryName='health'/>" +
      "<transaction id='6' month='200606' day='2' amount='-20.0' categoryName='food'/>" +
      "<transaction id='7' month='200606' day='5' amount='70.0' categoryName='reimbursements'/>" +
      "" +
      "<transaction id='8' month='200608' day='1' amount='-40.0' categoryName='health'/>" +
      "<transaction id='9' month='200608' day='2' amount='-20.0' categoryName='doctor'/>" +
      "<transaction id='10' month='200608' day='2' amount='-20.0' categoryName='doctor'/>" +
      "<transaction id='11' month='200608' day='2' amount='-20.0' categoryName='doctor'/>" +
      "<transaction id='12' month='200608' day='5' amount='80.0' categoryName='reimbursements'/>";

    checker.parse(repository, input);
    if (isReset) {
      repository.update(Key.create(Transaction.TYPE, 2), Transaction.CATEGORY, 1000);
      repository.update(Key.create(Transaction.TYPE, 6),
                        FieldValue.value(Transaction.CATEGORY, 1001),
                        FieldValue.value(Transaction.AMOUNT, -40.)
      );
      updateStats();
    }
    else {
      updateStats();
      MonthStatTrigger trigger = new MonthStatTrigger();
      repository.addChangeListener(trigger);
      repository.enterBulkDispatchingMode();
      repository.update(Key.create(Transaction.TYPE, 2), Transaction.CATEGORY, 1000);
      repository.update(Key.create(Transaction.TYPE, 6),
                        FieldValue.value(Transaction.CATEGORY, 1001),
                        FieldValue.value(Transaction.AMOUNT, -40.)
      );

      repository.completeBulkDispatchingMode();
    }

    init(MonthStat.TOTAL_SPENT)
      .setMonths(200605, 200606, 200607, 200608)
      .add(MasterCategory.HEALTH, 120.0, 100.0, 0.0, 100.0)
      .add("doctor", 110.0, 0.0, 0.0, 60.0)
      .add("pharma", 0.0, 40.0, 0.0, 0.0)
      .add(MasterCategory.ALL, 120.0, 100.0, 0.0, 100.0)
      .check();

    init(MonthStat.TOTAL_RECEIVED)
      .setMonths(200605, 200606, 200607, 200608)
      .add(MasterCategory.HEALTH, 90.0, 70.0, 0.0, 80.0)
      .add("reimbursements", 90.0, 70.0, 0.0, 80.0)
      .add(MasterCategory.ALL, 90.0, 70.0, 0.0, 80.0)
      .check();

  }

  public void testExcludesInternalTransfersReset() throws Exception {
    checkExcludesInternalTransfers(true);
  }

  public void testExcludesInternalTransfersChangeSet() throws Exception {
    checkExcludesInternalTransfers(false);
  }

  public void checkExcludesInternalTransfers(boolean isReset) throws Exception {
    checker.parse(repository,
                  "<month id='200605'/>" +
                  "<month id='200606'/>" +
                  "<transaction id='1' month='200605' day='1' amount='-20.0' categoryName='health'/>" +
                  "<transaction id='2' month='200605' day='1' amount='-9000.0' categoryName='food'/>" +
                  "<transaction id='3' month='200605' day='1' amount='45.0' categoryName='internal'/>" +
                  "<transaction id='4' month='200605' day='1' amount='12000.0' categoryName='internal'/>" +
                  "<transaction id='5' month='200606' day='1' amount='600.0' categoryName='internal'/>");

    if (isReset) {
      repository.update(Key.create(Transaction.TYPE, 2), Transaction.CATEGORY, MasterCategory.INTERNAL.getId());
      repository.update(Key.create(Transaction.TYPE, 3),
                        FieldValue.value(Transaction.CATEGORY, MasterCategory.HEALTH.getId()),
                        FieldValue.value(Transaction.AMOUNT, 15.)
      );
      updateStats();
    }
    else {
      updateStats();
      MonthStatTrigger trigger = new MonthStatTrigger();
      repository.addChangeListener(trigger);
      repository.enterBulkDispatchingMode();
      repository.update(Key.create(Transaction.TYPE, 2), Transaction.CATEGORY, MasterCategory.INTERNAL.getId());
      repository.update(Key.create(Transaction.TYPE, 3),
                        FieldValue.value(Transaction.CATEGORY, MasterCategory.HEALTH.getId()),
                        FieldValue.value(Transaction.AMOUNT, 15.)
      );
      repository.completeBulkDispatchingMode();
    }


    init(MonthStat.TOTAL_SPENT)
      .setMonths(200605, 200606)
      .add(MasterCategory.HEALTH, 20.0, 0.0)
      .add(MasterCategory.INTERNAL, 9000.0, 0.0)
      .add(MasterCategory.ALL, 20.0, 0.0)
      .check();

    init(MonthStat.TOTAL_RECEIVED)
      .setMonths(200605, 200606)
      .add(MasterCategory.HEALTH, 15.0, 0.0)
      .add(MasterCategory.INTERNAL, 12000.0, 600.0)
      .add(MasterCategory.ALL, 15.0, 0.0)
      .check();
  }

  public void testAccountManagementReset() throws Exception {
    checkAccountManagement(true);
  }

  public void testAccountManagementChangeSet() throws Exception {
    checkAccountManagement(false);
  }


  public void checkAccountManagement(boolean isReset) throws Exception {

    checker.parse(repository,
                  "<account id='1'/>" +
                  "<month id='200605'/>" +
                  "<account id='" + Account.SUMMARY_ACCOUNT_ID + "'/>" +
                  "<account id='1'/>" +
                  "<transaction id='1' month='200605' day='1' amount='-40.0' account='1' categoryName='health'/>" +
                  "<transaction id='2' month='200605' day='2' amount='-30.0' account='1'/>" +
                  "<transaction id='3' month='200605' day='3' amount='-30.0' categoryName='health'/>" +
                  "<transaction id='4' month='200605' day='4' amount='-40.0' account='1'/>" +
                  "<transaction id='5' month='200605' day='5' amount='-50.0'/>" +
                  "<transaction id='6' month='200605' day='6' amount='100.0' account='1' categoryName='income'/>" +
                  "<transaction id='7' month='200605' day='7' amount='200.0' account='1'/>" +
                  "<transaction id='8' month='200605' day='7' amount='300.0' categoryName='income'/>"
    );
    if (isReset) {
      repository.update(Key.create(Transaction.TYPE, 1), Transaction.AMOUNT, -10.);
      repository.update(Key.create(Transaction.TYPE, 2), Transaction.AMOUNT, -20.);
      updateStats();
    }
    else {
      updateStats();
      MonthStatTrigger trigger = new MonthStatTrigger();
      repository.addChangeListener(trigger);
      repository.enterBulkDispatchingMode();
      repository.update(Key.create(Transaction.TYPE, 1), Transaction.AMOUNT, -10.);
      repository.update(Key.create(Transaction.TYPE, 2), Transaction.AMOUNT, -20.);

      repository.completeBulkDispatchingMode();
    }

    init(MonthStat.TOTAL_SPENT)
      .setMonths(200605)
      .add(MasterCategory.HEALTH, 40.0)
      .add(MasterCategory.NONE, 110.0)
      .add(MasterCategory.ALL, 150.0)
      .check();

    init(MonthStat.TOTAL_SPENT)
      .setAccount(1)
      .setMonths(200605)
      .add(MasterCategory.HEALTH, 10.0)
      .add(MasterCategory.NONE, 60.0)
      .add(MasterCategory.ALL, 70.0)
      .check();

    init(MonthStat.TOTAL_RECEIVED)
      .setMonths(200605)
      .add(MasterCategory.INCOME, 400.0)
      .add(MasterCategory.NONE, 200.0)
      .add(MasterCategory.ALL, 600.0)
      .check();

    init(MonthStat.TOTAL_RECEIVED)
      .setAccount(1)
      .setMonths(200605)
      .add(MasterCategory.INCOME, 100.0)
      .add(MasterCategory.NONE, 200.0)
      .add(MasterCategory.ALL, 300.0)
      .check();
  }

  public void testKeepsInternalTransfersInAccountSpecificStatsReset() throws Exception {
    checkKeepsInternalTransfersInAccountSpecificStats(true);
  }

  public void testKeepsInternalTransfersInAccountSpecificStatsChangeSet() throws Exception {
    checkKeepsInternalTransfersInAccountSpecificStats(false);
  }

  public void checkKeepsInternalTransfersInAccountSpecificStats(boolean reset) throws Exception {
    checker.parse(repository,
                  "<account id='1'/>" +
                  "<month id='200605'/>" +
                  "<transaction id='1' month='200605' day='1' amount='-10.0' account='1' categoryName='food'/>" +
                  "<transaction id='2' month='200605' day='2' amount='-20.0' account='1'/>" +
                  "<transaction id='3' month='200605' day='6' amount='100.0' account='1'/>" +
                  "<transaction id='4' month='200605' day='7' amount='200.0' account='1'/>"
    );
    if (reset) {
      repository.update(Key.create(Transaction.TYPE, 1), Transaction.CATEGORY, MasterCategory.INTERNAL.getId());
      repository.update(Key.create(Transaction.TYPE, 3), Transaction.CATEGORY, MasterCategory.INTERNAL.getId());
      updateStats();
    }
    else {
      updateStats();
      MonthStatTrigger trigger = new MonthStatTrigger();
      repository.addChangeListener(trigger);
      repository.enterBulkDispatchingMode();
      repository.update(Key.create(Transaction.TYPE, 1), Transaction.CATEGORY, MasterCategory.INTERNAL.getId());
      repository.update(Key.create(Transaction.TYPE, 3), Transaction.CATEGORY, MasterCategory.INTERNAL.getId());
      repository.completeBulkDispatchingMode();
    }

    init(MonthStat.TOTAL_SPENT)
      .setMonths(200605)
      .add(MasterCategory.INTERNAL, 10.0)
      .add(MasterCategory.NONE, 20.0)
      .add(MasterCategory.ALL, 20.0)
      .check();

    init(MonthStat.TOTAL_RECEIVED)
      .setMonths(200605)
      .add(MasterCategory.INTERNAL, 100.0)
      .add(MasterCategory.NONE, 200.0)
      .add(MasterCategory.ALL, 200.0)
      .check();

    init(MonthStat.TOTAL_SPENT)
      .setAccount(1)
      .setMonths(200605)
      .add(MasterCategory.INTERNAL, 10.0)
      .add(MasterCategory.NONE, 20.0)
      .add(MasterCategory.ALL, 30.0)
      .check();

    init(MonthStat.TOTAL_RECEIVED)
      .setAccount(1)
      .setMonths(200605)
      .add(MasterCategory.INTERNAL, 100.0)
      .add(MasterCategory.NONE, 200.0)
      .add(MasterCategory.ALL, 300.0)
      .check();
  }

  private void updateStats() {
    MonthStatTrigger trigger = new MonthStatTrigger();
    repository.enterBulkDispatchingMode();
    trigger.run(repository);
    repository.completeBulkDispatchingMode();
  }

  private MonthStatChecker init(Field field) {
    return new MonthStatChecker(repository, field);
  }

}
