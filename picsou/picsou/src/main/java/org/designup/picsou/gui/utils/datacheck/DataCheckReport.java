package org.designup.picsou.gui.utils.datacheck;

public class DataCheckReport {

  private StringBuilder builder = new StringBuilder();
  private boolean hasError;

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
    return hasError;
  }

  public String toString() {
    return builder.toString();
  }

  public void clear() {
    hasError = false;
    builder = new StringBuilder();
  }
}
