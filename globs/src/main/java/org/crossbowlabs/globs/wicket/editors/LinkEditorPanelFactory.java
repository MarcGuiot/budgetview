package org.crossbowlabs.globs.wicket.editors;

import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.wicket.form.GlobFormFeedbackBorder;
import wicket.Component;

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
