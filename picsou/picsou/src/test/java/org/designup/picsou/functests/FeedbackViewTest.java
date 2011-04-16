package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class FeedbackViewTest extends LoggedInFunctionalTestCase {

  public void test() throws Exception {

    feedbackView.openFeedback()
      .checkConnected()
      .cancel();

    feedbackView.openHelp()
      .checkTitle("Index")
      .close();

    feedbackView.checkTwitterLinksTo("http://twitter.com/home?status=BudgetView");

    feedbackView.checkFacebookLinksTo("http://www.facebook.com/sharer.php?u=http://www.mybudgetview.fr");
  }
}
