package org.globsframework.gui.actions;

import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.KeyChangeListener;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleBooleanAction extends AbstractAction implements Disposable {

  private final Key key;
  private final BooleanField field;
  private final String textForTrue;
  private final String textForFalse;
  private final GlobRepository repository;
  private KeyChangeListener changeListener;

  public ToggleBooleanAction(Key key, BooleanField field,
                             String textForTrue, String textForFalse,
                             GlobRepository repository) {
    this.key = key;
    this.field = field;
    this.textForTrue = textForTrue;
    this.textForFalse = textForFalse;
    this.repository = repository;
    this.changeListener = new KeyChangeListener(key) {
      protected void update() {
        doUpdate();
      }
    };
    repository.addChangeListener(changeListener);
    doUpdate();
  }

  public void dispose() {
    repository.removeChangeListener(changeListener);
    changeListener = null;
  }

  private void doUpdate() {
    Glob glob = repository.find(key);
    setEnabled(glob != null);
    if (glob != null) {
      putValue(Action.NAME, glob.isTrue(field) ? textForTrue : textForFalse);
    }
    else {
      putValue(Action.NAME, textForFalse);
    }
  }

  public void actionPerformed(ActionEvent e) {
    Glob glob = repository.find(key);
    if (glob != null) {
      repository.update(key, field, !glob.isTrue(field));
    }
  }
}
