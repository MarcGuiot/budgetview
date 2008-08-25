package org.globsframework.wicket.table.rows;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.Link;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.MutableFieldValues;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.wicket.component.LinkButtonPanel;
import org.globsframework.wicket.form.GlobForm;
import org.globsframework.wicket.form.GlobFormBuilder;
import org.globsframework.wicket.form.GlobFormCancelAction;
import org.globsframework.wicket.table.GlobTableRowEditor;
import org.globsframework.wicket.table.GlobTableRowEditorFactory;

import java.util.List;

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
