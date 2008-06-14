package org.globsframework.gui.splits;

import org.globsframework.gui.splits.exceptions.TextNotFound;


public interface TextLocator {
  String get(String code);

  TextLocator NULL = new TextLocator() {
    public String get(String code) {
      throw new TextNotFound("No TextLocator available - cannot find text for: " + code);
    }
  };

}
