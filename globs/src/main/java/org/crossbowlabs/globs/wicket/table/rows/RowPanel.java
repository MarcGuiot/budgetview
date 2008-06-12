package org.crossbowlabs.globs.wicket.table.rows;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.utils.DefaultFieldValues;
import org.crossbowlabs.globs.wicket.FormSubmitListener;
import org.crossbowlabs.globs.wicket.GlobPage;
import org.crossbowlabs.globs.wicket.table.GlobTableColumn;
import org.crossbowlabs.globs.wicket.table.GlobTableRowEditor;
import org.crossbowlabs.globs.wicket.table.GlobTableRowEditorFactory;
import wicket.AttributeModifier;
import wicket.Component;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.Panel;
import wicket.model.Model;

import java.util.List;

public class RowPanel extends Panel implements Submittable {
  private Key key;
  private DefaultFieldValues values;
  private List<FormSubmitListener> submitListeners;
  private Component editionPanel;
  private Component switcher;
  private String tableId;

  private static final String EVEN_CLASS = "even";
  private static final String ODD_CLASS = "odd";

  public RowPanel(String parentId,
                  String tableId,
                  final Glob glob,
                  List<GlobTableColumn> columns,
                  final int rowIndex,
                  GlobTableRowEditorFactory rowEditorFactory,
                  List<FormSubmitListener> submitListeners,
                  GlobRepository repository,
                  DescriptionService descriptionService) {
    super(parentId);
    this.key = glob.getKey();
    this.values = new DefaultFieldValues(glob);
    this.submitListeners = submitListeners;
    this.tableId = tableId;
    setRenderBodyOnly(true);

    Row row = new Row(columns, rowIndex);
    add(row);

    GlobTableRowEditor rowEditor = rowEditorFactory.getEditor("cell", "editionPanel", key, values,
                                                              row, rowIndex, repository, descriptionService);
    editionPanel = rowEditor.getEditionPanel();
    editionPanel.setVisible(false);

    switcher = rowEditor.getSwitcher();

    WebMarkupContainer rowEditorContainer = new WebMarkupContainer("rowEditor") {
      public boolean isVisible() {
        return editionPanel.isVisible();
      }
    };
    rowEditorContainer.add(new AttributeModifier("colspan", new Model(columns.size())));
    rowEditorContainer.add(editionPanel);

    row.add(rowEditorContainer);
  }

  public void submit() {
    GlobPage page = (GlobPage)getPage();
    GlobRepository repository = page.getRepository();
    repository.enterBulkDispatchingMode();
    try {
      repository.update(key, values.toArray());
      for (FormSubmitListener listener : submitListeners) {
        listener.onSubmit(key, values, repository);
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  private String getCssClass(int rowIndex) {
    return rowIndex % 2 == 0 ? EVEN_CLASS : ODD_CLASS;
  }

  private class Row extends WebMarkupContainer {
    private Row(List<GlobTableColumn> columns, final int rowIndex) {
      super("row");
      setOutputMarkupId(true);

      add(new AttributeModifier("class", true, new Model(getCssClass(rowIndex))));
      add(new RowRenderer(columns, rowIndex));
    }
  }

  private class RowRenderer extends WebMarkupContainer {
    private RowRenderer(List<GlobTableColumn> columns, int rowIndex) {
      super("rowRenderer");
      setRenderBodyOnly(true);
      add(new Columns(columns, rowIndex));
    }

    public boolean isVisible() {
      return !editionPanel.isVisible();
    }
  }

  private class Columns extends ListView {
    private final int rowIndex;

    private Columns(List<GlobTableColumn> columns, int rowIndex) {
      super("columns", columns);
      this.rowIndex = rowIndex;
    }

    protected void populateItem(final ListItem cellItem) {
      GlobPage page = (GlobPage)getPage();
      GlobRepository repository = page.getRepository();
      DescriptionService descriptionService = page.getDescriptionService();

      GlobTableColumn column = (GlobTableColumn)cellItem.getModelObject();
      if (column instanceof GlobTableRowEditorColumnAdapter) {
        cellItem.add(switcher);
      }
      else {
        Component child =
          column.getComponent("cell", tableId, key, values, rowIndex, this, repository, descriptionService);
        child.setRenderBodyOnly(true);
        cellItem.add(child);
      }
    }
  }
}
