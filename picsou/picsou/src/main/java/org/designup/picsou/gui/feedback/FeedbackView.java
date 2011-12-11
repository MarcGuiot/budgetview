package org.designup.picsou.gui.feedback;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.feedback.actions.FacebookAction;
import org.designup.picsou.gui.feedback.actions.SendFeedbackAction;
import org.designup.picsou.gui.feedback.actions.TwitterAction;
import org.designup.picsou.gui.help.actions.GotoWebsiteAction;
import org.designup.picsou.gui.help.actions.HelpAction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class FeedbackView extends View {

  public FeedbackView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/feedback/feedbackView.splits",
                                                      repository, directory);

    builder.add("help", new HelpAction(Lang.get("feedback.help.text"), "index", Lang.get("feedback.help.tooltip"), directory));
    builder.add("sendFeedback", new SendFeedbackAction(Lang.get("feedback.send.text"), repository, directory));
    builder.add("visitWebsite", new GotoWebsiteAction(Lang.get("feedback.goto.website"), directory));
    builder.add("twitter", new TwitterAction(directory));
    builder.add("facebook", new FacebookAction(directory));

    parentBuilder.add("feedbackView", builder);
  }
}
