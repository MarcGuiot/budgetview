package org.functests4j;

import org.functests4j.kernel.FuncTestEvent;
import org.functests4j.kernel.AbstractFuncTestEvent;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class DefaultFuncTestEvent extends AbstractFuncTestEvent {
  static Object UNVALUED = new Object();

  private Map attributs = new HashMap();
  private Map result = new HashMap();
  private String name;

  public DefaultFuncTestEvent(String name) {
    this.name = name;
  }

  public FuncTestEvent setAttributes(String attributeNane, Object attributeValue) {
    attributs.put(attributeNane, attributeValue);
    return this;
  }

  public Object getResult(String resultName) {
    if (result.containsKey(resultName)) {
      return result.get(resultName);
    }
    return getResultValue(resultName);
  }


  public Object getAttributeValue(String attributeName) {
    if (attributs.containsKey(attributeName)) {
      return attributs.get(attributeName);
    }
    return getAttributeNotFound(attributeName);
  }

  public FuncTestEvent visitAttribute(AttributeValue attributeValue) {
    for (Iterator iterator = attributs.entrySet().iterator(); iterator.hasNext();) {
      Map.Entry entry = (Map.Entry) iterator.next();
      attributeValue.process((String) entry.getKey(), entry.getValue());
    }
    return this;
  }

  public FuncTestEvent visitResult(AttributeValue attributeValue) {
    for (Iterator iterator = result.entrySet().iterator(); iterator.hasNext();) {
      Map.Entry entry = (Map.Entry) iterator.next();
      attributeValue.process((String) entry.getKey(), entry.getValue());
    }
    return this;
  }

  public String getName() {
    return name;
  }

  public DefaultFuncTestEvent setReturnValue(String attName, Object value) {
    result.put(attName, value);
    return this;
  }

  public boolean isEquivalent(FuncTestEvent eventToFind) {
    return eventToFind.getName().equals(getName());
  }

  public String getDescription() {
    return getName();
  }

  interface AttributeValue {
    void process(String attributeName, Object value);
  }

  public Object getAttributeNotFound(String attributeName) {
    return UNVALUED;
  }

  public Object getResultValue(String resultName) {
    return UNVALUED;
  }

  public String toString() {
    return name;
  }
}
