package com.budgetview.desktop.components.images;

import javax.swing.*;
import java.awt.*;

public interface IconFactory {

  Icon createIcon(Dimension size);

  public static final IconFactory NULL_ICON_FACTORY = new IconFactory() {
    public Icon createIcon(Dimension size) {
      return null;
    }
  };
}
