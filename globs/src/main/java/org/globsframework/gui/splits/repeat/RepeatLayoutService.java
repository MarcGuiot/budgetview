package org.globsframework.gui.splits.repeat;

import java.util.HashMap;
import java.util.Map;

public class RepeatLayoutService {
  Map<String, String> layoutClassName = new HashMap<String, String>();

  public void add(String name, String repeatLayoutClass){
    layoutClassName.put(name, repeatLayoutClass);
  }

  public String getClassName(String property) {
    return layoutClassName.containsKey(property) ? layoutClassName.get(property) : property;
  }
}
