package org.crossbowlabs.globs.gui;

import org.crossbowlabs.globs.gui.actions.CreateGlobAction;
import org.crossbowlabs.globs.gui.actions.DeleteGlobAction;
import org.crossbowlabs.globs.gui.editors.GlobNumericEditor;
import org.crossbowlabs.globs.gui.editors.GlobPasswordEditor;
import org.crossbowlabs.globs.gui.editors.GlobTextEditor;
import org.crossbowlabs.globs.gui.views.GlobComboView;
import org.crossbowlabs.globs.gui.views.GlobLabelView;
import org.crossbowlabs.globs.gui.views.GlobListView;
import org.crossbowlabs.globs.gui.views.GlobTableView;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.DoubleField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.IconLocator;
import org.crossbowlabs.splits.SplitsBuilder;
import org.crossbowlabs.splits.TextLocator;
import org.crossbowlabs.splits.color.ColorService;

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

  private GlobsPanelBuilder(GlobRepository repository, Directory directory) {
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

  public GlobLabelView addLabel(GlobType type, GlobLabelView.Stringifier stringifier) {
    return store(GlobLabelView.init(type, repository, directory, stringifier));
  }

  public GlobsPanelBuilder add(String name, Component component) {
    splits.add(name, component);
    return this;
  }

  public GlobsPanelBuilder add(String name, Action action) {
    splits.add(name, action);
    return this;
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