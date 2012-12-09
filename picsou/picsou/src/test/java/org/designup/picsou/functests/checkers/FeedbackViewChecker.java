package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;

public class FeedbackViewChecker extends ViewChecker {

  private Panel feedbackPanel;

  public FeedbackViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void checkFeedbackLink() {
    FeedbackDialogChecker.init(getPanel().getButton("sendFeedback").triggerClick())
      .checkComponents()
      .cancel();
  }

  public HelpChecker openHelp() {
    return HelpChecker.open(getPanel().getButton("help").triggerClick());
  }

  public void checkTwitterLinksTo(String urlPart) {
    checkButtonOpensUrl("twitter", urlPart);
  }

  public void checkFacebookLinksTo(String urlPart) {
    checkButtonOpensUrl("facebook", urlPart);
  }

  public void checkWebsiteLinksTo(String urlPart) {
    checkButtonOpensUrl("visitWebsite", urlPart);
  }

  private void checkButtonOpensUrl(String button, String urlPart) {
    BrowsingChecker.checkDisplayedUrlContains(getPanel().getButton(button).triggerClick(), urlPart);
  }

  private Panel getPanel() {
    if (feedbackPanel == null) {
      views.selectHome();
      feedbackPanel = mainWindow.getPanel("feedbackView");
    }
    return feedbackPanel;
  }
}
