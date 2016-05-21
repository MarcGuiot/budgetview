package com.budgetview.importer;

public enum BankFileType {
  OFX,
  QIF,
  CSV;

  public static BankFileType getTypeFromName(String fileName) {
    String lowerCase = fileName.toLowerCase();
    if (lowerCase.endsWith(".ofx")) {
      return OFX;
    }
    if (lowerCase.endsWith(".ofc")) {
      return OFX;
    }
    if (lowerCase.endsWith(".qif")) {
      return QIF;
    }
    if (lowerCase.endsWith(".csv")) {
      return CSV;
    }
    if (lowerCase.endsWith(".tsv")) {
      return CSV;
    }
    return null;
  }

  public static boolean isFileNameSupported(String name) {
    return getTypeFromName(name) != null;
  }
}
