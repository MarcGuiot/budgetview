package org.designup.picsou.gui.notifications;

import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.util.Date;

public class GlobNotification implements Notification {
  private GlobRepository repository;
  private int id;
  private Glob glob;
  private DateField dateField;
  private BooleanField clearedField;
  private String message;

  public GlobNotification(GlobRepository repository, int id, Glob glob,
                          DateField dateField, BooleanField clearedField,
                          String message) {
    this.repository = repository;
    this.id = id;
    this.glob = glob;
    this.dateField = dateField;
    this.clearedField = clearedField;
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

  public void clear() {
    repository.update(glob.getKey(), this.clearedField, true);
  }

  public int hashCode() {
    return glob.hashCode();
  }

  public boolean equals(Object o) {
    return o.equals(glob);
  }
}
