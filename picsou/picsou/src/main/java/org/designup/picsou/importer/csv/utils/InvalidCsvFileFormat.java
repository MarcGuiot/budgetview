package org.designup.picsou.importer.csv.utils;

import org.designup.picsou.gui.importer.utils.InvalidFileFormat;
import org.designup.picsou.utils.Lang;

public class InvalidCsvFileFormat extends InvalidFileFormat {
  public InvalidCsvFileFormat(String details) {
    super(Lang.get("import.csv.invalidFileFormat"), details);
  }

  public String getMessage(String absolutePath) {
    return Lang.get("import.csv.invalidFileFormat");
  }
}
