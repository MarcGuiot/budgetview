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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;

public class GlobComboView extends AbstractGlobComponentHolder<GlobComboView> implements GlobSelectionListener {
  private ListCellRenderer renderer;
  private Comparator<Glob> comparator;
  private GlobSelectionHandler selectionHandler = new DefaultSelectionHandler();
  private Model model;
  private JComboBox jComboBox;
  private boolean showEmptyOption = false;
  private boolean updateWithIncomingSelections = true;
  private boolean selectionEnabled = true;
  private String name;
  private String emptyOptionLabel = "";
  private GlobMatcher matcher;
  private GlobViewModel.Listener listener = new GlobViewModel.NullListener();

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

  public GlobComboView setListener(GlobViewModel.Listener listener) {
    this.listener = listener;
    return this;
  }

  public GlobComboView setRenderer(ListCellRenderer renderer, Comparator<Glob> comparator) {
    this.renderer = renderer;
    this.comparator = comparator;
    return this;
  }

  public GlobComboView setComparator(Comparator<Glob> comparator) {
    this.comparator = comparator;
    if (model != null) {
      this.model.updateSorting();
    }
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
    if (model != null) {
      model.setShowEmptyOption(showEmptyOption);
      if (model.model.size() != 0) {
        jComboBox.setSelectedIndex(0);
      }
    }
    return this;
  }

  public GlobComboView setName(String name) {
    this.name = name;
    if (jComboBox != null) {
      jComboBox.setName(name);
    }
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

  public GlobComboView setEmptyOptionLabel(String emptyOptionLabel) {
    this.emptyOptionLabel = emptyOptionLabel;
    return this;
  }

  public void setEnabled(boolean enable) {
    getComponent().setEnabled(enable);
  }

  public void setVisible(boolean visible) {
    getComponent().setVisible(visible);
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
      setRenderer(new StringListCellRenderer(stringifier, repository) {
        protected String getValueForNull() {
          return emptyOptionLabel;
        }
      }, comparator);
    }
    if (jComboBox == null) {
      jComboBox = new JComboBox();
    }
    if (updateWithIncomingSelections) {
      selectionService.addListener(this, type);
    }
  }

  public GlobComboView setFilter(GlobMatcher matcher) {
    if (jComboBox == null) {
      this.matcher = matcher;
    }
    else {
      Glob glob = (Glob)jComboBox.getSelectedItem();
      model.model.setFilter(matcher, true);
      if (glob != null && matcher.matches(glob, repository)) {
        jComboBox.setSelectedItem(glob);
      }
      if ((glob == null || !matcher.matches(glob, repository)) && model.model.size() != 0) {
        jComboBox.setSelectedIndex(0);
      }
    }
    return this;
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
          listener.globInserted(index);
          fireIntervalAdded(this, index, index);
        }

        public void globUpdated(int index) {
          listener.globUpdated(index);
          fireContentsChanged(this, index, index);
        }

        public void globRemoved(int index) {
          listener.globRemoved(index);
          fireIntervalRemoved(this, index, index);
        }

        public void globMoved(int previousIndex, int newIndex) {
          listener.globMoved(previousIndex, newIndex);
          globRemoved(previousIndex);
          globInserted(newIndex);
        }

        public void globListPreReset() {
          listener.globListPreReset();
        }

        public void globListReset() {
          listener.globListReset();
          fireContentsChanged(this, 0, model != null ? model.size() : 0);
          if (model != null) {
            if (((selected == null) || (model.indexOf(selected) < 0)) && (model.size() > 0)) {
              selected = null;
            }
          }
        }

        public void startUpdate() {
          listener.startUpdate();
        }

        public void updateComplete() {
          listener.updateComplete();
        }
      });
      if (matcher != null) {
        model.setFilter(matcher, false);
      }
      if (model.size() > 0) {
        selected = model.get(0);
      }
    }

    public void setShowEmptyOption(boolean shown) {
      model.setShowNullElement(shown);
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

    public void updateSorting() {
      model.sort(comparator);
    }

    public int indexOf(Glob glob) {
      return model.indexOf(glob);
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
