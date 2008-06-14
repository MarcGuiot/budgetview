package org.globsframework.wicket.table;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.exceptions.InvalidConfiguration;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.wicket.FormSubmitListener;
import org.globsframework.wicket.GlobSession;
import org.globsframework.wicket.table.columns.GlobFieldColumn;
import org.globsframework.wicket.table.columns.GlobFieldEditorColumn;
import org.globsframework.wicket.table.columns.GlobLinkColumn;
import org.globsframework.wicket.table.columns.SubmitColumn;
import org.globsframework.wicket.table.rows.DefaultRowEditorFactory;
import org.globsframework.wicket.table.rows.GlobTableRowEditorColumnAdapter;
import org.globsframework.wicket.table.wrapper.FormWrapper;
import wicket.Session;
import wicket.markup.html.panel.Panel;

import java.util.ArrayList;
import java.util.List;

public class GlobTableBuilder {
  private GlobType type;
  private List<GlobTableColumn> columns = new ArrayList<GlobTableColumn>();
  private List fields = new ArrayList();
  private GlobMatcher matcher = GlobMatchers.ALL;
  private TableEditPolicy editPolicy;
  private List<FormSubmitListener> submitListeners = new ArrayList<FormSubmitListener>();
  private GlobTableRowEditorFactory rowEditorFactory = GlobTableRowEditorFactory.NULL;

  public static GlobTableBuilder init(GlobType type, TableEditPolicy editPolicy) {
    return new GlobTableBuilder(type, editPolicy);
  }

  private GlobTableBuilder(GlobType type, TableEditPolicy editPolicy) {
    this.editPolicy = editPolicy;
    this.type = type;
  }

  public GlobTableBuilder setMatcher(GlobMatcher matcher) {
    this.matcher = matcher;
    return this;
  }

  public GlobTableBuilder add(Field field) {
    fields.add(field);
    GlobSession session = (GlobSession)Session.get();
    return add(new GlobFieldColumn(field, session.getDescriptionService()));
  }

  public GlobTableBuilder add(LinkField field) {
    return add((Link)field);
  }

  public GlobTableBuilder add(Link link) {
    fields.add(link);
    GlobSession session = (GlobSession)Session.get();
    return add(new GlobLinkColumn(link, session.getDescriptionService()));
  }

  public GlobTableBuilder addFieldEditor(Field field) {
    if (TableEditPolicy.READ_ONLY.equals(editPolicy)) {
      throw new InvalidParameter("Impossible to add a FieldEditor when editPolicy == READ_ONLY.");
    }
    fields.add(field);
    GlobSession session = (GlobSession)Session.get();
    return add(new GlobFieldEditorColumn(field, session.getDescriptionService()));
  }

  public GlobTableBuilder add(GlobTableColumn column) {
    columns.add(column);
    return this;
  }

  public GlobTableBuilder addSubmitButton(String columnName) {
    columns.add(new SubmitColumn(columnName));
    return this;
  }

  public GlobTableBuilder addSubmitListener(FormSubmitListener submitListener) {
    submitListeners.add(submitListener);
    return this;
  }

  public GlobTableBuilder addRowEditor(String columnName, GlobTableRowEditorFactory editorFactory) {
    if (this.rowEditorFactory != GlobTableRowEditorFactory.NULL) {
      throw new InvalidParameter("Only one row editor is supported");
    }
    this.rowEditorFactory = editorFactory;
    this.add(new GlobTableRowEditorColumnAdapter(columnName));
    return this;
  }

  public GlobTableBuilder addDefaultRowEditor(String columnName, String switcherText) throws InvalidConfiguration {
    if (!editPolicy.equals(TableEditPolicy.READ_ONLY)) {
      throw new InvalidConfiguration("DefaultRowEditor provides its own form and can only be used when the "
                                     + "table edit policy is " + TableEditPolicy.READ_ONLY.name());
    }
    addRowEditor(columnName, new DefaultRowEditorFactory(switcherText, fields));
    return this;
  }

  public Panel getPanel(String id) {
    if (editPolicy == TableEditPolicy.TABLE) {
      GlobTable table = createTable(FormWrapper.CONTENT_ID);
      return new FormWrapper(id, table, table);
    }
    return createTable(id);
  }

  private GlobTable createTable(String id) {
    return new GlobTable(id, type, columns, matcher, editPolicy, rowEditorFactory, submitListeners);
  }
}
