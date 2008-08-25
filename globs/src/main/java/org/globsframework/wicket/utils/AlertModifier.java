package org.globsframework.wicket.utils;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.Model;

public class AlertModifier extends AttributeModifier {

  public AlertModifier(String message) {
    super("onclick", true, new Model(message));
  }

  protected String newValue(final String currentValue, final String message) {
    return "alert('" + message + "');";
  }
}
