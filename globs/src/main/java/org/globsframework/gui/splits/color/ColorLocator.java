package org.globsframework.gui.splits.color;

import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;

public interface ColorLocator {
  Color get(Object key) throws ItemNotFound, InvalidParameter;
}
