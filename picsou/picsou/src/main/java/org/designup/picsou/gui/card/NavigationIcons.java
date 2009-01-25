package org.designup.picsou.gui.card;

import org.designup.picsou.gui.model.Card;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.gui.splits.ImageLocator;

import javax.swing.*;
import java.awt.*;

public class NavigationIcons {
  public static final Dimension DIMENSION = new Dimension(45, 45);

  public static ImageIcon get(ImageLocator locator, Card card) {
    return locator.get("cards/" + getName(card) + ".png");
  }

  public static ImageIcon getRollover(ImageLocator locator, Card card) {
    return locator.get("cards/" + getName(card) + "_rollover.png");
  }

  public static ImageIcon getLarge(ImageLocator locator, Card card) {
    return locator.get("home/" + getName(card) + ".png");
  }

  public static ImageIcon getLargeWithRollover(ImageLocator locator, Card card) {
    return locator.get("home/" + getName(card) + "_rollover.png");
  }

  public static String getName(Card card) {
    return card.getName().toLowerCase();
  }

}
