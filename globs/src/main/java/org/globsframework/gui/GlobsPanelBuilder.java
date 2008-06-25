package org.globsframework.gui;

import org.globsframework.gui.actions.CreateGlobAction;
import org.globsframework.gui.actions.DeleteGlobAction;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.editors.GlobPasswordEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GlobsPanelBuilder extends SplitsBuilder {
  private GlobRepository repository;
  private Directory directory;
  private List<ComponentHolder> componentHolders = new ArrayList<ComponentHolder>();

  public GlobsPanelBuilder(Class referenceClass, String file, GlobRepository repository, Directory directory) {
    super(directory);
    this.repository = repository;
    this.directory = directory;
    super.setSource(referenceClass, file);
  }

  public GlobTableView addTable(GlobType type, Comparator<Glob> comparator) {
    return store(GlobTableView.init(type, repository, comparator, directory));
  }

  public GlobListView addList(GlobType type) {
    return store(GlobListView.init(type, repository, directory));
  }

  public GlobComboView addCombo(GlobType type) {
    return store(GlobComboView.init(type, repository, directory));
  }

  public GlobComboView addCombo(String name, GlobType type) {
    return addCombo(type).setName(name);
  }

  public GlobLinkComboEditor addComboEditor(Link field) {
    return store(new GlobLinkComboEditor(field, repository, directory));
  }

  public GlobTextEditor addEditor(StringField field) {
    return store(GlobTextEditor.init(field, repository, directory));
  }

  public GlobNumericEditor addEditor(DoubleField field) {
    return store(GlobNumericEditor.init(field, repository, directory));
  }

  public void addEditors(StringField... fields) {
    for (StringField field : fields) {
      addEditor(field);
    }
  }

  public GlobPasswordEditor addPassword(StringField field) {
    return store(GlobPasswordEditor.init(field, repository, directory));
  }

  public GlobLabelView addLabel(GlobType type, GlobListStringifier stringifier) {
    return store(GlobLabelView.init(type, repository, directory, stringifier));
  }

  public GlobsPanelBuilder add(String name, ComponentHolder holder) {
    holder.setName(name);
    store(holder);
    return this;
  }

  public GlobsPanelBuilder addCreateAction(String label, String name, GlobType type) {
    add(name, new CreateGlobAction(label, type, repository, directory));
    return this;
  }

  public GlobsPanelBuilder addDeleteAction(String label, String name, GlobType type) {
    add(name, new DeleteGlobAction(label, type, repository, directory));
    return this;
  }

  private <T extends ComponentHolder> T store(T component) {
    componentHolders.add(component);
    return component;
  }

  protected void complete() {
    for (ComponentHolder componentHolder : componentHolders) {
      add(componentHolder.getComponent());
    }
    componentHolders.clear();
  }
}