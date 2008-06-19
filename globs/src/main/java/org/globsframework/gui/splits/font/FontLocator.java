package org.globsframework.gui.splits.font;

import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;

public interface FontLocator {
  Font get(String name) throws ItemNotFound, InvalidParameter;
}
