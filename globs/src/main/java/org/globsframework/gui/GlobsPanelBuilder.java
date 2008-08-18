package org.globsframework.gui;

import org.globsframework.gui.actions.CreateGlobAction;
import org.globsframework.gui.actions.DeleteGlobAction;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.editors.GlobPasswordEditor;
import org.globsframework.gui.editors.GlobTextEditor;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.views.*;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatcher;
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

  public GlobTableView addTable(String name, GlobType type, Comparator<Glob> comparator) {
    return store(GlobTableView.init(type, repository, comparator, directory).setName(name));
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

  public GlobTextEditor addEditor(String name, StringField field) {
    final GlobTextEditor editor = addEditor(field);
    editor.setName(name);
    return editor;
  }

  public GlobTextEditor addEditor(StringField field) {
    return store(GlobTextEditor.init(field, repository, directory));
  }

  public GlobNumericEditor addEditor(String name, DoubleField field) {
    GlobNumericEditor numericEditor = store(GlobNumericEditor.init(field, repository, directory));
    numericEditor.setName(name);
    return numericEditor;
  }

  public GlobNumericEditor addEditor(DoubleField field) {
    return store(GlobNumericEditor.init(field, repository, directory));
  }

  public GlobNumericEditor addEditor(String name, IntegerField field) {
    GlobNumericEditor numericEditor = store(GlobNumericEditor.init(field, repository, directory));
    numericEditor.setName(name);
    return numericEditor;
  }

  public GlobNumericEditor addEditor(IntegerField field) {
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

  public GlobLabelView addLabel(String name, GlobType type, GlobListStringifier stringifier) {
    return store(GlobLabelView.init(type, repository, directory, stringifier)).setName(name);
  }

  public GlobLabelView addLabel(String name, Field field) {
    return store(GlobLabelView.init(field, repository, directory)).setName(name);
  }

  public GlobMultiLineTextView addMultiLineTextView(String name, GlobType type) {
    return addMultiLineTextView(name, type, directory.get(DescriptionService.class).getListStringifier(type));
  }

  public GlobMultiLineTextView addMultiLineTextView(String name, GlobType type, GlobListStringifier stringifier) {
    return store(GlobMultiLineTextView.init(type, repository, directory, stringifier).setName(name));
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

  public GlobRepeat addRepeat(String name, final GlobType type, GlobMatcher matcher,
                              RepeatComponentFactory factory) {
    final GlobStringifier stringifier = directory.get(DescriptionService.class).getStringifier(type);
    return addRepeat(name, type, matcher, stringifier.getComparator(repository), factory);
  }

  public GlobRepeat addRepeat(String name, final GlobType type, GlobMatcher matcher,
                              Comparator<Glob> comparator, RepeatComponentFactory factory) {
    return addRepeat(name, type, matcher, comparator, repository, this, factory);
  }

  public static GlobRepeat addRepeat(String name, final GlobType type, GlobMatcher matcher,
                                     Comparator<Glob> comparator, GlobRepository repository, SplitsBuilder builder,
                                     RepeatComponentFactory factory) {
    GlobRepeatListener listener = new GlobRepeatListener();
    GlobViewModel model = new GlobViewModel(type, repository, comparator, listener);
    model.setFilter(matcher);
    Repeat<Glob> repeat = builder.addRepeat(name, model.getAll(), factory);
    listener.set(model, repeat);
    return listener;
  }

  public static GlobRepeat addRepeat(String name, final GlobType type, GlobMatcher matcher,
                                     Comparator<Glob> comparator, GlobRepository repository, RepeatCellBuilder builder,
                                     RepeatComponentFactory factory) {
    GlobRepeatListener listener = new GlobRepeatListener();
    GlobViewModel model = new GlobViewModel(type, repository, comparator, listener);
    model.setFilter(matcher);
    Repeat<Glob> repeat = builder.addRepeat(name, model.getAll(), factory);
    listener.set(model, repeat);
    return listener;
  }

  private <T extends ComponentHolder> T store(T component) {
    componentHolders.add(component);
    return component;
  }

  protected void completeBeforeLoad() {
    super.completeBeforeLoad();
    for (ComponentHolder componentHolder : componentHolders) {
      add(componentHolder.getComponent());
    }
    componentHolders.clear();
  }

  private static class GlobRepeatListener implements GlobViewModel.Listener, GlobRepeat {
    private GlobViewModel model;
    private Repeat<Glob> repeat;

    public void globInserted(int index) {
      repeat.insert(model.get(index), index);
    }

    public void globUpdated(int index) {
    }

    public void globRemoved(int index) {
      repeat.remove(index);
    }

    public void globListPreReset() {
    }

    public void globListReset() {
      if ((repeat != null) && (model != null)) {
        repeat.set(model.getAll());
      }
    }

    public void set(GlobViewModel model, Repeat<Glob> repeat) {
      this.model = model;
      this.repeat = repeat;
    }

    public void setFilter(GlobMatcher matcher) {
      model.setFilter(matcher);
    }
  }
}