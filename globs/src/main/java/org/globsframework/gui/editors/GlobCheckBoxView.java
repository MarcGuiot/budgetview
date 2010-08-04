package org.globsframework.gui.editors;

import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobSelection;
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
  private GlobList currentsGlob;

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
    for (Glob glob : currentsGlob) {
      repository.update(glob.getKey(), field, checkBox.isSelected());
    }
    repository.completeChangeSet();
  }

  public JCheckBox getComponent() {
    return checkBox;
  }

  public void dispose() {
    repository.removeTrigger(this);
    selectionService.removeListener(this);
  }

  public void selectionUpdated(GlobSelection selection) {
    currentsGlob = selection.getAll(type);
    updateBox();
  }

  private void updateBox() {
    Set set = currentsGlob.getValueSet(field);
    if (set.size() == 1){
      checkBox.setSelected(Boolean.TRUE.equals(set.iterator().next()));
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    updateBox();
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(type)){
      currentsGlob = GlobList.EMPTY;
    }
  }
}