package org.crossbowlabs.globs.wicket.utils;

import wicket.AttributeModifier;
import wicket.model.Model;

public class OnClickConfirmationModifier extends AttributeModifier {
  public OnClickConfirmationModifier(String message) {
    super("onclick", true, new Model(message));
  }

  protected String newValue(final String currentValue, final String message) {
    return "if (!confirm('" + message + "')) return false; " + currentValue;
  }
}
