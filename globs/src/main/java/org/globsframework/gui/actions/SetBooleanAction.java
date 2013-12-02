package org.globsframework.gui.actions;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class SetBooleanAction extends AbstractAction implements ChangeSetListener, Disposable {

  private Key key;
  private final BooleanField field;
  private final boolean value;
  private final GlobRepository repository;

  public SetBooleanAction(Key key, BooleanField field,
                          boolean value, String text, GlobRepository repository) {
    super(text);
    this.key = key;
    this.field = field;
    this.value = value;
    this.repository = repository;
    repository.addChangeListener(this);
    doUpdate();
  }

  public void setKey(Key newKey) {
    if (!Utils.equal(this.key, newKey)) {
      this.key = newKey;
      doUpdate();
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (key != null && changeSet.containsChanges(key)) {
      doUpdate();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (key != null && changedTypes.contains(key.getGlobType())) {
      doUpdate();
    }
  }

  public void doUpdate() {
    Glob glob = repository.find(key);
    setEnabled(glob != null);
  }

  public void actionPerformed(ActionEvent e) {
    Glob glob = repository.find(key);
    if (glob != null) {
      repository.update(key, field, value);
    }
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }
}
