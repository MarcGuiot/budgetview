package org.globsframework.gui.splits.layout;

import org.globsframework.utils.exceptions.InvalidParameter;

public interface TabHandler {
  void select(int tabIndex) throws InvalidParameter;
}
