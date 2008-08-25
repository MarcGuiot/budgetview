package org.globsframework.wicket.editors;

import org.apache.wicket.Component;
import org.globsframework.metamodel.Link;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.wicket.form.GlobFormFeedbackBorder;

public class LinkEditorPanelFactory {
  public static Component getPanel(String panelId,
                                   String componentId,
                                   Link link,
                                   MutableFieldValues values,
                                   GlobRepository repository,
                                   DescriptionService descriptionService) {

    LinkEditorPanel editorPanel = new LinkEditorPanel(GlobFormFeedbackBorder.CHILD_ID, componentId,
                                                      link, values, repository, descriptionService);

    GlobFormFeedbackBorder feedbackBorder = new GlobFormFeedbackBorder(panelId);
    feedbackBorder.add(editorPanel);

    return feedbackBorder;
  }
}
