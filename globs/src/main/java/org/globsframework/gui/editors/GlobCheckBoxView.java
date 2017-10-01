package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class GlobCheckBoxView extends AbstractGlobComponentHolder<GlobCheckBoxView> implements GlobSelectionListener, ChangeSetListener {
  private JCheckBox checkBox;
  private BooleanField field;
  private GlobList currentGlobs = new GlobList();
  private Key forcedKey;

  public static GlobCheckBoxView init(BooleanField field, GlobRepository globRepository, Directory directory) {
    return new GlobCheckBoxView(field, globRepository, directory);
  }

  private GlobCheckBoxView(BooleanField field, GlobRepository globRepository, Directory directory) {
    super(field.getGlobType(), globRepository, directory);
    repository.addChangeListener(this);
    selectionService.addListener(this, type);
    this.field = field;
    checkBox = new JCheckBox();
    checkBox.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        apply();
      }
    });
  }

  private void apply() {
    repository.startChangeSet();
    for (Glob glob : currentGlobs) {
      repository.update(glob, field, checkBox.isSelected());
    }
    repository.completeChangeSet();
  }

  public JCheckBox getComponent() {
    return checkBox;
  }

  public GlobCheckBoxView forceSelection(Key key) {
    this.forcedKey = key;
    selectionService.removeListener(this);
    Glob glob = repository.find(forcedKey);
    if (glob != null) {
      currentGlobs = new GlobList(glob);
    }
    else {
      currentGlobs = new GlobList();
    }
    updateBox();
    return this;
  }

  public void selectionUpdated(GlobSelection selection) {
    currentGlobs = selection.getAll(type);
    updateBox();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(type)) {
      currentGlobs.keepExistingGlobsOnly(repository);
      if ((forcedKey != null)) {
        Glob glob = repository.find(forcedKey);
        if (glob != null && !currentGlobs.contains(glob)) {
          currentGlobs.add(glob);
        }
      }
      updateBox();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(type)) {
      currentGlobs = new GlobList();
      updateBox();
    }
  }

  private void updateBox() {
    Set values = currentGlobs.getValueSet(field);
    checkBox.setSelected(!values.isEmpty() && Boolean.TRUE.equals(values.iterator().next()));
    checkBox.setEnabled(values.size() > 0);
  }

  public void dispose() {
    repository.removeTrigger(this);
    selectionService.removeListener(this);
  }
}