package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesAmountEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class BudgetSummaryViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/07");
    super.setUp();
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
      .skipWizard()
      .checkUncategorized(1000.00 + 200.00 + 50.00)
      .gotoUncategorized();

    views.checkCategorizationSelected();
    categorization.checkShowsUncategorizedTransactionsOnly();
    categorization.checkNoSelectedTableRows();
    categorization.selectTransactions("WorldCo").selectIncome().createSeries("Salary");
    categorization.selectTransactions("Auchan").selectEnvelopes().createSeries("Groceries");

    timeline.selectMonth("2008/07");
    views.selectBudget();
    budgetView.getSummary()
      .skipWizard()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+750.00)
      .checkEndPosition("End of jul 08 position", 2300.00)
      .checkUncategorized(50.00);

    timeline.selectAll();

    budgetView.getSummary()
      .checkMultiSelection(4)
      .checkMonthBalance(+3150.00)
      .checkEndPosition("End of sep 08 position", 3900.00)
      .checkUncategorized(50.00);

    timeline.selectMonth("2008/06");
    budgetView.getSummary()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+800.00)
      .checkEndPosition("Position", 1550.00)
      .checkUncategorizedNotShown();

    views.selectCategorization();
    categorization.showAllTransactions();
    categorization.setNewEnvelope("FNAC", "Leisures");

    timeline.selectMonth("2008/07");
    views.selectBudget();
    budgetView.getSummary()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+750.00)
      .checkEndPosition("End of jul 08 position", 2300.00)
      .checkUncategorizedNotShown();

    timeline.selectMonth("2008/09");
    views.selectBudget();
    budgetView.getSummary()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+750.00)
      .checkEndPosition("End of sep 08 position", 3800.00)
      .checkUncategorizedNotShown();
  }

  public void testTooltipsOverun() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/04", -30, "fringue")
      .addTransaction("2008/07/04", -150, "Auchan")
      .load();
    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "courses");
    categorization.setNewEnvelope("fringue", "habillement");

    views.selectBudget();
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkSeries("habillement", -30, -30)
      .checkSeries("courses", -150, -150);

    setEnvelopesAmount("habillement", 70);
    setEnvelopesAmount("courses", 130);
    budgetView.envelopes
      .checkSeriesGaugeRemaining("courses", 0., true)
      .checkGaugeTooltip("habillement", "Reste : 40")
      .checkTotalErrorOverrun()
      .checkTotalAmounts(-180, -200)
      .checkTotalTooltips(40., 20., 200. + 20.);

    setEnvelopesAmount("habillement", 50);
    budgetView.envelopes.checkTotalErrorOverrun()
      .checkTotalAmounts(-180, -180)
      .checkTotalTooltips(20., 20., 180. + 20.);

    setEnvelopesAmount("habillement", 40);
    budgetView.envelopes.checkTotalErrorOverrun()
      .checkTotalAmounts(-180, -170)
      .checkTotalTooltips(10., 20., 170. + 20.);

    setEnvelopesAmount("habillement", 30);
    budgetView.envelopes.checkTotalErrorOverrun()
      .checkTotalAmounts(-180, -160)
      .checkTotalTooltips(0., 20., 160. + 20.);

    setEnvelopesAmount("habillement", -20);
    budgetView.envelopes.checkSeries("habillement", -30, 20)
      .checkGaugeTooltip("habillement", "Attendu : 50")
      .checkSeriesGaugeRemaining("habillement", 50., false)
      .checkTotalErrorOverrun()
      .checkTotalAmounts(-180, -110)
      .checkTotalTooltips(20., 50., 110. + 20.);
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
      .checkGaugeTooltip("epargne", "Extra : 10");

    setSavingsAmount("epargne", 40);
    budgetView.savings
      .checkSeriesGaugeRemaining("epargne", -10., false)
      .checkGaugeTooltip("epargne", "10.00 aurait du être épargner");

    setSavingsAmount("epargne", 30);
    setSavingsAmount("financement", 40);
    budgetView.savings
      .checkSeriesGaugeRemaining("financement", 10., false)
      .checkGaugeTooltip("financement", "Restait 10.00 à retirer de l'épargne");

    budgetView.savings
      .checkTotalGaugeTooltips("Restait 10.00 à retirer de l'épargne");
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
      .checkGaugeTooltip("epargne", "Extra : 10");

    setSavingsAmount("epargne", 40);
    budgetView.savings
      .checkSeriesGaugeRemaining("epargne", -10., false)
      .checkGaugeTooltip("epargne", "Reste 10.00 à épargner");

    setSavingsAmount("financement", 20);
    budgetView.savings
      .checkSeriesGaugeRemaining("financement", 0., true)
      .checkGaugeTooltip("financement", "Dépassement : 10")
      .checkTotalGaugeTooltips("Dépassement : 10", "Reste 10.00 à épargner");

    setSavingsAmount("epargne", 30);
    setSavingsAmount("financement", 40);
    budgetView.savings
      .checkSeriesGaugeRemaining("financement", 10., false)
      .checkGaugeTooltip("financement", "Reste 10.00 à retirer de l'épargne");
  }

  public void testEnveloppeWithPositive() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/04", 30, "remboursement")
      .addTransaction("2008/07/04", -150, "Auchan")
      .load();
    views.selectCategorization();
    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "courses");
    categorization.setNewEnvelope("remboursement", "SECU");
    views.selectBudget();
    timeline.selectMonth("2008/07");

    setEnvelopesAmount("SECU", 40);
    setEnvelopesAmount("courses", 140);

    budgetView.envelopes.checkTotalAmounts(-120, -100)
      .checkTotalTooltips(10, 110)
      .checkTotalGaugeTooltips("Attendu : 10", "Dépassement : 10");

    setEnvelopesAmount("SECU", 30);
    setEnvelopesAmount("courses",150);

    budgetView.envelopes.checkTotalGauge(-120, -120);

    setEnvelopesAmount("SECU", 40);
    budgetView.envelopes.checkTotalAmounts(-120, -110)
      .checkTotalGaugeTooltips("Attendu : 10");

    setEnvelopesAmount("courses", 180);
    budgetView.envelopes.checkTotalGaugeTooltips("Attendu : 10", "Reste : 30")
      .checkGaugeTooltip("SECU", "Attendu : 10")
      .checkGaugeTooltip("courses", "Reste : 30");

    setEnvelopesAmount("courses", 120);

    setEnvelopesAmount("SECU", 20);

    budgetView.envelopes.checkTotalGaugeTooltips("Dépassement : 30", "Extra : 10")
      .checkGaugeTooltip("SECU", "Extra : 10")
      .checkGaugeTooltip("courses", "Dépassement : 30")
      .checkTotalTooltips(20, 120);
  }

  public void testMonthInPast() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/05/04", -10, "other")
      .addTransaction("2008/06/04", -150, "Auchan")
      .addTransaction("2008/07/04", -120, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "courses");
    views.selectBudget();

    budgetView.envelopes.editSeries("courses").switchToManual()
      .selectAllMonths().setAmount(150).validate();

    timeline.selectAll();
    String nonDepense = "Non dépensé : 150";
    String reste = "Reste : 30";
    budgetView.envelopes
      .checkGaugeTooltip("courses", nonDepense, reste)
      .checkTotalGaugeTooltips(nonDepense, reste)
      .checkTotalAmounts(-270., -450.);

    setEnvelopesAmount("courses", 100);
    nonDepense = "Non dépensé : 100";
    String depassement = "Dépassement : 70";
    budgetView.envelopes
      .checkGaugeTooltip("courses", nonDepense, depassement)
      .checkTotalGaugeTooltips(nonDepense, depassement);
  }

  public void testWithPlannedZero() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/05/04", -30, "medecin")
      .addTransaction("2008/06/04", 29, "mutuel")
      .load();
    views.selectCategorization();
    categorization.setNewEnvelope("medecin", "secu");
    categorization.setEnvelope("mutuel", "secu");
    views.selectBudget();
    budgetView.envelopes.editSeries("secu").switchToManual()
      .selectAllMonths().setAmount(0)
      .validate();
    timeline.selectAll();
    budgetView.envelopes.checkTotalGaugeTooltips("Dépassement : 1")
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
    budgetView.income.checkTotalGaugeTooltips("Extra : 600", "Non reçu : 100")
    .checkTotalTooltips(600., 2600);

    setIncome("salaire", 2000);

    budgetView.income.checkTotalGaugeTooltips("Extra : 400", "Attendu : 1000", "Non reçu : 1100")
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
    budgetView.income.checkTotalGaugeTooltips("Attendu : 1100");
    timeline.selectMonth("2008/07");
    budgetView.income.checkGaugeBeginInError();
  }

  private void setEnvelopesAmount(final String seriesName, final double amount) {
    SeriesAmountEditionDialogChecker seriesAmountEditionChecker = budgetView.envelopes.editPlannedAmount(seriesName);
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
