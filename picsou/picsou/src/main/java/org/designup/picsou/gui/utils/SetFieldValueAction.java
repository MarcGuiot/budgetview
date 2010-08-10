package org.designup.picsou.gui.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.model.Key;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SetFieldValueAction extends AbstractAction {
  private Key key;
  private Field field;
  private Object value;
  private GlobRepository repository;

  public SetFieldValueAction(String name, Key key, Field field, Object value, GlobRepository repository) {
    super(name);
    this.key = key;
    this.field = field;
    this.value = value;
    this.repository = repository;
  }

  public SetFieldValueAction(Key key, Field field, Object value, GlobRepository repository) {
    this(null, key, field, value, repository);
  }

  public void actionPerformed(ActionEvent e) {
    repository.update(key, field, value);
  }
}
