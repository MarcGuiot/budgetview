package org.crossbowlabs.globs.wicket.form;

import wicket.feedback.ContainerFeedbackMessageFilter;
import wicket.feedback.FeedbackMessages;
import wicket.feedback.IFeedback;
import wicket.feedback.IFeedbackMessageFilter;
import wicket.markup.html.basic.Label;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

public class GlobFormFeedbackBorder extends Panel implements IFeedback {
  public static final String CHILD_ID = "child";

  private static final long serialVersionUID = 1L;
  private boolean error;

  public GlobFormFeedbackBorder(final String id) {
    super(id);
    add(new ErrorIndicator("errorIndicator", new Model("*")));
  }

  public void updateFeedback() {
    FeedbackMessages feedbackMessages = getPage().getFeedbackMessages();
    error = feedbackMessages.messages(getMessagesFilter()).size() > 0;
  }

  protected IFeedbackMessageFilter getMessagesFilter() {
    return new ContainerFeedbackMessageFilter(this);
  }

  private final class ErrorIndicator extends Label {
    private static final long serialVersionUID = 1L;

    public ErrorIndicator(String id, Model model) {
      super(id, model);
    }

    public boolean isVisible() {
      return error;
    }
  }
}
