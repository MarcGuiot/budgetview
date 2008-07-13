package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategorizationDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;

public class CategorizationTest extends LoggedInFunctionalTestCase {

  public void testStandardIncomeTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -1129.90, "WorldCo/june")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("WorldCo/june");

    dialog.selectIncome();
    dialog.checkContainsIncomeSeries("Salary", "Exceptional Income");
    dialog.selectIncomeSeries("Salary");
    dialog.validate();

    transactionDetails.checkSeries("Salary");
    transactionDetails.checkCategory(MasterCategory.INCOME);

    CategorizationDialogChecker reopenedDialog = transactions.categorize(0);
    reopenedDialog.checkIncomeSeriesIsSelected("Salary");
    dialog.selectIncomeSeries("Exceptional Income");
    dialog.checkIncomeSeriesIsNotSelected("Salary");
  }

  public void testStandardRecurringTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free Telecom")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("Free Telecom");

    dialog.selectRecurring();
    dialog.checkContainsRecurringSeries("Internet", "Rental", "Electricity");
    dialog.selectRecurringSeries("Internet");
    dialog.validate();

    transactionDetails.checkSeries("Internet");
    transactionDetails.checkCategory(MasterCategory.TELECOMS);

    CategorizationDialogChecker reopenedDialog = transactions.categorize(0);
    reopenedDialog.checkRecurringSeriesIsSelected("Internet");
    dialog.selectRecurringSeries("Rental");
    dialog.checkRecurringSeriesIsNotSelected("Internet");
  }

  public void testStandardEnvelopeTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "AUCHAN C'EST BON")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("AUCHAN C'EST BON");

    dialog.selectEnvelopes();
    dialog.checkContainsEnvelope("Groceries", MasterCategory.FOOD, MasterCategory.HOUSE);
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD);
    dialog.validate();

    transactionDetails.checkSeries("Groceries");
    transactionDetails.checkCategory(MasterCategory.FOOD);

    CategorizationDialogChecker reopenedDialog = transactions.categorize(0);
    reopenedDialog.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.FOOD);
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.HOUSE);
    dialog.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.HOUSE);
    dialog.checkEnveloppeSeriesIsNotSelected("Groceries", MasterCategory.FOOD);
  }

  public void testStandardOccasionalTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -199.90, "Fouquet's")
      .load();

    categories.createSubCategory(MasterCategory.FOOD, "Saucisson");
    categories.select(MasterCategory.ALL);

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("Fouquet's");

    dialog.selectOccasional();
    dialog.checkContainsOccasional(MasterCategory.MULTIMEDIA,
                                   MasterCategory.CLOTHING,
                                   MasterCategory.BEAUTY,
                                   MasterCategory.EDUCATION);
    dialog.checkContainsOccasional(MasterCategory.FOOD, "Saucisson");

    dialog.selectOccasionalSeries(MasterCategory.FOOD);
    dialog.validate();

    transactionDetails.checkSeries("Occasional");
    transactionDetails.checkCategory(MasterCategory.FOOD);

    CategorizationDialogChecker reopenedDialog = transactions.categorize(0);
    reopenedDialog.checkOccasionalSeries(MasterCategory.FOOD);
    reopenedDialog.selectOccasionalSeries(MasterCategory.FOOD, "Saucisson");
    reopenedDialog.validate();

    transactionDetails.checkSeries("Occasional");
    transactionDetails.checkCategory("Saucisson");
  }

  public void testUnassignedTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -199.90, "LDLC")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("LDLC");

    dialog.checkNoBudgetAreaSelected();
    dialog.checkTextVisible("Select the series type");
  }

  public void testCancel() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -29.90, "Free Telecom")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("Free Telecom");

    dialog.selectRecurring();
    dialog.checkContainsRecurringSeries("Internet", "Rental", "Electricity");
    dialog.selectRecurringSeries("Internet");
    dialog.cancel();

    transactionDetails.checkNoSeries();
  }

  public void testNextPreviousTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -29.90, "Free Telecom")
      .addTransaction("2008/06/30", -40, "Auchan")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0, 1);
    dialog.checkLabel("Free Telecom");
    dialog.checkNoBudgetAreaSelected();
    dialog.selectRecurring();
    dialog.selectRecurringSeries("Internet");
    dialog.checkNextIsEnabled();
    dialog.checkPreviousIsDisabled();

    dialog.selectNext();
    dialog.checkLabel("Auchan");
    dialog.checkNoBudgetAreaSelected();
    dialog.checkNextIsDisabled();
    dialog.checkPreviousIsEnabled();
    dialog.checkNoBudgetAreaSelected();
    dialog.selectEnvelopes();
    dialog.selectEnvelopeSeries("Groceries", MasterCategory.FOOD);

    dialog.selectPrevious();
    dialog.checkLabel("Free Telecom");
    dialog.checkNextIsEnabled();
    dialog.checkPreviousIsDisabled();
    dialog.checkRecurringSeriesIsSelected("Internet");

    dialog.selectNext();
    dialog.checkLabel("Auchan");
    dialog.checkNextIsDisabled();
    dialog.checkPreviousIsEnabled();
    dialog.checkEnvelopeSeriesIsSelected("Groceries", MasterCategory.FOOD);
  }

  public void testSelectingRecurringSelectsBudgetArea() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -29.90, "Free Telecom")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);

    dialog.selectRecurring();
    dialog.selectRecurringSeries("Internet");
    dialog.checkBudgetAreaIsSelected(BudgetArea.RECURRING_EXPENSES);
  }

  public void testEscClosesTheDialogAndCancelsChanges() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/28", -29.90, "Free Telecom")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.assertVisible(true);
    dialog.selectRecurring();
    dialog.selectRecurringSeries("Internet");

    dialog.pressEscapeKey();
    dialog.assertVisible(false);
    transactionDetails.checkNoSeries();
  }
}
