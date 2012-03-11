package org.designup.picsou.gui.importer.csv;

public enum CsvSeparator {
  COMMA(','),
  TAB('\t'),
  COLON(':'),
  SEMICOLON(';');

  private char separator;

  CsvSeparator(char c) {
    this.separator = c;
  }

  public char getSeparator() {
    return separator;
  }
}
