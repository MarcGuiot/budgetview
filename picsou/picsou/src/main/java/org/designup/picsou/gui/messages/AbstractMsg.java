package org.designup.picsou.gui.messages;

import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.util.Date;

public class AbstractMsg implements MessageDisplay {
  private GlobRepository repository;
  private int id;
  private Glob glob;
  private DateField dateField;
  private BooleanField clear;
  private String message;

  public AbstractMsg(GlobRepository repository, int id, Glob glob, DateField dateField, BooleanField clear, String message) {
    this.repository = repository;
    this.id = id;
    this.glob = glob;
    this.dateField = dateField;
    this.clear = clear;
    this.message = message;
  }

  public int getId() {
    return id;
  }

  public Date getDate() {
    return glob.get(dateField);
  }

  public String getMessage() {
    return message;
  }

  public boolean isCleared() {
    return glob.get(clear);
  }

  public void clear(boolean clear) {
    repository.update(glob.getKey(), this.clear, clear);
  }
}
