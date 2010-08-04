package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BankEntity;

public class SeriesReportTest extends LoggedInFunctionalTestCase {

  public void test() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(3)
      .validate();
    OfxBuilder.init(this)
      .addTransaction("2008/08/29", -40.00, "Auchan")
      .addTransaction("2008/08/29", 100.00, "Loto")
      .addTransaction("2008/08/29", -100.00, "Fringue")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Courses");
    categorization
      .editSeries("Courses")
      .switchToManual()
      .selectAllMonths()
      .setAmount("100")
      .toggleAutoReport()
      .validate();

    categorization.setNewVariable("Fringue", "Fringues");
    categorization
      .editSeries("Fringues")
      .switchToManual()
      .selectAllMonths()
      .setAmount("40")
      .toggleAutoReport()
      .validate();

    categorization.setNewIncome("Loto", "Gain");
    categorization
      .editSeries("Gain")
      .switchToManual()
      .selectAllMonths()
      .setAmount("200")
      .toggleAutoReport()
      .validate();

    views.selectBudget();
    budgetView.variable.checkSeries("Courses", -40, -100);
    budgetView.variable.checkSeries("Fringues", -100, -40);
    budgetView.income.checkSeries("Gain", 100, 200);

    operations.nextMonth();
    timeline.selectMonth("2008/08");
    budgetView.variable.checkSeries("Courses", -40, -40);
    budgetView.variable.checkSeries("Fringues", -100, -100);
    budgetView.income.checkSeries("Gain", 100, 100);
    timeline.selectMonth("2008/09");
    budgetView.variable.checkSeries("Courses", 0, -160);
    budgetView.variable.checkSeries("Fringues", 0, 0);
    budgetView.income.checkSeries("Gain", 0, 300);
    timeline.selectMonth("2008/10");
    budgetView.variable.checkSeries("Fringues", 0, -20);
    operations.nextMonth();
    timeline.selectMonth("2008/09");
    budgetView.variable.checkSeries("Courses", 0, 0);
    budgetView.variable.checkSeries("Fringues", 0, 0);
    timeline.selectMonth("2008/10");
    budgetView.variable.checkSeries("Courses", 0, -260);
    budgetView.variable.checkSeries("Fringues", 0, -20);

  }

  public void testWithZero() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(3)
      .validate();
    OfxBuilder.init(this)
      .addTransaction("2008/08/29", -40.00, "Pharama")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Pharama", "Santé");
    categorization
      .editSeries("Santé")
      .switchToManual()
      .selectAllMonths()
      .setAmount("0")
      .toggleAutoReport()
      .validate();

    views.selectBudget();
    budgetView.variable.checkSeries("Santé", -40, 0);

    operations.nextMonth();
    timeline.selectMonth("2008/08");
    budgetView.variable.checkSeries("Santé", -40, -40);
    
    timeline.selectMonth("2008/09");
    budgetView.variable.checkSeries("Santé", 0, 40);

    operations.nextMonth();

    timeline.selectMonth("2008/09");
    budgetView.variable.checkSeries("Santé", 0, 0);

    timeline.selectMonth("2008/10");
    budgetView.variable.checkSeries("Santé", 0, 40);
  }

  public void testWithSavings() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", 50.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -50.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CA")
      .setFromAccount("Main accounts")
      .setToAccount("Account n. 111")
      .switchToManual()
      .selectAllMonths()
      .setAmount(100)
      .toggleAutoReport()
      .validate();

    views.selectCategorization();
    categorization.setSavings("Virement", "CA");
    views.selectBudget();
    budgetView.savings.checkSeries("CA", 50, 100);
    operations.nextMonth();
    timeline.selectMonth("2008/08");
    budgetView.savings.checkSeries("CA", 50, 50);
    timeline.selectMonth("2008/09");
    budgetView.savings.checkSeries("CA", 0, 150);
    views.selectSavings();
    savingsView.checkSeriesAmounts("Account n. 111", "CA", 0, 150);
  }
}
