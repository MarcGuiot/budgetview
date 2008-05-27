package org.crossbowlabs.globs.wicket.table;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import org.crossbowlabs.globs.wicket.FormSubmitListener;
import org.crossbowlabs.globs.wicket.GlobPage;
import org.crossbowlabs.globs.wicket.model.GlobListModel;
import org.crossbowlabs.globs.wicket.table.rows.FormRowPanel;
import org.crossbowlabs.globs.wicket.table.rows.RowPanel;
import org.crossbowlabs.globs.wicket.table.rows.Submittable;
import wicket.AttributeModifier;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

class GlobTable extends Panel implements Submittable {
  private List<GlobTableColumn> columns;
  private List<Submittable> submittables = new ArrayList<Submittable>();

  private static final String TABLE_CLASS = "content";

  GlobTable(final String tableId,
            final GlobType type,
            final List<GlobTableColumn> columns,
            final GlobMatcher matcher,
            final TableEditPolicy editPolicy,
            GlobTableRowEditorFactory rowEditorFactory,
            final List<FormSubmitListener> submitListeners) {
    super(tableId);
    this.columns = columns;
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
    repository.enterBulkDispatchingMode();
    try {
      for (Submittable submittable : submittables) {
        submittable.submit();
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
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
      super("rows", new GlobListModel(type, matcher));
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
