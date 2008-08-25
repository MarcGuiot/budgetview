package org.globsframework.wicket.form;

import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.feedback.FeedbackMessages;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

public class GlobFormFeedbackBorder extends FeedbackPanel implements IFeedback {
  public static final String CHILD_ID = "child";

  private static final long serialVersionUID = 1L;
  private boolean error;

  public GlobFormFeedbackBorder(final String id) {
    super(id);
    add(new ErrorIndicator("errorIndicator", new Model("*")));
  }

  public void updateFeedback() {
    FeedbackMessages feedbackMessages = getPage().getSession().getFeedbackMessages();
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
