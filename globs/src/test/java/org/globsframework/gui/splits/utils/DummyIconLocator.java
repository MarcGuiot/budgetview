package org.globsframework.gui.splits.utils;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.exceptions.IconNotFound;

import javax.swing.*;

public class DummyIconLocator implements IconLocator {

  public static final String ICON1_NAME = "icon1";
  public static final String ICON2_NAME = "icon2";
  public static final String ICON3_NAME = "icon3";

  public static final ImageIcon ICON1 = new ImageIcon();
  public static final ImageIcon ICON2 = new ImageIcon();
  public static final ImageIcon ICON3 = new ImageIcon();

  public ImageIcon get(String name) throws IconNotFound {
    if (ICON1_NAME.equals(name)) {
      return ICON1;
    }
    if (ICON2_NAME.equals(name)) {
      return ICON2;
    }
    if (ICON3_NAME.equals(name)) {
      return ICON3;
    }
    return new ImageIcon();
  }
}
