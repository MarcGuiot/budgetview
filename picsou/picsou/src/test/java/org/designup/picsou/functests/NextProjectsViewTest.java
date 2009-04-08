package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class NextProjectsViewTest extends LoggedInFunctionalTestCase {

  public void testNextProjectsWithNoAccounts() throws Exception {

    operations.openPreferences().setFutureMonthsCount(12).validate();
    
    views.selectSavings();
    nextProjects.checkEmpty();

    timeline.selectMonth("2008/08");
    nextProjects.createProject()
      .setName("Bahamas")
      .setCategory(MasterCategory.LEISURES)
      .validate();

    nextProjects.initContent()
      .check();

    nextProjects.editProjects()
      .checkSeriesListContains("Bahamas")
      .selectSeries("Bahamas")
      .setAmount(1000.00)
      .validate();

    nextProjects.initContent()
      .add("Aug 2008", "Bahamas", -1000.00, -1000.00, null, -1000.00)
      .check();

    timeline.selectMonth("2008/12");
    nextProjects.createProject()
      .setName("Ski")
      .setCategory(MasterCategory.LEISURES)
      .setAmount(1500.00)
      .validate();
    nextProjects.createProject()
      .setName("Noel")
      .setCategory(MasterCategory.GIFTS)
      .setAmount(500.00)
      .validate();

    nextProjects.initContent()
      .add("Dec 2008", "Noel", -500.00, -3000.00, null, -3000.00)
      .add("Dec 2008", "Ski", -1500.00, -3000.00, null, -3000.00)
      .check();

    timeline.selectMonth("2008/08");
    nextProjects.initContent()
      .add("Aug 2008", "Bahamas", -1000.00, -1000.00, null, -1000.00)
      .add("Dec 2008", "Noel", -500.00, -3000.00, null, -3000.00)
      .add("Dec 2008", "Ski", -1500.00, -3000.00, null, -3000.00)
      .check();
  }

  public void testProjectsWithMultipleOccurencesAndMultipleAccounts() throws Exception {

    operations.openPreferences().setFutureMonthsCount(12).validate();

    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 10000, "2008/08/15")
      .addTransaction("2008/08/10", -100.00, "Monoprix")
      .load();

    views.selectSavings();

    nextProjects.createProject()
      .setName("Ski")
      .setCategory(MasterCategory.LEISURES)
      .setEveryMonth()
      .setEndDate(200812)
      .setStartDate(200811)
      .selectAllMonths()
      .setAmount(500.00)
      .validate();

    nextProjects.initContent()
      .add("Nov 2008", "Ski", -500.00, 9500.00, null, 9500.00)
      .add("Dec 2008", "Ski", -500.00, 9000.00, null, 9000.00)
      .check();

    views.selectHome();
    OfxBuilder
      .init(this)
      .addBankAccount(14559, 12345, "0001111", 5000, "2008/08/17")
      .addTransaction("2008/08/15", 200.00, "Epargne")
      .load();
    mainAccounts.edit("Account n. 0001111")
      .setAccountName("ING")
      .setAsSavings()
      .validate();

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Monthly payments")
      .setCategory(MasterCategory.SAVINGS)
      .switchToManual()
      .setToAccount("ING")
      .selectAllMonths()
      .setAmount(300.00)
      .validate();

    views.selectSavings();
    nextProjects.initContent()
      .add("Nov 2008", "Ski", -500.00, 8300.00, 6200.00, 14500.00)
      .add("Dec 2008", "Ski", -500.00, 7500.00, 6500.00, 14000.00)
      .check();
  }

  public void testOnlyProjectsWithNonEmptyAmountsAreShown() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();

    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 10000, "2008/08/15")
      .addTransaction("2008/08/10", -100.00, "FNAC")
      .load();

    views.selectSavings();

    nextProjects.createProject()
      .setName("Plasma TV")
      .setCategory(MasterCategory.LEISURES)
      .setEveryMonth()
      .setEndDate(200903)
      .setStartDate(200811)
      .selectAllMonths()
      .setAmount(500.00)
      .selectMonth(200901)
      .setAmount(0.00)
      .validate();

    nextProjects.initContent()
      .add("Nov 2008", "Plasma TV", -500.00, 9500.00, null, 9500.00)
      .add("Dec 2008", "Plasma TV", -500.00, 9000.00, null, 9000.00)
      .add("Feb 2009", "Plasma TV", -500.00, 8500.00, null, 8500.00)
      .add("Mar 2009", "Plasma TV", -500.00, 8000.00, null, 8000.00)
      .check();
  }

  public void testClickingOnAProjectNameOpensTheSeriesEditionDialog() throws Exception {

    operations.openPreferences().setFutureMonthsCount(12).validate();

    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 10000, "2008/08/15")
      .addTransaction("2008/08/10", -100.00, "FNAC")
      .load();

    views.selectSavings();

    nextProjects.createProject()
      .setName("Plasma TV")
      .setCategory(MasterCategory.LEISURES)
      .setEveryMonth()
      .setEndDate(200812)
      .setStartDate(200811)
      .selectAllMonths()
      .setAmount(500.00)
      .validate();

    timeline.selectMonth("2008/12");

    nextProjects.createProject()
      .setName("Noël")
      .setCategory(MasterCategory.GIFTS)
      .setAmount(300.00)
      .validate();
    
    timeline.selectMonth("2008/11");

    nextProjects.initContent()
      .add("Nov 2008", "Plasma TV", -500.00, 9500.00, null, 9500.00)
      .add("Dec 2008", "Noël", -300.00, 8700.00, null, 8700.00)
      .add("Dec 2008", "Plasma TV", -500.00, 8700.00, null, 8700.00)
      .check();

    nextProjects.editRow(1)
      .checkSeriesSelected("Noël")
      .setAmount(500)
      .validate();
    
    nextProjects.initContent()
      .add("Nov 2008", "Plasma TV", -500.00, 9500.00, null, 9500.00)
      .add("Dec 2008", "Noël", -500.00, 8500.00, null, 8500.00)
      .add("Dec 2008", "Plasma TV", -500.00, 8500.00, null, 8500.00)
      .check();
  }

  public void testPositionsAreDisplayedInRedWhenTooLow() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();

    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 750, "2008/10/15")
      .addTransaction("2008/10/10", -100.00, "Monoprix")
      .load();

    views.selectSavings();

    nextProjects.createProject()
      .setName("Ski")
      .setCategory(MasterCategory.LEISURES)
      .setEveryMonth()
      .setEndDate(200812)
      .setStartDate(200811)
      .selectAllMonths()
      .setAmount(500.00)
      .validate();

    nextProjects.initContent()
      .add("Nov 2008", "Ski", -500.00, 250.00, null, 250.00)
      .add("Dec 2008", "Ski", -500.00, -250.00, null, -250.00)
      .check();

    nextProjects.checkCellTextColorIsNormal(0, 3);
    nextProjects.checkCellTextColorIsError(1, 3);

    views.selectHome();
    mainAccounts.setThreshold(500);

    views.selectSavings();
    nextProjects.checkCellTextColorIsError(0, 3);
    nextProjects.checkCellTextColorIsError(1, 3);

    views.selectHome();
    OfxBuilder
      .init(this)
      .addBankAccount(14559, 12345, "0001111", -1500, "2008/10/17")
      .addTransaction("2008/10/15", 200.00, "Epargne")
      .load();
    mainAccounts.edit("Account n. 0001111")
      .setAccountName("ING")
      .setAsSavings()
      .validate();

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Monthly payments")
      .setCategory(MasterCategory.SAVINGS)
      .switchToManual()
      .setToAccount("ING")
      .selectAllMonths()
      .setAmount(1000.00)
      .validate();

    views.selectSavings();
    nextProjects.initContent()
      .add("Nov 2008", "Ski", -500.00, -750.00, -500.00, -1250.00)
      .add("Dec 2008", "Ski", -500.00, -2250.00, 500.00, -1750.00)
      .check();

    nextProjects.checkCellTextColorIsError(0, 3);
    nextProjects.checkCellTextColorIsError(1, 3);

    nextProjects.checkCellTextColorIsError(0, 4);
    nextProjects.checkCellTextColorIsNormal(1, 4);
  }
}
