package org.globsframework.wicket.utils;

import wicket.AttributeModifier;
import wicket.model.Model;

public class AlertModifier extends AttributeModifier {

  public AlertModifier(String message) {
    super("onclick", true, new Model(message));
  }

  protected String newValue(final String currentValue, final String message) {
    return "alert('" + message + "');";
  }
}
