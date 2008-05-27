package org.crossbowlabs.globs.wicket.table.rows;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.wicket.FormSubmitListener;
import org.crossbowlabs.globs.wicket.table.GlobTableColumn;
import org.crossbowlabs.globs.wicket.table.GlobTableRowEditorFactory;
import wicket.AttributeModifier;
import wicket.markup.html.form.Form;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

import java.util.List;

public class FormRowPanel extends Panel implements Submittable {
  private Submittable innerRow;
  private String tableId;

  public FormRowPanel(String parentId,
                      String tableId,
                      final Glob glob,
                      List<GlobTableColumn> columns,
                      int rowIndex,
                      String formPrefix,
                      GlobTableRowEditorFactory rowEditorFactory,
                      List<FormSubmitListener> submitListeners,
                      GlobRepository repository,
                      DescriptionService descriptionService) {
    super(parentId);
    this.tableId = tableId;
    add(new MyForm(glob, columns, rowIndex, formPrefix,
                   rowEditorFactory, submitListeners, repository, descriptionService));
    setRenderBodyOnly(true);
  }

  public void submit() {
    innerRow.submit();
  }

  private class MyForm extends Form {

    MyForm(final Glob glob,
           List<GlobTableColumn> columns,
           int rowIndex,
           String formId,
           GlobTableRowEditorFactory rowEditorFactory,
           List<FormSubmitListener> submitListeners,
           GlobRepository repository,
           DescriptionService descriptionService) {
      super("form");
      RowPanel rowPanel = new RowPanel("content", tableId, glob, columns, rowIndex,
                                       rowEditorFactory, submitListeners, repository, descriptionService);
      add(rowPanel);
      add(new AttributeModifier("id", true, new Model(formId + "_" + rowIndex)));
      innerRow = rowPanel;
    }

    protected void onSubmit() {
      submit();
    }
  }
}
