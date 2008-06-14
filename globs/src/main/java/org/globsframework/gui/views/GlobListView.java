package org.globsframework.gui.views;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.views.impl.StringListCellRenderer;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class GlobListView extends AbstractGlobComponentHolder implements GlobSelectionListener {
  private ListCellRenderer renderer;
  private Comparator<Glob> comparator;
  private GlobSelectionHandler selectionHandler = new DefaultGlobSelectionHandler();
  private Model model;
  private JList jList;
  private boolean updateWithIncomingSelections = true;
  private boolean showEmptyOption = false;
  private boolean selectionEnabled = true;

  public static GlobListView init(GlobType type, GlobRepository repository, Directory directory) {
    return new GlobListView(type, repository, directory);
  }

  public GlobListView(GlobType type, GlobRepository repository, Directory directory) {
    super(type, repository, directory);
  }

  public GlobListView setSelectionHandler(GlobSelectionHandler selectionHandler) {
    this.selectionHandler = selectionHandler;
    return this;
  }

  public void selectionUpdated(GlobSelection selection) {
    Set newSelection = new HashSet(selection.getAll(type));
    Set currentSelection = new HashSet(getCurrentSelection());
    if (!newSelection.equals(currentSelection)) {
      try {
        selectionEnabled = false;
        select(newSelection);
      }
      finally {
        selectionEnabled = true;
      }
    }
  }

  public GlobListView setUpdateWithIncomingSelections(boolean updateWithIncomingSelections) {
    this.updateWithIncomingSelections = updateWithIncomingSelections;
    return this;
  }

  public interface GlobSelectionHandler {
    void processSelection(GlobList selection);
  }

  public GlobListView setBaseList(JList jList) {
    this.jList = jList;
    return this;
  }

  public GlobListView setRenderer(Field field) {
    GlobStringifier stringifier = descriptionService.getStringifier(field);
    return setRenderer(stringifier);
  }

  public GlobListView setRenderer(GlobStringifier stringifier) {
    return setRenderer(new StringListCellRenderer(stringifier, repository),
                       stringifier.getComparator(repository));
  }

  public GlobListView setRenderer(ListCellRenderer renderer, Comparator<Glob> comparator) {
    this.renderer = renderer;
    this.comparator = comparator;
    return this;
  }

  public JList getComponent() {
    complete();
    jList.setName(type.getName());
    jList.setModel(model);
    jList.setCellRenderer(renderer);
    registerSelectionListener();
    if (updateWithIncomingSelections) {
      selectionService.addListener(this, type);
    }
    return jList;
  }

  private void registerSelectionListener() {
    jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    jList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent event) {
        if (jList.getSelectionModel().getValueIsAdjusting()) {
          return;
        }
        GlobList selection = getCurrentSelection();
        selectionHandler.processSelection(selection);
      }
    });
  }

  public GlobList getCurrentSelection() {
    GlobList selection = new GlobList();
    for (int index : jList.getSelectedIndices()) {
      selection.add(model.getElementAt(index));
    }
    return selection;
  }

  public void selectFirst() {
    jList.setValueIsAdjusting(true);
    try {
      jList.clearSelection();
      if (jList.getModel().getSize() > 0) {
        jList.addSelectionInterval(0, 0);
      }
    }
    finally {
      jList.setValueIsAdjusting(false);
    }
  }

  public void select(Glob... globs) {
    select(Arrays.asList(globs));
  }

  public void select(Iterable<Glob> globs) {
    jList.setValueIsAdjusting(true);
    try {
      jList.clearSelection();
      for (Glob glob : globs) {
        int index = model.model.indexOf(glob);
        if (index >= 0) {
          jList.addSelectionInterval(index, index);
        }
      }
    }
    finally {
      jList.setValueIsAdjusting(false);
    }
  }

  private void complete() {
    if (renderer == null) {
      GlobStringifier stringifier = descriptionService.getStringifier(type);
      setRenderer(new StringListCellRenderer(stringifier, repository), stringifier.getComparator(repository));
    }
    if (model == null) {
      model = new Model();
    }
    if (jList == null) {
      jList = new JList();
    }
  }

  public void setFilter(GlobMatcher matcher) {
    complete();
    model.model.setFilter(matcher);
  }

  public Glob getGlobAt(int index) {
    return model.getElementAt(index);
  }

  public void clear() {
    model.clear();
  }

  public GlobListView setShowEmptyOption(boolean enabled) {
    this.showEmptyOption = enabled;
    return this;
  }

  private class Model extends AbstractListModel {
    private GlobViewModel model;

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
        }
      });
    }

    public int getSize() {
      return model.size();
    }

    public Glob getElementAt(int index) {
      return model.get(index);
    }

    public void clear() {
      model.clear();
    }

    public void dispose() {
      model.dispose();
    }
  }

  private class DefaultGlobSelectionHandler implements GlobSelectionHandler {
    public void processSelection(GlobList selection) {
      if (selectionEnabled) {
        selectionService.select(selection, type);
      }
    }
  }

  public void dispose() {
    model.dispose();
  }
}
