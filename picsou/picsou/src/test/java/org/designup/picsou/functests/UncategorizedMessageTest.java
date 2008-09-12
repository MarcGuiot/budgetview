package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class UncategorizedMessageTest extends LoggedInFunctionalTestCase {

  public void testCategorizationFromWarningMessage() throws Exception {

    OfxBuilder
      .init(this)
      .addTransaction("2008/05/15", 12.00, "mac do")
      .addTransaction("2008/04/15", 12.00, "quick")
      .addTransaction("2008/03/15", 100.00, "fouquet's")
      .load();
    
    timeline.selectMonth("2008/05");

    views.selectHome();
    informationPanel.assertWarningIsDisplayed(3);

    informationPanel.categorize();
    timeline.checkSelection("2008/03", "2008/04", "2008/05");
    views.checkCategorizationSelected();
    categorization.setOccasional("quick", MasterCategory.FOOD);

    views.selectHome();
    informationPanel.assertWarningIsDisplayed(2);

    informationPanel.categorize();
    timeline.checkSelection("2008/03", "2008/05");
    views.checkCategorizationSelected();
    categorization.setOccasional("mac do", MasterCategory.FOOD);

    views.selectHome();
    informationPanel.assertWarningIsDisplayed(1);

    informationPanel.categorize();
    timeline.checkSelection("2008/03");
    views.checkCategorizationSelected();
    categorization.setOccasional("fouquet's", MasterCategory.FOOD);
    
    views.selectHome();
    informationPanel.assertNoWarningIsDisplayed();
  }
}
