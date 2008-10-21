package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class StatTest extends LoggedInFunctionalTestCase {

  public void testCategorisationWithPositiveTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/07/15", -90.00, "Auchan")
      .addTransaction("2008/07/14", -80.00, "Carouf")
      .load();

    views.selectCategorization();

    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, true);
    categorization.setEnvelope("Carouf", "Courant", MasterCategory.FOOD, false);
    views.selectBudget();

    budgetView.envelopes.createSeries().setName("Secu")
      .switchToManual()
      .selectAllMonths()
      .setCategory(MasterCategory.HEALTH).setAmount("0").validate();

    views.selectCategorization();
    OfxBuilder
      .init(this)
      .addTransaction("2008/08/26", -49.90, "ED")
      .addTransaction("2008/08/25", 60.90, "Auchan")
      .addTransaction("2008/08/13", 10.00, "Pharma")
      .load();

    categorization.setEnvelope("Pharma", "Secu", MasterCategory.HEALTH, false);
    categorization.setEnvelope("ED", "Courant", MasterCategory.FOOD, false);
    views.selectData();
    transactions.initContent()
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Secu", "", -10, "Secu", MasterCategory.HEALTH)
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Courant", "", (-(80 + 90) - 60.90 + 49.9) /*181*/,
           "Courant", MasterCategory.FOOD)
      .add("26/08/2008", TransactionType.PRELEVEMENT, "ED", "", -49.90, "Courant", MasterCategory.FOOD)
      .add("25/08/2008", TransactionType.VIREMENT, "Auchan", "", 60.90, "Courant", MasterCategory.FOOD)
      .add("13/08/2008", TransactionType.VIREMENT, "Pharma", "", 10.00, "Secu", MasterCategory.HEALTH)
      .check();

    views.selectBudget();
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("Courant", -90 - 80, -90 - 80);
    budgetView.envelopes.checkTotalAmounts(-90 - 80, -90 - 80);

    timeline.selectMonth("2008/08");
    budgetView.envelopes.checkSeries("Courant", (60.90 - 49.9), -90 - 80);// 11,  -170 == > 181
    budgetView.envelopes.checkSeries("Secu", 10, 0);

    budgetView.envelopes.checkTotalAmounts(10 + 60.90 - 49.9, -90 - 80);
  }

  public void testChangeSeriesBudgetCanCreatePlannedTransaction() throws Exception {
    views.selectBudget();
    budgetView.envelopes.createSeries().setName("Secu")
      .switchToManual()
      .selectAllMonths()
      .setCategory(MasterCategory.HEALTH).selectPositiveAmounts()
      .setAmount("10").validate();

    views.selectCategorization();
    OfxBuilder
      .init(this)
      .addTransaction("2008/08/13", 10.00, "Pharma")
      .load();

    categorization.setEnvelope("Pharma", "Secu", MasterCategory.HEALTH, false);
    views.selectData();
    transactions.initContent()
      .add("13/08/2008", TransactionType.VIREMENT, "Pharma", "", 10.00, "Secu", MasterCategory.HEALTH)
      .check();

    views.selectBudget();
    budgetView.envelopes.editSeriesList().selectSeries("Secu").setAmount("0").validate();
    transactions.initContent()
      .add("13/08/2008", TransactionType.PLANNED, "Planned: Secu", "", -10.00, "Secu", MasterCategory.HEALTH)
      .add("13/08/2008", TransactionType.VIREMENT, "Pharma", "", 10.00, "Secu", MasterCategory.HEALTH)
      .check();
  }

  public void testOverBurnIsTakenInAccountInMonthSummaryView() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/07/15", -90.00, "Auchan")
      .addTransaction("2008/07/14", -80.00, "Carouf")
      .load();

    views.selectCategorization();

    categorization.setEnvelope("Auchan", "Auchan", MasterCategory.FOOD, true);
    categorization.setEnvelope("Carouf", "Carouf", MasterCategory.FOOD, true);

    OfxBuilder
      .init(this)
      .addTransaction("2008/08/15", -110.00, "Auchan")
      .addTransaction("2008/08/14", -0.00, "Carouf")
      .load();

    timeline.selectAll();
    timeline.selectMonth("2008/08");
    views.selectHome();
    monthSummary.checkEnvelope(110, 190, -20);
    views.selectBudget();
    budgetView.envelopes.checkTotalAmounts(-110, -190);
    views.selectData();
    transactions.initContent()
      .add("15/08/2008", TransactionType.PLANNED, "Planned: Carouf", "", -80.00, "Carouf", MasterCategory.FOOD)
      .add("15/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -110.00, "Auchan", MasterCategory.FOOD)
      .add("14/08/2008", TransactionType.VIREMENT, "Carouf", "", -0.00, "Carouf", MasterCategory.FOOD)
      .check();
  }

  public void testBalanceDoNotTakeInAcountOperationForNextMont() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/01", 200.00, "Salaire")
      .addTransaction("2008/06/15", -90.00, "Auchan")
      .addTransaction("2008/06/30", "2008/07/01", -80.00, "Carouf")
      .load();
    views.selectCategorization();
    categorization.setIncome("Salaire", "Salaire", true);
    categorization.setEnvelope("Auchan", "courses", MasterCategory.FOOD, true);
    categorization.setEnvelope("Carouf", "courses", MasterCategory.FOOD, false);
    timeline.selectMonth("2008/06");
    views.selectHome();
    monthSummary.checkBalance(200 - 90);  //balance banque du mois : ne prends pas en compte les 80
    balanceSummary.checkTotal(80);
    timeline.selectMonth("2008/07");
    balanceSummary.checkTotal(200 - 170);
    monthSummary.checkBalance(200 - 80 - 170);  //-50 ==> prends en comptes les 80
  }
}
