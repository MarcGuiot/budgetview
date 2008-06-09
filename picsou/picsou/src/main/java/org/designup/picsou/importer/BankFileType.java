package org.designup.picsou.importer;

public enum BankFileType {
  OFX(".OFX"),
  QIF(".QIF");

  private String extension;

  BankFileType(String extension) {
    this.extension = extension;
  }

  public static BankFileType getTypeFromName(String fileName) {
    for (BankFileType type : values()) {
      if (fileName.toUpperCase().endsWith(type.extension)) {
        return type;
      }
    }
    return null;
  }
}
