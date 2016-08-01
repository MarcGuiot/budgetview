package com.budgetview.gui.notifications;

import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.util.Date;

public abstract class GlobNotification implements Notification {
  private GlobRepository repository;
  private Glob glob;
  private DateField dateField;
  private BooleanField clearedField;
  private String message;

  public GlobNotification(Glob glob, String message, DateField dateField, BooleanField clearedField, GlobRepository repository) {
    this.repository = repository;
    this.glob = glob;
    this.dateField = dateField;
    this.clearedField = clearedField;
    this.message = message;
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
