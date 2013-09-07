package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.gui.views.GlobViewModel;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Comparator;
import java.util.Set;

public class GlobLinkComboEditor
  extends AbstractGlobComponentHolder
  implements ChangeSetListener, GlobSelectionListener {

  private Link link;
  private GlobComboView globComboView;
  private Key currentKey;
  private boolean updateInProgress = false;
  private boolean forcedEnabled = true;
  private Key forcedSelectionKey = null;
  private boolean completed = false;
  private JComboBox component;

  public static GlobLinkComboEditor init(final Link link, final GlobRepository repository, Directory directory) {
    return new GlobLinkComboEditor(link, repository, directory);
  }

  public GlobLinkComboEditor(final Link link, final GlobRepository repository, Directory directory) {
    super(link.getTargetType(), repository, directory);
    this.link = link;
    selectionService.addListener(this, link.getSourceType());

    globComboView =
      GlobComboView.init(link.getTargetType(), repository, directory)
        .setShowEmptyOption(true)
        .setUpdateWithIncomingSelections(false)
        .setSelectionHandler(new GlobComboView.GlobSelectionHandler() {
          public void processSelection(Glob glob) {
            if (currentKey == null) {
              return;
            }
            if (!repository.contains(currentKey)) {
              currentKey = null;
              return;
            }
            if (!updateInProgress) {
              repository.setTarget(currentKey, link, glob != null ? glob.getKey() : null);
            }
          }
        })
    .setListener(new GlobLinkListener());

    repository.addChangeListener(this);

    updateSelection();
  }

  Runnable action = null;

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (forcedSelectionKey != null &&
        currentKey == null &&
        changeSet.containsChanges(link.getSourceType())) {
      action = new Runnable() {
        public void run() {
          Glob glob = repository.find(forcedSelectionKey);
          if (glob != null) {
            select(new GlobList(glob));
          }
        }
      };
      return;
    }

    if (currentKey == null || !changeSet.containsChanges(currentKey)) {
      return;
    }

    action = new Runnable() {
      public void run() {
        Glob source = repository.find(currentKey);
        if (source == null) {
          setTarget(null);
          globComboView.setEnabled(false);
          return;
        }

        Glob target = repository.findLinkTarget(source, link);
        setTarget(target);

        globComboView.setEnabled(forcedEnabled);
      }
    };
  }

  public void globsReset(final GlobRepository repository, Set<GlobType> changedTypes) {
    if (!changedTypes.contains(link.getSourceType())) {
      return;
    }
    action = new Runnable() {
      public void run() {
        if (forcedSelectionKey != null) {
          Glob glob = repository.find(forcedSelectionKey);
          if (glob != null) {
            select(new GlobList(glob));
            return;
          }
        }
        select(GlobList.EMPTY);
      }
    };
  }

  private void setTarget(Glob target) {
    try {
      updateInProgress = true;
      globComboView.select(target);
    }
    finally {
      updateInProgress = false;
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    if (forcedSelectionKey != null) {
      return;
    }
    updateInProgress = true;
    try {
      GlobList globs = selection.getAll(link.getSourceType());
      select(globs);
    }
    finally {
      updateInProgress = false;
    }
  }

  private void select(GlobList globs) {
    if (globs.size() != 1) {
      setSelectedGlob(null);
    }
    else {
      setSelectedGlob(globs.get(0));
    }
  }

  public GlobLinkComboEditor setFilter(final GlobMatcher filter) {
    globComboView.setFilter(filter);
    return this;
  }

  public GlobLinkComboEditor setRenderer(Field field) {
    globComboView.setRenderer(field);
    return this;
  }

  public GlobLinkComboEditor setRenderer(GlobStringifier stringifier) {
    globComboView.setRenderer(stringifier);
    if (completed) {
      updateSelection();
    }
    return this;
  }

  public GlobLinkComboEditor setRenderer(ListCellRenderer renderer, Comparator<Glob> comparator) {
    globComboView.setRenderer(renderer, comparator);
    if (completed) {
      updateSelection();
    }
    return this;
  }

  public GlobLinkComboEditor setComparator(final Comparator<Glob> comparator) {
    globComboView.setComparator(comparator);
    if (completed) {
      updateSelection();
    }
    return this;
  }

  public GlobLinkComboEditor setShowEmptyOption(boolean showEmpty) {
    globComboView.setShowEmptyOption(showEmpty);
    if (completed) {
      updateSelection();
    }
    return this;
  }

  public GlobLinkComboEditor setEmptyOptionLabel(String label) {
    globComboView.setEmptyOptionLabel(label);
    if (completed) {
      updateSelection();
    }
    return this;
  }

  private void setSelectedGlob(Glob glob) {
    this.currentKey = (glob == null) ? null : glob.getKey();
    globComboView.setEnabled(forcedEnabled && (currentKey != null));
    setTarget(glob == null ? null : repository.findLinkTarget(glob, link));
  }

  public GlobLinkComboEditor forceSelection(Key key) {
    forcedSelectionKey = key;
    selectionService.removeListener(this);
    Glob glob = repository.find(key);
    if (glob != null) {
      setSelectedGlob(glob);
    }
    return this;
  }

  public GlobLinkComboEditor setName(String name) {
    globComboView.setName(name);
    return this;
  }

  public JComboBox getComponent() {
    if (!completed) {
      updateSelection();
    }
    if (component == null){
      component = globComboView.getComponent();
      // listener must be registered after the one from globComboView
//      repository.addChangeListener(this);
    }
    return component;
  }

  private void updateSelection() {
    select(selectionService.getSelection(link.getSourceType()));
    this.completed = true;
  }

  public void setEnabled(boolean enabled) {
    this.forcedEnabled = enabled;
    globComboView.setEnabled(forcedEnabled && (currentKey != null));
  }

  public void setVisible(boolean visible) {
    globComboView.setVisible(visible);
  }

  public void dispose() {
    if (globComboView != null) {
      repository.removeChangeListener(this);
      selectionService.removeListener(this);
      globComboView.dispose();
      globComboView = null;
    }
  }

  private class GlobLinkListener implements GlobViewModel.Listener {

    public void globInserted(int index) {
      runAction();
    }

    public void globUpdated(int index) {
      runAction();
    }

    public void globRemoved(int index) {
      runAction();
    }

    public void globMoved(int previousIndex, int newIndex) {
      runAction();
    }

    public void globListPreReset() {
    }

    public void globListReset() {
      runAction();
    }

    public void startUpdate() {
    }

    public void updateComplete() {
    }
  }

  private void runAction() {
    if (action != null) {
      Runnable tmp = action;
      action = null;
      tmp.run();
    }
  }
}