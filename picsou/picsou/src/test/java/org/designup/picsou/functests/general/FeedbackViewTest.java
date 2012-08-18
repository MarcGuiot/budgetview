package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class FeedbackViewTest extends LoggedInFunctionalTestCase {

  public void test() throws Exception {

    feedbackView.checkFeedbackLinksTo("http://support.mybudgetview.fr/anonymous_requests/new");

    feedbackView.openHelp()
      .checkTitle("Index")
      .close();

    feedbackView.checkTwitterLinksTo("http://twitter.com/#!/budgetview");

    feedbackView.checkFacebookLinksTo("http://www.facebook.com/pages/BudgetView/111823858873970");
  }
}