package org.globsframework.gui;

import org.globsframework.gui.actions.CreateGlobAction;
import org.globsframework.gui.actions.DeleteGlobAction;
import org.globsframework.gui.editors.*;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.views.*;
import org.globsframework.gui.views.utils.GlobRepeatUpdater;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobListFunctor;
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

  public GlobListView addList(String name, GlobType type) {
    return store(GlobListView.init(type, repository, directory).setName(name));
  }

  public GlobComboView addCombo(GlobType type) {
    return store(GlobComboView.init(type, repository, directory));
  }

  public GlobComboView addCombo(String name, GlobType type) {
    return addCombo(type).setName(name);
  }

  public GlobLinkComboEditor addComboEditor(String name, Link field) {
    return store(GlobLinkComboEditor.init(field, repository, directory).setName(name));
  }

  public GlobComboEditor addComboEditor(String name, IntegerField field, int[] values) {
    return store(GlobComboEditor.init(field, values, repository, directory).setName(name));
  }

  public GlobComboEditor addComboEditor(String name, Key key, IntegerField field, int[] values) {
    return store(GlobComboEditor.init(field, values, repository, directory).forceKey(key).setName(name));
  }

  public GlobCheckBoxView addCheckBox(String name, BooleanField field) {
    GlobCheckBoxView boxView = store(GlobCheckBoxView.init(field, repository, directory));
    boxView.setName(name);
    return boxView;
  }

  public GlobToggleEditor addToggleEditor(String name, BooleanField field) {
    GlobToggleEditor toggleEditor = store(GlobToggleEditor.init(field, repository, directory));
    toggleEditor.setName(name);
    return toggleEditor;
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

  public GlobMultiLineTextEditor addMultiLineEditor(String name, StringField field) {
    return store(GlobMultiLineTextEditor.init(field, repository, directory).setName(name));
  }

  public GlobSliderEditor addSlider(String name, DoubleField field, GlobSliderAdapter adapter) {
    return store(GlobSliderEditor.init(field, repository,directory, adapter).setName(name));
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

  public GlobLabelView addLabel(String name, Link link) {
    return store(GlobLabelView.init(link, repository, directory)).setName(name);
  }

  public GlobLabelView addLabel(String name, LinkField link) {
    return store(GlobLabelView.init(link, repository, directory)).setName(name);
  }

  public GlobLabelView addLabel(String name, LinkField link, String textForEmptySelection, String textForMultipleValues) {
    return store(GlobLabelView.init(link, textForEmptySelection, textForMultipleValues, repository, directory))
      .setName(name);
  }

  public GlobMultiLineTextView addMultiLineTextView(String name, GlobType type) {
    return addMultiLineTextView(name, type, directory.get(DescriptionService.class).getListStringifier(type));
  }

  public GlobMultiLineTextView addMultiLineTextView(String name, GlobType type, GlobListStringifier stringifier) {
    return store(GlobMultiLineTextView.init(type, repository, directory, stringifier).setName(name));
  }

  public GlobHtmlView addHtmlView(String name, GlobType type, GlobListStringifier stringifier) {
    return store(GlobHtmlView.init(type, repository, directory, stringifier).setName(name));
  }

  public GlobButtonView addButton(String name, GlobType type, GlobListStringifier stringifier, GlobListFunctor callback) {
    return store(GlobButtonView.init(type, repository, directory, stringifier, callback).setName(name));
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
                              RepeatComponentFactory<Glob> factory) {
    final GlobStringifier stringifier = directory.get(DescriptionService.class).getStringifier(type);
    return addRepeat(name, type, matcher, stringifier.getComparator(repository), factory);
  }

  public GlobRepeat addRepeat(String name, final GlobType type, GlobMatcher matcher,
                              Comparator<Glob> comparator, RepeatComponentFactory<Glob> factory) {
    return addRepeat(name, type, matcher, comparator, repository, this, factory);
  }

  public static GlobRepeat addRepeat(String name, final GlobType type, GlobMatcher matcher,
                                     Comparator<Glob> comparator, GlobRepository repository, SplitsBuilder builder,
                                     RepeatComponentFactory<Glob> factory) {
    GlobRepeatUpdater updater = new GlobRepeatUpdater();
    GlobViewModel model = new GlobViewModel(type, repository, comparator, updater);
    model.setFilter(matcher, true);
    Repeat<Glob> repeat = builder.addRepeat(name, model.getAll(), factory);
    builder.addDisposable(model);
    updater.set(model, repeat);
    builder.addDisposable(updater);
    return updater;
  }

  public static GlobRepeat addRepeat(String name, final GlobType type, GlobMatcher matcher,
                                     Comparator<Glob> comparator, GlobRepository repository, PanelBuilder builder,
                                     RepeatComponentFactory<Glob> factory) {
    GlobRepeatUpdater updater = new GlobRepeatUpdater();
    final GlobViewModel model = new GlobViewModel(type, repository, comparator, updater);
    model.setFilter(matcher, true);
    Repeat<Glob> repeat = builder.addRepeat(name, model.getAll(), factory);
    builder.addDisposable(model);
    updater.set(model, repeat);
    builder.addDisposable(updater);
    return updater;
  }

  private <T extends ComponentHolder> T store(T component) {
    componentHolders.add(component);
    addDisposable(component);
    return component;
  }

  protected void completeBeforeLoad() {
    super.completeBeforeLoad();
    for (ComponentHolder componentHolder : componentHolders) {
      add(componentHolder.getComponent());
    }
    componentHolders.clear();
  }

}