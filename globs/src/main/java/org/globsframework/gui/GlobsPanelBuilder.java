package org.globsframework.gui;

import org.globsframework.gui.actions.CreateGlobAction;
import org.globsframework.gui.actions.DeleteGlobAction;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.editors.GlobPasswordEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.layout.CardHandler;
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

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GlobsPanelBuilder {
  private GlobRepository repository;
  private Directory directory;
  private List<ComponentHolder> componentHolders = new ArrayList<ComponentHolder>();
  private SplitsBuilder splits;

  public static GlobsPanelBuilder init(GlobRepository repository, Directory directory) {
    return new GlobsPanelBuilder(repository, directory);
  }

  public GlobsPanelBuilder(GlobRepository repository, Directory directory) {
    this.directory = directory;
    this.repository = repository;
    this.splits = new SplitsBuilder(directory.get(ColorService.class),
                                    directory.find(IconLocator.class),
                                    directory.find(TextLocator.class));
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

  public GlobsPanelBuilder add(String name, Component component) {
    splits.add(name, component);
    return this;
  }

  public GlobsPanelBuilder add(Component... component) {
    splits.add(component);
    return this;
  }


  public GlobsPanelBuilder add(String name, Action action) {
    splits.add(name, action);
    return this;
  }

  public CardHandler addCardHandler(String name) {
    return splits.addCardHandler(name);
  }

  public GlobsPanelBuilder addCreateAction(String label, String name, GlobType type) {
    splits.add(name, new CreateGlobAction(label, type, repository, directory));
    return this;
  }

  public GlobsPanelBuilder addDeleteAction(String label, String name, GlobType type) {
    splits.add(name, new DeleteGlobAction(label, type, repository, directory));
    return this;
  }

  private <T extends ComponentHolder> T store(T component) {
    componentHolders.add(component);
    return component;
  }

  public Component parse(InputStream stream) {
    complete();
    return splits.parse(stream);
  }

  public Component parse(Class targetClass, String resourceName) {
    complete();
    return splits.parse(targetClass, resourceName);
  }

  private void complete() {
    for (ComponentHolder componentHolder : componentHolders) {
      splits.add(componentHolder.getComponent());
    }
  }
}