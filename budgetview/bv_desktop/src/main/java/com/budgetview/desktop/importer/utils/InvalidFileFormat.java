package com.budgetview.desktop.importer.utils;

import com.budgetview.utils.Lang;

public class InvalidFileFormat extends RuntimeException {

  private String details;

  public InvalidFileFormat(String details) {
    this(Lang.get("import.file.error"), details);
  }

  protected InvalidFileFormat(String message, String details) {
    super(message);
    this.details = details;
  }

  public String getMessage(String absolutePath) {
    return Lang.get("import.file.error", absolutePath);
  }

  public String getDetails() {
    return details;
  }
}
