package org.crossbowlabs.globs.wicket.table.rows;

import java.util.List;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.MutableFieldValues;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.wicket.component.LinkButtonPanel;
import org.crossbowlabs.globs.wicket.form.GlobForm;
import org.crossbowlabs.globs.wicket.form.GlobFormBuilder;
import org.crossbowlabs.globs.wicket.form.GlobFormCancelAction;
import org.crossbowlabs.globs.wicket.table.GlobTableRowEditor;
import org.crossbowlabs.globs.wicket.table.GlobTableRowEditorFactory;
import wicket.Component;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.markup.html.panel.Panel;

public class DefaultRowEditorFactory implements GlobTableRowEditorFactory {
  private final String switcherText;
  private final List fields;

  public DefaultRowEditorFactory(String switcherText, List fields) {
    this.switcherText = switcherText;
    this.fields = fields;
  }

  public GlobTableRowEditor getEditor(String switcherId,
                                      String editorId,
                                      Key key,
                                      MutableFieldValues fieldValues,
                                      final Component tr,
                                      int rowIndex,
                                      GlobRepository repository,
                                      DescriptionService descriptionService) {
    GlobFormBuilder builder = GlobFormBuilder.init(key, fieldValues);
    for (Object field : fields) {
      if (field instanceof Link) {
        builder.add(((Link)field));
      }
      else {
        builder.add(((Field)field));
      }
    }
    builder.setCancelAction(new GlobFormCancelAction() {
      public String getName() {
        return "Cancel";
      }

      public void run(GlobForm form, AjaxRequestTarget target) {
        form.setVisible(false);
        target.addComponent(tr);
      }
    });
    final Panel editorForm = builder.create(editorId);

    AjaxFallbackLink switcherLink = new AjaxFallbackLink(LinkButtonPanel.ID) {
      public void onClick(final AjaxRequestTarget target) {
        editorForm.setVisible(true);
        target.addComponent(tr);
      }
    };
    LinkButtonPanel switcherLinkPanel = new LinkButtonPanel(switcherId, switcherText, switcherLink);

    return new GlobTableRowEditor(switcherLinkPanel, editorForm);
  }
}
