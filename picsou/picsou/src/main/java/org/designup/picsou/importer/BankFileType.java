package org.designup.picsou.importer;

public enum BankFileType {
  OFX,
  QIF;

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
    return null;
  }

  public static boolean isFileNameSupported(String name) {
    return getTypeFromName(name) != null;
  }
}
