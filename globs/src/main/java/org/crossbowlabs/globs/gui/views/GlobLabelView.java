package org.crossbowlabs.globs.gui.views;

import org.crossbowlabs.globs.gui.ComponentHolder;
import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.ChangeSet;
import org.crossbowlabs.globs.model.ChangeSetListener;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;

import javax.swing.*;
import java.util.List;

public class GlobLabelView implements GlobSelectionListener, ChangeSetListener, ComponentHolder {
  private JLabel label;
  private GlobType type;
  private GlobRepository repository;
  private Stringifier stringifier;
  private GlobList currentSelection = new GlobList();

  public interface Stringifier {
    String toString(GlobList selected);
  }

  public static GlobLabelView init(GlobType type, GlobRepository globRepository,
                                   Directory directory, Stringifier stringifier) {
    return new GlobLabelView(type, globRepository, directory, stringifier);
  }

  public GlobLabelView(GlobType type, GlobRepository repository, Directory directory, Stringifier stringifier) {
    this.type = type;
    this.repository = repository;
    this.stringifier = stringifier;
    this.label = new JLabel();
    this.label.setName(type.getName());
    directory.get(SelectionService.class).addListener(this, type);
    repository.addChangeListener(this);
    update();
  }

  public JLabel getComponent() {
    return label;
  }

  public GlobLabelView update() {
    label.setText(stringifier.toString(currentSelection));
    return this;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository globRepository) {
    if (changeSet.containsChanges(type)) {
      update();
    }
  }

  public void globsReset(GlobRepository globRepository, List<GlobType> changedTypes) {
    currentSelection = new GlobList();
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    currentSelection = selection.getAll(type);
    update();
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }
}
