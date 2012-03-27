package org.globsframework.gui.views;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.views.impl.StringListCellRenderer;
import org.globsframework.gui.views.utils.GlobViewUtils;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class GlobListView extends AbstractGlobComponentHolder<GlobListView> implements GlobSelectionListener {
  private JList jList = new JList();
  private Model model;
  private String name;

  private GlobStringifier stringifier;
  private ListCellRenderer renderer;
  private Comparator<Glob> comparator;

  private GlobSelectionHandler selectionHandler = new DefaultGlobSelectionHandler();

  private boolean updateWithIncomingSelections = true;
  private boolean showEmptyOption = false;
  private boolean selectionEnabled = true;
  private boolean singleSelectionMode = false;

  private Action doubleClickAction;

  public static GlobListView init(GlobType type, GlobRepository repository, Directory directory) {
    return new GlobListView(type, repository, directory);
  }

  public GlobListView(GlobType type, GlobRepository repository, Directory directory) {
    super(type, repository, directory);
    stringifier = descriptionService.getStringifier(type);
  }

  public GlobListView setComparator(Comparator<Glob> comparator) {
    this.comparator = comparator;
    return this;
  }

  public GlobListView setSelectionHandler(GlobSelectionHandler selectionHandler) {
    this.selectionHandler = selectionHandler;
    return this;
  }

  GlobType getType() {
    return type;
  }

  public void selectionUpdated(GlobSelection selection) {
    Set<Glob> newSelection = new HashSet<Glob>(selection.getAll(type));
    Set<Glob> currentSelection = new HashSet<Glob>(getCurrentSelection());
    if (!newSelection.equals(currentSelection)) {
      selectSilently(newSelection);
    }
  }

  private void selectSilently(Collection<Glob> newSelection) {
    try {
      selectionEnabled = false;
      select(newSelection);
    }
    finally {
      selectionEnabled = true;
    }
  }

  public GlobListView setSingleSelectionMode() {
    this.singleSelectionMode = true;
    return this;
  }

  public GlobListView setUpdateWithIncomingSelections(boolean updateWithIncomingSelections) {
    this.updateWithIncomingSelections = updateWithIncomingSelections;
    return this;
  }

  public int getSize() {
    return model.getSize();
  }

  public String toString(Glob item) {
    if (stringifier == null) {
      throw new InvalidState("No stringifier defined");
    }
    return stringifier.toString(item, repository);
  }

  GlobRepository getRepository() {
    return repository;
  }

  public interface GlobSelectionHandler {
    void processSelection(GlobList selection);
  }

  public GlobListView setRenderer(Field field) {
    GlobStringifier stringifier = descriptionService.getStringifier(field);
    return setRenderer(stringifier);
  }

  public GlobListView setRenderer(GlobStringifier stringifier) {
    this.stringifier = stringifier;
    this.renderer = getDefaultRenderer(stringifier);
    this.comparator = stringifier.getComparator(repository);
    return this;
  }

  public GlobListView setRenderer(ListCellRenderer renderer, Comparator<Glob> comparator) {
    this.renderer = renderer;
    this.comparator = comparator;
    return this;
  }

  public GlobListView setName(String name) {
    this.name = name;
    return this;
  }

  public GlobListView addDoubleClickAction(final Action action) {
    this.doubleClickAction = action;
    this.jList.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          triggerDoubleClickAction();
        }
      }
    });
    return this;
  }

  void triggerDoubleClickAction() {
    if (doubleClickAction != null) {
      doubleClickAction.actionPerformed(new ActionEvent(jList, 0, "doubleClick"));
    }
  }

  public JList getComponent() {
    complete();
    jList.setName(name != null ? name : type.getName());
    jList.setModel(model);
    jList.setCellRenderer(renderer);
    registerSelectionListener();
    if (updateWithIncomingSelections) {
      selectionService.addListener(this, type);
    }
    return jList;
  }

  private void registerSelectionListener() {
    jList.setSelectionMode(
      singleSelectionMode ? ListSelectionModel.SINGLE_SELECTION : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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

  public void setVisible(boolean visible) {
    jList.setVisible(visible);
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
    if (jList.getModel().getSize() == 0) {
      selectionService.clear(type);
    }
  }

  public void select(Glob... globs) {
    select(Arrays.asList(globs));
  }

  public void select(Iterable<Glob> globs) {
    jList.setValueIsAdjusting(true);
    Integer firstIndex = null;
    try {
      jList.clearSelection();
      for (Glob glob : globs) {
        int index = model.model.indexOf(glob);
        if (index >= 0) {
          jList.addSelectionInterval(index, index);
          if (firstIndex == null) {
            firstIndex = index;
          }
        }
      }
    }
    finally {
      jList.setValueIsAdjusting(false);
    }
    if (firstIndex != null) {
      jList.scrollRectToVisible(jList.getCellBounds(firstIndex, firstIndex));
    }
  }

  private void complete() {
    if (renderer == null) {
      GlobStringifier stringifier = descriptionService.getStringifier(type);
      setRenderer(getDefaultRenderer(stringifier),
                  stringifier.getComparator(repository));
    }
    if (model == null) {
      model = new Model();
    }
  }

  private StringListCellRenderer getDefaultRenderer(GlobStringifier stringifier) {
    return new StringListCellRenderer(stringifier, repository).setUseWhiteSpaceForEmptyStrings(true);
  }

  public GlobListView setFilter(GlobMatcher matcher) {
    complete();

    GlobList selection = getCurrentSelection();
    int initialSize = selection.size();
    selection.filterSelf(matcher, repository);
    boolean selectionChanged = (initialSize != selection.size());

    model.model.setFilter(matcher, true);

    if (!selectionChanged) {
      selectSilently(selection);
    }

    return this;
  }

  GlobList getGlobList() {
    return model.model.getAll();
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

        private Key[] lastSelection;

        public void globInserted(int index) {
          fireIntervalAdded(this, index, index);
        }

        public void globUpdated(int index) {
          fireContentsChanged(this, index, index);
        }

        public void globRemoved(int index) {
          fireIntervalRemoved(this, index, index);
        }

        public void globMoved(int previousIndex, int newIndex) {

          selectionEnabled = false;
          GlobViewUtils.updateSelectionAfterItemMoved(jList.getSelectionModel(),
                                                      jList.getSelectedIndices(),
                                                      previousIndex, newIndex);
          selectionEnabled = true;
        }

        public void globListPreReset() {
          lastSelection = GlobListView.this.getCurrentSelection().getKeys();
        }

        public void globListReset() {
          fireContentsChanged(this, 0, model != null ? model.size() : 0);
          GlobList newSelection = new GlobList();
          for (Key key : lastSelection) {
            Glob glob = repository.find(key);
            if ((glob != null) && (model.indexOf(glob) >= 0)) {
              newSelection.add(glob);
            }
          }
          if (newSelection.size() != lastSelection.length) {
            select(newSelection);
          }
          else {
            selectSilently(newSelection);
          }
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
