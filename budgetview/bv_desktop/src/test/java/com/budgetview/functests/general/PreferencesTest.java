package com.budgetview.functests.general;

import com.budgetview.functests.checkers.PreferencesChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.ColorTheme;
import com.budgetview.model.TransactionType;
import org.junit.Test;

public class PreferencesTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    setCurrentDate("2008/08/30");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  @Test
  public void testChangeFutureMonths() throws Exception {
    timeline.checkDisplays("2008/08");
    operations.openPreferences().setFutureMonthsCount(24).validate();
    timeline.checkSpanEquals("2008/08", "2010/08");
  }

  @Test
  public void testChangeFutureMonthsAndBackAndAgainWithSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/12", -95.00, "Auchan")
      .addTransaction("2008/08/04", -55.00, "EDF")
      .addTransaction("2008/08/01", 1200.00, "Salaire Aout")
      .load();

    categorization.setNewVariable("Auchan", "Courant", -95.00);
    categorization.setNewRecurring("EDF", "EDF");
    categorization.setNewIncome("Salaire Aout", "Salaire");

    operations.openPreferences().setFutureMonthsCount(24).validate();

    timeline.selectLast();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("11/08/2010", TransactionType.PLANNED, "Planned: Courant", "", -95.00, "Courant")
      .add("04/08/2010", TransactionType.PLANNED, "Planned: EDF", "", -55.00, "EDF")
      .add("04/08/2010", TransactionType.PLANNED, "Planned: Salaire", "", 1200.00, "Salaire")
      .check();

    operations.openPreferences().setFutureMonthsCount(12).validate();
    timeline.checkSpanEquals("2008/08", "2009/08");
    timeline.selectLast();
    transactions.initContent()
      .add("11/08/2009", TransactionType.PLANNED, "Planned: Courant", "", -95.00, "Courant")
      .add("04/08/2009", TransactionType.PLANNED, "Planned: EDF", "", -55.00, "EDF")
      .add("04/08/2009", TransactionType.PLANNED, "Planned: Salaire", "", 1200.00, "Salaire")
      .check();

    operations.openPreferences().setFutureMonthsCount(36).validate();

    timeline.selectLast();
    transactions.initContent()
      .add("11/08/2011", TransactionType.PLANNED, "Planned: Courant", "", -95.00, "Courant")
      .add("04/08/2011", TransactionType.PLANNED, "Planned: EDF", "", -55.00, "EDF")
      .add("04/08/2011", TransactionType.PLANNED, "Planned: Salaire", "", 1200.00, "Salaire")
      .check();

    operations.openPreferences().setFutureMonthsCount(12).validate();
    timeline.assertEmpty();
  }

  @Test
  public void testChangeColorTheme() throws Exception {
    screen.checkBackgroundColorIsStandard();

    PreferencesChecker preferences = operations.openPreferences();
    preferences.checkColorThemeSelected(ColorTheme.STANDARD);
    screen.checkBackgroundColorIsStandard();
    preferences.selectColorTheme(ColorTheme.BLACK);
    screen.checkBackgroundColorIsBlack();
    preferences.validate();

    screen.checkBackgroundColorIsBlack();

    PreferencesChecker preferences2 = operations.openPreferences();
    preferences2.checkColorThemeSelected(ColorTheme.BLACK);
    preferences2.selectColorTheme(ColorTheme.STANDARD);
    screen.checkBackgroundColorIsStandard();
    preferences2.cancel();

    screen.checkBackgroundColorIsBlack();
  }
}
