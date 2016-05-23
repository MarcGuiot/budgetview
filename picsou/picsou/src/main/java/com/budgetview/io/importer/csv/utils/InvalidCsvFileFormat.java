package com.budgetview.io.importer.csv.utils;

import com.budgetview.gui.importer.utils.InvalidFileFormat;
import com.budgetview.utils.Lang;

public class InvalidCsvFileFormat extends InvalidFileFormat {
  public InvalidCsvFileFormat(String details) {
    super(Lang.get("import.csv.invalidFileFormat"), details);
  }

  public String getMessage(String absolutePath) {
    return Lang.get("import.csv.invalidFileFormat");
  }
}
