package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.MonthStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.PicsouTestCase;
import org.globsframework.metamodel.Field;

import java.util.Collections;

public class MonthStatComputerTest extends PicsouTestCase {
  public void testStandardCase() throws Exception {
    String input =
      "<series name='salary' budgetAreaName='income'/>" +
      "<series name='groceries' budgetAreaName='expensesEnvelope'/>" +
      "<account id='" + Account.SUMMARY_ACCOUNT_ID + "'/>" +
      "<transaction month='200605' day='1' amount='-20.0' categoryName='health'/>" +
      "<transaction month='200605' day='2' amount='-40.0' categoryName='health'/>" +
      "<transaction month='200605' day='2' amount='-400.0' categoryName='house'/>" +
      "<transaction month='200605' day='3' amount='600.0' categoryName='income'/>" +
      "" +
      "<transaction month='200606' day='1' amount='-100.0' categoryName='health'/>" +
      "<transaction month='200606' day='1' amount='20.0' categoryName='health'/>" +
      "<transaction month='200606' day='2' amount='-400.0' categoryName='house'/>" +
      "<transaction month='200606' day='3' amount='800.0' categoryName='income'/>" +
      "" +
      "<transaction month='200608' day='4' amount='10.0' categoryName='health'/>" +
      "<transaction month='200608' day='5' amount='-200.0' categoryName='health'/>" +
      "<transaction month='200608' day='6' amount='-410.0' categoryName='house'/>" +
      "<transaction month='200608' day='7' amount='700.0' categoryName='income'/>" +
      "<transaction month='200608' day='31' amount='900.0' categoryName='house' seriesName='groceries' planned='true'/>" +
      "<transaction month='200608' day='31' amount='1500.0' categoryName='income' seriesName='salary' planned='true'/>" +
      "";
    checker.parse(repository, input);

    updateStats();

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

    init(MonthStat.PLANNED_INCOME_RECEIVED)
      .setMonths(200605, 200606, 200607, 200608)
      .add(MasterCategory.INCOME, 0.0, 0.0, 0.0, 1500.0)
      .add(MasterCategory.ALL, 0.0, 0.0, 0.0, 1500.0)
      .check();
  }

  public void testDoesNothingWhenThereAreNoTransactions() throws Exception {
    updateStats();
    assertTrue(repository.getAll(MonthStat.TYPE).isEmpty());
  }

  public void testAddingMonths() throws Exception {
    checker.parse(repository,
                  "<transaction month='200605' day='1' amount='-20.0' categoryName='health'/>");
    updateStats();

    listener.reset();
    checker.parse(repository,
                  "<transaction month='200607' day='5' amount='-10.0'/>");
    updateStats();

    listener.assertLastChangesEqual(Month.TYPE,
                                    "<create type='month' id='200607'/>" +
                                    "<create type='month' id='200606'/>");
  }

  public void testTransactionsWithNoCategoryAreStoredWithCategoryNone() throws Exception {
    checker.parse(repository,
                  "<transaction month='200605' day='1' amount='-20.0'/>" +
                  "<transaction month='200605' day='10' amount='15.0'/>");
    updateStats();

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

  public void testManagesSubCategories() throws Exception {
    String input =
      "<account id='" + Account.SUMMARY_ACCOUNT_ID + "'/>" +
      "<category id='1000' name='doctor' masterName='health'/>" +
      "<category id='1001' name='pharma' masterName='health'/>" +
      "<category id='1002' name='reimbursements' masterName='health'/>" +
      "<transaction month='200605' day='1' amount='-10.0' categoryName='health'/>" +
      "<transaction month='200605' day='2' amount='-60.0' categoryName='doctor'/>" +
      "<transaction month='200605' day='2' amount='-50.0' categoryName='doctor'/>" +
      "<transaction month='200605' day='5' amount='90.0' categoryName='reimbursements'/>" +
      "" +
      "<transaction month='200606' day='1' amount='-60.0' categoryName='health'/>" +
      "<transaction month='200606' day='2' amount='-40.0' categoryName='pharma'/>" +
      "<transaction month='200606' day='5' amount='70.0' categoryName='reimbursements'/>" +
      "" +
      "<transaction month='200608' day='1' amount='-40.0' categoryName='health'/>" +
      "<transaction month='200608' day='2' amount='-20.0' categoryName='doctor'/>" +
      "<transaction month='200608' day='2' amount='-20.0' categoryName='doctor'/>" +
      "<transaction month='200608' day='2' amount='-20.0' categoryName='doctor'/>" +
      "<transaction month='200608' day='5' amount='80.0' categoryName='reimbursements'/>";
    checker.parse(repository, input);

    updateStats();

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

  public void testExcludesInternalTransfers() throws Exception {
    checker.parse(repository,
                  "<transaction month='200605' day='1' amount='-20.0' categoryName='health'/>" +
                  "<transaction month='200605' day='1' amount='-9000.0' categoryName='internal'/>" +
                  "<transaction month='200605' day='1' amount='15.0' categoryName='health'/>" +
                  "<transaction month='200605' day='1' amount='12000.0' categoryName='internal'/>" +
                  "<transaction month='200606' day='1' amount='600.0' categoryName='internal'/>");
    updateStats();

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

  public void testAccountManagement() throws Exception {
    checker.parse(repository,
                  "<account id='1'/>" +
                  "<transaction month='200605' day='1' amount='-10.0' account='1' categoryName='health'/>" +
                  "<transaction month='200605' day='2' amount='-20.0' account='1'/>" +
                  "<transaction month='200605' day='3' amount='-30.0' categoryName='health'/>" +
                  "<transaction month='200605' day='4' amount='-40.0' account='1'/>" +
                  "<transaction month='200605' day='5' amount='-50.0'/>" +
                  "<transaction month='200605' day='6' amount='100.0' account='1' categoryName='income'/>" +
                  "<transaction month='200605' day='7' amount='200.0' account='1'/>" +
                  "<transaction month='200605' day='7' amount='300.0' categoryName='income'/>"
    );
    updateStats();

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

  public void testKeepsInternalTransfersInAccountSpecificStats() throws Exception {
    checker.parse(repository,
                  "<account id='1'/>" +
                  "<transaction month='200605' day='1' amount='-10.0' account='1' categoryName='internal'/>" +
                  "<transaction month='200605' day='2' amount='-20.0' account='1'/>" +
                  "<transaction month='200605' day='6' amount='100.0' account='1' categoryName='internal'/>" +
                  "<transaction month='200605' day='7' amount='200.0' account='1'/>"
    );
    updateStats();

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

  public void testDispensability() throws Exception {
    String input =
      "<account id='" + Account.SUMMARY_ACCOUNT_ID + "'/>" +
      "<category id='1000' name='doctor' masterName='health'/>" +
      "<transaction month='200605' day='1' amount='-20.0' categoryName='health' dispensable='true'/>" +
      "<transaction month='200605' day='2' amount='-40.0' categoryName='health'/>" +
      "<transaction month='200605' day='2' amount='-400.0' categoryName='house'/>" +
      "<transaction month='200605' day='3' amount='-600.0' categoryName='house'/>" +
      "<transaction month='200605' day='4' amount='-100.0' categoryName='house' dispensable='true'/>" +
      "<transaction month='200605' day='5' amount='-50.0' categoryName='house' dispensable='true'/>" +
      "<transaction month='200606' day='25' amount='-30.0' categoryName='doctor' dispensable='true'/>";
    checker.parse(repository, input);

    updateStats();

    init(MonthStat.DISPENSABLE)
      .setMonths(200605, 200606)
      .add(MasterCategory.HEALTH, 20.0, 30.0)
      .add("doctor", 0.0, 30.0)
      .add(MasterCategory.HOUSE, 150.0, 0.0)
      .add(MasterCategory.ALL, 170.0, 30.0)
      .check();
  }

  private void updateStats() {
    MonthStatComputer computer = new MonthStatComputer(repository);
    computer.run(Collections.<Integer>emptySet());
  }

  private MonthStatChecker init(Field field) {
    return new MonthStatChecker(repository, field);
  }

}
