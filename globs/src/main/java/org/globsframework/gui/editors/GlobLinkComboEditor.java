package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.views.GlobComboView;
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

  public static GlobLinkComboEditor init(final Link link, final GlobRepository repository, Directory directory)  {
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
        });

    repository.addChangeListener(this);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (forcedSelectionKey != null && currentKey == null &&
        changeSet.containsChanges(link.getSourceType())) {
      Glob glob = repository.find(forcedSelectionKey);
      if (glob != null) {
        select(new GlobList(glob));
      }
    }

    if (currentKey == null || !changeSet.containsChanges(currentKey)) {
      return;
    }

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

  private void setTarget(Glob target) {
    try {
      updateInProgress = true;
      globComboView.select(target);
    }
    finally {
      updateInProgress = false;
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (!changedTypes.contains(link.getSourceType())) {
      return;
    }
    if (forcedSelectionKey != null) {
      Glob glob = repository.find(forcedSelectionKey);
      if (glob != null) {
        select(new GlobList(glob));
      }
    }
    else {
      select(GlobList.EMPTY);
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    updateInProgress = true;
    try {
      GlobList globs = selection.getAll(link.getTargetType());
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
    return this;
  }

  public GlobLinkComboEditor setRenderer(ListCellRenderer renderer, Comparator<Glob> comparator) {
    globComboView.setRenderer(renderer, comparator);
    return this;
  }

  public GlobLinkComboEditor setComparator(final Comparator<Glob> comparator) {
    globComboView.setComparator(comparator);
    return this;
  }

  public GlobLinkComboEditor setShowEmptyOption(boolean showEmpty) {
    globComboView.setShowEmptyOption(showEmpty);
    return this;
  }

  public GlobLinkComboEditor setEmptyOptionLabel(String label) {
    globComboView.setEmptyOptionLabel(label);
    return this;
  }

  private void setSelectedGlob(Glob glob) {
    this.currentKey = glob == null ? null : glob.getKey();
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
    super.setName(name);
    return this;
  }

  public JComboBox getComponent() {
    JComboBox jComboBox = globComboView.getComponent();
    if (forcedSelectionKey == null) {
      GlobList selection = selectionService.getSelection(type);
      select(selection);
    }
    return jComboBox;
  }

  public void setEnabled(boolean enabled) {
    this.forcedEnabled = enabled;
    globComboView.setEnabled(forcedEnabled && (currentKey != null));
  }

  public void setVisible(boolean visible) {
    globComboView.setVisible(visible);
  }

  public void dispose() {
    repository.removeChangeListener(this);
    selectionService.removeListener(this);
    globComboView.dispose();
  }
}