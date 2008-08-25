package org.globsframework.wicket.table.rows;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.wicket.FormSubmitListener;
import org.globsframework.wicket.table.GlobTableColumn;
import org.globsframework.wicket.table.GlobTableRowEditorFactory;

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
