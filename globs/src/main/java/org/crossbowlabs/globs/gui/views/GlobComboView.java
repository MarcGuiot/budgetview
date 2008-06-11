package org.crossbowlabs.globs.gui.views;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.utils.AbstractGlobComponentHolder;
import org.crossbowlabs.globs.gui.views.impl.StringListCellRenderer;
import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.model.utils.GlobMatcher;
import org.crossbowlabs.globs.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;

public class GlobComboView extends AbstractGlobComponentHolder implements GlobSelectionListener {
  private ListCellRenderer renderer;
  private Comparator<Glob> comparator;
  private GlobSelectionHandler selectionHandler = new DefaultSelectionHandler();
  private Model model;
  private JComboBox jComboBox;
  private boolean showEmptyOption = false;
  private boolean updateWithIncomingSelections = true;
  private boolean selectionEnabled = true;
  private String name;

  public static GlobComboView init(GlobType type, GlobRepository globRepository, Directory directory) {
    return new GlobComboView(type, globRepository, directory);
  }

  private GlobComboView(GlobType type, GlobRepository repository, Directory directory) {
    super(type, repository, directory);
  }

  public GlobComboView setRenderer(Field field) {
    GlobStringifier stringifier = descriptionService.getStringifier(field);
    return setRenderer(stringifier);
  }

  public GlobComboView setRenderer(GlobStringifier stringifier) {
    return setRenderer(new StringListCellRenderer(stringifier, repository), stringifier.getComparator(repository));
  }

  public GlobComboView setRenderer(ListCellRenderer renderer, Comparator<Glob> comparator) {
    this.renderer = renderer;
    this.comparator = comparator;
    return this;
  }

  public GlobComboView setComparator(Comparator<Glob> comparator) {
    this.comparator = comparator;
    return this;
  }

  public JComboBox getComponent() {
    if (jComboBox != null) {
      return jComboBox;
    }
    complete();
    jComboBox.setName(name == null ? type.getName() : name);
    model = new Model();
    jComboBox.setModel(model);
    jComboBox.setRenderer(renderer);
    registerSelectionListener();
    return jComboBox;
  }

  public GlobComboView setShowEmptyOption(boolean showEmptyOption) {
    this.showEmptyOption = showEmptyOption;
    return this;
  }

  public Glob getCurrentSelection() {
    return (Glob)jComboBox.getSelectedItem();
  }

  public void selectionUpdated(GlobSelection selection) {
    if (!selection.isRelevantForType(type)) {
      return;
    }
    try {
      selectionEnabled = false;
      GlobList all = selection.getAll(type);
      if (all.size() != 1) {
        select(null);
      }
      else {
        select(all.get(0));
      }
    }
    finally {
      selectionEnabled = true;
    }

  }

  public GlobComboView setUpdateWithIncomingSelections(boolean updateWithIncomingSelections) {
    this.updateWithIncomingSelections = updateWithIncomingSelections;
    return this;
  }

  public GlobComboView setName(String name) {
    this.name = name;
    return this;
  }

  public interface GlobSelectionHandler {
    void processSelection(Glob glob);
  }

  public GlobComboView setSelectionHandler(GlobSelectionHandler handler) {
    this.selectionHandler = handler;
    return this;
  }

  public void selectFirst() {
    if (model.getSize() > 0) {
      jComboBox.setSelectedIndex(0);
    }
  }

  public void select(Glob glob) {
    jComboBox.setSelectedItem(glob);
  }

  private void complete() {
    GlobStringifier stringifier = descriptionService.getStringifier(type);
    if (comparator == null) {
      comparator = stringifier.getComparator(repository);
    }
    if (renderer == null) {
      setRenderer(new StringListCellRenderer(stringifier, repository), comparator);
    }
    if (jComboBox == null) {
      jComboBox = new JComboBox();
    }
    if (updateWithIncomingSelections) {
      selectionService.addListener(this, type);
    }
  }

  public void setFilter(GlobMatcher matcher) {
    model.model.setFilter(matcher);
  }

  public Glob getGlobAt(int index) {
    return model.getElementAt(index);
  }

  private class Model extends AbstractListModel implements ComboBoxModel {
    private GlobViewModel model;
    private Glob selected;

    public Model() {
      model = new GlobViewModel(type, repository, comparator, showEmptyOption, new GlobViewModel.Listener() {
        public void globInserted(int index) {
          fireIntervalAdded(this, index, index);
        }

        public void globUpdated(int index) {
          fireContentsChanged(this, index, index);
        }

        public void globRemoved(int index) {
          fireIntervalRemoved(this, index, index);
        }

        public void globListReset() {
          fireContentsChanged(this, 0, model != null ? model.size() : 0);
          if (model != null) {
            if (((selected == null) || (model.indexOf(selected) < 0)) && (model.size() > 0)) {
              select(model.get(0));
            }
          }
        }
      });
      if (model.size() > 0) {
        selected = model.get(0);
      }
    }

    public void dispose() {
      model.dispose();
    }

    public int getSize() {
      return model.size();
    }

    public Glob getElementAt(int index) {
      if (index < 0) {
        return null;
      }
      return model.get(index);
    }

    public void setSelectedItem(Object object) {
      selected = (Glob)object;
    }

    public Object getSelectedItem() {
      return selected;
    }
  }

  public void dispose() {
    model.dispose();
  }

  private void registerSelectionListener() {
    jComboBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        Glob glob = (Glob)jComboBox.getSelectedItem();
        selectionHandler.processSelection(glob);
      }
    });
  }

  private class DefaultSelectionHandler implements GlobSelectionHandler {

    public void processSelection(Glob glob) {
      if (selectionEnabled) {
        selectionService.select((glob != null) ? Collections.singletonList(glob) : Collections.EMPTY_LIST, type);
      }
    }

  }
}
