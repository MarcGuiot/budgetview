package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SubSeriesEditionTest extends LoggedInFunctionalTestCase {

  public void testStandardUsage() throws Exception {

    fail("Regis: voir probleme d'acces à Lang.get dans les splitters TabGroup");

    views.selectBudget();

    budgetView.envelopes.createSeries()
      .setName("Series")
      .selectSubSeriesTab()
      .addSubSeries("SubSeries 1")
      .addSubSeries("SubSeries 2")
      .validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/01", -29.00, "Tx 1")
      .addTransaction("2008/07/15", -40.00, "Tx 2")
      .load();

    views.selectCategorization();
    categorization
      .selectTransaction("Tx 1")
      .selectEnvelopes()
      .checkSeriesContainsSubSeries("Series", "SubSeries 1", "SubSeries 2")
      .selectSeries("SubSeries 1");

    categorization
      .selectTransaction("Tx 2")
      .selectEnvelopes()
      .checkSeriesContainsSubSeries("Series", "SubSeries 1", "SubSeries 2")
      .selectSeries("SubSeries 2");

    categorization
      .selectTransaction("Tx 1")
      .selectEnvelopes()
      .checkSeriesIsSelected("SubSeries 1")
      .checkSeriesNotSelected("SubSeries 2");

    categorization
      .selectTransaction("Tx 2")
      .selectEnvelopes()
      .checkSeriesNotSelected("SubSeries 1")
      .checkSeriesIsSelected("SubSeries 2");
  }

  public void testCreationChecks() throws Exception {
    fail("Regis: tbd");
  }
}
