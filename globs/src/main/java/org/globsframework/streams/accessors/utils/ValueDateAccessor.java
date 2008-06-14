package org.globsframework.streams.accessors.utils;

import org.globsframework.streams.accessors.DateAccessor;

import java.util.Date;

public class ValueDateAccessor implements DateAccessor {
  private Date value;

  public ValueDateAccessor(Date value) {
    this.value = value;
  }

  public void setValue(Date value) {
    this.value = value;
  }

  public Date getDate() {
    return value;
  }

  public Object getObjectValue() {
    return value;
  }
}
