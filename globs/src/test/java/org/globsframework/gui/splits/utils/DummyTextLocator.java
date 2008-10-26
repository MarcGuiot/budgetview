package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.exceptions.TextNotFound;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DummyTextLocator implements TextLocator {

  private static Pattern pattern = Pattern.compile("[A-z\\.]+");
  private Map<String, String> values = new HashMap<String, String>();

  public void set(String key, String value) {
    values.put(key, value);
  }

  public String get(String code) {
    if (values.containsKey(code)) {
      return values.get(code);
    }
    if (!pattern.matcher(code).matches()) {
      throw new TextNotFound("Invalid format for '" + code + "'");
    }
    return code.replace('.', ' ');
  }
}
