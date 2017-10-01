package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.HashSet;
import java.util.Set;

public class GlobToggleEditor extends AbstractGlobFieldEditor<GlobToggleEditor> {

  private final JToggleButton toggle = new JToggleButton();
  private final BooleanField field;

  public static GlobToggleEditor init(BooleanField field, GlobRepository repository, Directory directory) {
    return new GlobToggleEditor(field, repository, directory);
  }

  protected GlobToggleEditor(BooleanField field, GlobRepository repository, Directory directory) {
    super(field.getGlobType(), repository, directory);
    this.field = field;
    this.toggle.getModel().addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        updateFromToggle();
      }
    });
    toggle.setEnabled(false);
    toggle.setSelected(false);
  }

  protected void updateFromGlobs() {
    if (isAdjusting) {
      return;
    }
    isAdjusting = true;
    try {
      if (currentGlobs.isEmpty()) {
        toggle.setSelected(false);
        toggle.setEnabled(false);
        return;
      }

      toggle.setEnabled(true);
      Set<Boolean> values = new HashSet<Boolean>();
      for (Glob glob : currentGlobs) {
        values.add(glob.isTrue(field));
      }
      if (values.size() != 1) {
        toggle.setSelected(false);
      }
      else {
        toggle.setSelected(values.iterator().next());
      }
    }
    finally {
      isAdjusting = false;
    }
  }

  private void updateFromToggle() {
    if (isAdjusting) {
      return;
    }

    try {
      isAdjusting = true;
      boolean isSelected = toggle.isSelected();
      repository.startChangeSet();
      for (Glob glob : currentGlobs) {
        repository.update(glob, field, isSelected);
      }
    }
    finally {
      repository.completeChangeSet();
      isAdjusting = false;
    }
  }

  public JToggleButton getComponent() {
    return toggle;
  }
}
