package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesAmountEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class BudgetSummaryViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/07");
    super.setUp();
  }

  public void testUncategorized() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "0001212", 1500.00, "2008/07/10")
      .addTransaction("2008/06/05", 1000.00, "WorldCo")
      .addTransaction("2008/06/10", -200.00, "Auchan")
      .addTransaction("2008/06/30", "2008/07/01", -20.00, "ED")
      .addTransaction("2008/07/05", -50.00, "FNAC")
      .load();

    views.selectBudget();
    timeline.checkSelection("2008/07");
    budgetView.getSummary()
      .checkUncategorized(50.00)
      .gotoUncategorized();

    categorization.initContent()
      .add("05/07/2008", "", "FNAC", -50)
      .check();
  }

  public void test() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    views.selectBudget();

    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "0001212", 1500.00, "2008/07/10")
      .addTransaction("2008/06/05", 1000.00, "WorldCo")
      .addTransaction("2008/06/10", -200.00, "Auchan")
      .addTransaction("2008/07/05", -50.00, "FNAC")
      .load();

    views.selectBudget();
    timeline.checkSelection("2008/07");
    timeline.selectAll();
    budgetView.getSummary()
      .checkUncategorized(1000.00 + 200.00 + 50.00)
      .gotoUncategorized();

    views.checkCategorizationSelected();
    categorization.checkShowsUncategorizedTransactionsForSelectedMonths();
    categorization.checkNoSelectedTableRows();
    categorization.selectTransactions("WorldCo").selectIncome().createSeries("Salary");
    categorization.setNewVariable("Auchan", "Groceries", -200.);

    timeline.selectMonth("2008/07");
    views.selectBudget();
    budgetView.getSummary()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+750.00)
      .checkReferencePosition("Position on 05/07/2008", 1500.00)
      .checkEndPosition("Estimated end of jul 08 position", 2300.00)
      .checkUncategorized(50.00);

    timeline.selectAll();

    budgetView.getSummary()
      .checkMultiSelection(4)
      .checkMonthBalance(+3150.00)
      .checkEndPosition("Estimated end of sep 08 position", 3900.00)
      .checkUncategorized(50.00);

    timeline.selectMonth("2008/06");
    budgetView.getSummary()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+800.00)
      .checkEndPosition("Position on june 08", 1550.00)
      .checkUncategorizedNotShown();

    views.selectCategorization();
    categorization.showAllTransactions();
    categorization.setNewVariable("FNAC", "Leisures", -50.);

    timeline.selectMonth("2008/07");
    views.selectBudget();
    budgetView.getSummary()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+750.00)
      .checkReferencePosition("Position on 05/07/2008", 1500.00)
      .checkEndPosition("Estimated end of jul 08 position", 2300.00)
      .checkUncategorizedNotShown();

    timeline.selectMonth("2008/09");
    views.selectBudget();
    budgetView.getSummary()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+750.00)
      .checkReferencePosition("Position on 05/07/2008", 1500.00)
      .checkEndPosition("Estimated end of sep 08 position", 3800.00)
      .checkUncategorizedNotShown();
  }

  public void testTooltipsOverun() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/04", -30, "fringue")
      .addTransaction("2008/07/04", -150, "Auchan")
      .load();
    views.selectCategorization();
    categorization.setNewVariable("Auchan", "courses", -150.);
    categorization.setNewVariable("fringue", "habillement", -30.);

    views.selectBudget();
    timeline.selectMonth("2008/07");
    budgetView.variable.checkSeries("habillement", -30, -30)
      .checkSeries("courses", -150, -150);

    setVariableAmount("habillement", 70);
    setVariableAmount("courses", 130);
    budgetView.variable
      .checkSeriesGaugeRemaining("courses", 0., true)
      .checkGaugeTooltip("habillement", "Il reste <b>40.00</b> à dépenser")
      .checkTotalErrorOverrun()
      .checkTotalAmounts(-180, -200)
      .checkTotalDescription(40., 20., 200. + 20.);

    setVariableAmount("habillement", 50);
    budgetView.variable.checkTotalErrorOverrun()
      .checkTotalAmounts(-180, -180)
      .checkTotalDescription(20., 20., 180. + 20.);

    setVariableAmount("habillement", 40);
    budgetView.variable.checkTotalErrorOverrun()
      .checkTotalAmounts(-180, -170)
      .checkTotalDescription(10., 20., 170. + 20.);

    setVariableAmount("habillement", 30);
    budgetView.variable.checkTotalErrorOverrun()
      .checkTotalAmounts(-180, -160)
      .checkTotalDescription(0., 20., 160. + 20.);

    setVariableAmount("habillement", -20);
    budgetView.variable.checkSeries("habillement", -30, 20)
      .checkGaugeTooltip("habillement", "Il vous reste <b>50.00</b> à recevoir")
      .checkSeriesGaugeRemaining("habillement", 50., false)
      .checkTotalErrorOverrun()
      .checkTotalAmounts(-180, -110)
      .checkTotalDescription(20., 50., 110. + 20.);
  }

  public void testGaugeInSavingsInThePast() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/04", -30, "virement")
      .addTransaction("2008/06/04", 30, "prelevement")
      .load();
    views.selectCategorization();
    categorization.setNewSavings("virement", "epargne", "Main accounts", "external account");
    categorization.setNewSavings("prelevement", "financement", "external account", "Main accounts");
    views.selectBudget();
    setSavingsAmount("epargne", 20);
    budgetView.savings
      .checkSeriesGaugeRemaining("epargne", 0., false)
      .checkGaugeTooltip("epargne", "<p>Vous avez épargné <b>10.00</b> de plus que prévu</p>");

    setSavingsAmount("epargne", 40);
    budgetView.savings
      .checkSeriesGaugeRemaining("epargne", -10., false)
      .checkGaugeTooltip("epargne", "Vous avez épargné <b>10.00</b> de moins que prévu");

    setSavingsAmount("epargne", 30);
    setSavingsAmount("financement", 40);
    budgetView.savings
      .checkSeriesGaugeRemaining("financement", 10., false)
      .checkGaugeTooltip("financement", "Il restait <b>10.00</b> à retirer de l'épargne");

    budgetView.savings
      .checkTotalGaugeTooltips("Il restait <b>10.00</b> à retirer de l'épargne");
  }

  public void testGaugeInSavings() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/04", -30, "virement")
      .addTransaction("2008/07/04", 30, "prelevement")
      .load();
    views.selectCategorization();
    categorization.setNewSavings("virement", "epargne", "Main accounts", "external account");
    categorization.setNewSavings("prelevement", "financement", "external account", "Main accounts");
    views.selectBudget();
    setSavingsAmount("epargne", 20);
    budgetView.savings
      .checkSeriesGaugeRemaining("epargne", 0., false)
      .checkGaugeTooltip("epargne", "Vous avez épargné <b>10.00</b> de plus que prévu");

    setSavingsAmount("epargne", 40);
    budgetView.savings
      .checkSeriesGaugeRemaining("epargne", -10., false)
      .checkGaugeTooltip("epargne", "Il vous reste <b>10.00</b> à épargner");

    setSavingsAmount("financement", 20);

    budgetView.savings
      .checkSeriesGaugeRemaining("financement", 0., true)
      .checkGaugeTooltip("financement", "Vous avez retiré <b>10.00</b> de plus que prévu")
      .checkTotalGaugeTooltips("Vous avez épargné <b>10.00</b> de moins que prévu",
                               "Il vous reste <b>10.00</b> à épargner");

    setSavingsAmount("epargne", 30);
    setSavingsAmount("financement", 40);
    budgetView.savings
      .checkSeriesGaugeRemaining("financement", 10., false)
      .checkGaugeTooltip("financement", "Il reste <b>10.00</b> à retirer de l'épargne");
  }

  public void testEnveloppeWithPositive() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/04", 30, "remboursement")
      .addTransaction("2008/07/04", -150, "Auchan")
      .load();
    views.selectCategorization();
    views.selectCategorization();
    categorization.setNewVariable("Auchan", "courses", -150.);
    categorization.setNewVariable("remboursement", "SECU", 30.);
    views.selectBudget();
    timeline.selectMonth("2008/07");

    setVariableAmount("SECU", 40);
    setVariableAmount("courses", 140);

    budgetView.variable.checkTotalAmounts(-120, -100)
      .checkTotalTooltips(10, 110)
      .checkTotalGaugeTooltips("Il reste <b>10.00</b> à recevoir",
                               "Vous avez dépensé <b>10.00</b> de plus que prévu");

    setVariableAmount("SECU", 30);
    setVariableAmount("courses", 150);

    budgetView.variable.checkTotalGauge(-120, -120);

    setVariableAmount("SECU", 40);
    budgetView.variable.checkTotalAmounts(-120, -110)
      .checkTotalGaugeTooltips("Il reste <b>10.00</b> à recevoir");

    setVariableAmount("courses", 180);
    budgetView.variable.checkTotalGaugeTooltips("Il reste <b>20.00</b> à dépenser")
      .checkGaugeTooltip("SECU", "Il vous reste <b>10.00</b> à recevoir")
      .checkGaugeTooltip("courses", "Il reste <b>30.00</b> à dépenser");

    setVariableAmount("courses", 120);

    setVariableAmount("SECU", 20);

    budgetView.variable.checkTotalGaugeTooltips("Vous avez dépensé <b>20.00</b> de plus que prévu")
      .checkGaugeTooltip("SECU", "Vous avez reçu <b>10.00</b> de plus que prévu")
      .checkGaugeTooltip("courses", "Vous avez dépensé <b>30.00</b> de plus que prévu")
      .checkTotalTooltips(20, 120);
  }

  public void testMonthInPast() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/05/04", -10, "other")
      .addTransaction("2008/06/04", -150, "Auchan")
      .addTransaction("2008/07/04", -120, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "courses");
    views.selectBudget();

    budgetView.variable.editSeries("courses")
      .selectAllMonths()
      .setAmount(150)
      .validate();

    timeline.selectAll();
    String nonDepense = "Vous avez dépensé <b>150.00</b> de moins que prévu";
    String reste = "Il reste <b>30.00</b> à dépenser";
    budgetView.variable
      .checkGaugeTooltip("courses", nonDepense, reste)
      .checkTotalGaugeTooltips(nonDepense, reste)
      .checkTotalAmounts(-270., -450.);

    setVariableAmount("courses", 100);
    String depassement = "Vous avez dépensé <b>30.00</b> de moins que prévu";
    budgetView.variable
      .checkGaugeTooltip("courses", depassement)
      .checkTotalGaugeTooltips(depassement);
  }

  public void testWithPlannedZero() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/05/04", -30, "medecin")
      .addTransaction("2008/06/04", 29, "mutuel")
      .load();
    views.selectCategorization();
    categorization.setNewVariable("medecin", "secu");
    categorization.setVariable("mutuel", "secu");
    views.selectBudget();
    budgetView.variable.editSeries("secu")
      .selectAllMonths()
      .setAmount(0)
      .validate();
    timeline.selectAll();
    budgetView.variable.checkTotalGaugeTooltips("Vous avez dépensé <b>1.00</b><br>alors que vous n'aviez rien prévu")
      .checkTotalErrorOverrun();
  }

  public void testIncome() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "0001212", 1500.00, "2008/07/10")
      .addTransaction("2008/06/05", 1000.00, "WorldCo")
      .addTransaction("2008/07/05", 500.00, "Loto")
      .addTransaction("2008/07/05", 1000.00, "WorldCo")
      .load();
    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "salaire");
    categorization.setNewIncome("Loto", "Loto");
    views.selectBudget();
    timeline.selectAll();
    setIncome("salaire", 900);
    setIncome("Loto", 100);
    budgetView.income.checkTotalGaugeTooltips("Vous avez reçu <b>500.00</b> de plus que prévu")
      .checkTotalTooltips(600., 2600);

    setIncome("salaire", 2000);

    budgetView.income.checkTotalGaugeTooltips("Vous avez reçu <b>700.00</b> de moins que prévu",
                                              "Il vous reste <b>1000.00</b> à recevoir")
      .checkTotalTooltips(400., 4600);
  }

  public void testNegativeIncome() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(-1, 10674, "0001212", 1500.00, "2008/07/10")
      .addTransaction("2008/06/05", 1000.00, "WorldCo")
      .addTransaction("2008/07/05", -100.00, "WorldCo")
      .load();
    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "salaire");
    views.selectBudget();
    timeline.selectAll();
    budgetView.income.checkTotalGaugeTooltips("Il vous reste <b>1100.00</b> à recevoir");
    timeline.selectMonth("2008/07");
    budgetView.income.checkGaugeBeginInError();
  }

  private void setVariableAmount(final String seriesName, final double amount) {
    SeriesAmountEditionDialogChecker seriesAmountEditionChecker = budgetView.variable.editPlannedAmount(seriesName);
    if (amount < 0) {
      seriesAmountEditionChecker.selectPositiveAmounts();
    }
    seriesAmountEditionChecker
      .setAmount(Math.abs(amount))
      .validate();
  }

  private void setIncome(final String seriesName, final double amount) {
    SeriesAmountEditionDialogChecker seriesAmountEditionChecker = budgetView.income.editPlannedAmount(seriesName);
    seriesAmountEditionChecker
      .setAmount(Math.abs(amount))
      .validate();
  }

  private void setSavingsAmount(final String seriesName, final double amount) {
    SeriesAmountEditionDialogChecker seriesAmountEditionChecker = budgetView.savings.editPlannedAmount(seriesName);
    seriesAmountEditionChecker
      .setAmount(Math.abs(amount))
      .validate();
  }

}
