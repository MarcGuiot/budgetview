package com.budgetview.gui.utils.datacheck;

import com.budgetview.gui.time.TimeService;
import org.globsframework.utils.Log;

import java.util.Date;

public class DataCheckReport {

  private StringBuilder builder = new StringBuilder();
  private boolean hasError;
  private Date date = TimeService.getCurrentDate();

  public StringBuilder append(Object message) {
    hasError = true;
    builder.append(message);
    return builder;
  }

  public void addError(String message) {
    hasError = true;
    builder.append(message)
      .append('\n');
  }

  public void addError(String message, Object info) {
    hasError = true;
    builder.append(message)
      .append(' ')
      .append(info)
      .append('\n');
  }

  public boolean hasError() {
    Log.write(toString());
    return hasError;
  }

  public String toString() {
    if (!hasError) {
      return "";
    }
    return "On " + date + ":\n" + builder.toString();
  }

  public void clear() {
    hasError = false;
    builder = new StringBuilder();
  }
}
