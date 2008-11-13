package org.globsframework.wicket.table;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;
import org.globsframework.wicket.FormSubmitListener;
import org.globsframework.wicket.GlobPage;
import org.globsframework.wicket.model.GlobListModel;
import org.globsframework.wicket.table.rows.FormRowPanel;
import org.globsframework.wicket.table.rows.RowPanel;
import org.globsframework.wicket.table.rows.Submittable;

import java.util.ArrayList;
import java.util.List;

class GlobTable extends Panel implements Submittable {
  private List<GlobTableColumn> columns;
  private GlobRepository repository;
  private Directory directory;
  private List<Submittable> submittables = new ArrayList<Submittable>();

  private static final String TABLE_CLASS = "content";

  GlobTable(final String tableId,
            final GlobType type,
            final List<GlobTableColumn> columns,
            final GlobMatcher matcher,
            final TableEditPolicy editPolicy,
            GlobTableRowEditorFactory rowEditorFactory,
            final List<FormSubmitListener> submitListeners, GlobRepository repository, Directory directory) {
    super(tableId);
    this.columns = columns;
    this.repository = repository;
    this.directory = directory;
    setRenderBodyOnly(true);

    WebMarkupContainer table = new WebMarkupContainer("table");

    table.add(new AttributeModifier("id", true, new Model(tableId)));
    table.add(new AttributeModifier("class", true, new Model(TABLE_CLASS)));
    table.add(new ListView("columnNames", GlobTable.this.columns) {
      protected void populateItem(final ListItem columnItem) {
        GlobTableColumn column = (GlobTableColumn)columnItem.getModelObject();
        Label label = new Label("name", column.getTitle());
        label.setRenderBodyOnly(true);
        columnItem.add(label);
      }
    });
    table.add(new RowListView(type, matcher, editPolicy, columns, tableId, rowEditorFactory, submitListeners));
    add(table);
  }

  public void submit() {
    GlobPage page = (GlobPage)getPage();
    GlobRepository repository = page.getRepository();
    repository.startChangeSet();
    try {
      for (Submittable submittable : submittables) {
        submittable.submit();
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private class RowListView extends ListView {
    private final TableEditPolicy editPolicy;
    private final List<GlobTableColumn> columns;
    private final String tableId;
    private final GlobTableRowEditorFactory rowEditorFactory;
    private final List<FormSubmitListener> submitListeners;

    private RowListView(GlobType type,
                        GlobMatcher matcher,
                        TableEditPolicy editPolicy,
                        List<GlobTableColumn> columns,
                        String tableId,
                        GlobTableRowEditorFactory rowEditorFactory,
                        List<FormSubmitListener> submitListeners) {
      super("rows", new GlobListModel(type, matcher, repository, directory));
      this.editPolicy = editPolicy;
      this.columns = columns;
      this.tableId = tableId;
      this.rowEditorFactory = rowEditorFactory;
      this.submitListeners = submitListeners;
      setRenderBodyOnly(true);
    }

    protected void populateItem(ListItem rowItem) {
      final Glob glob = (Glob)rowItem.getModelObject();

      GlobPage page = (GlobPage)getPage();
      GlobRepository repository = page.getRepository();
      DescriptionService descriptionService = page.getDescriptionService();

      if (editPolicy == TableEditPolicy.ROW) {
        rowItem.add(new FormRowPanel("rowContent", tableId, glob, columns, rowItem.getIndex(), tableId,
                                     rowEditorFactory, submitListeners, repository, descriptionService));
      }
      else {
        RowPanel row = new RowPanel("rowContent", tableId, glob, columns, rowItem.getIndex(),
                                    rowEditorFactory, submitListeners, repository, descriptionService);
        if (editPolicy == TableEditPolicy.TABLE) {
          submittables.add(row);
        }
        rowItem.add(row);
      }
      rowItem.setRenderBodyOnly(true);
    }
  }
}
