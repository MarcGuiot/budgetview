package org.designup.picsou.gui.card;

import org.designup.picsou.gui.model.Card;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.gui.splits.ImageLocator;

import javax.swing.*;
import java.awt.*;

public class NavigationIcons {
  public static final Dimension DIMENSION = new Dimension(45, 45);

  public static ImageIcon get(ImageLocator locator, Card card) {
    return locator.get("card_" + getName(card) + ".png");
  }

  public static ImageIcon getRollover(ImageLocator locator, Card card) {
    return locator.get("card_" + getName(card) + "_rollover.png");
  }

  public static ImageIcon getLarge(ImageLocator locator, Card card) {
    return locator.get("card_" + getName(card) + "_large.png");
  }

  public static ImageIcon getLargeWithRollover(ImageLocator locator, Card card) {
    return locator.get("card_" + getName(card) + "_large_rollover.png");
  }

  public static String getName(Card card) {
    switch (card) {
      case HOME:
        return "home";
      case BUDGET:
        return "budget";
      case EVOLUTION:
        return "line";
      case DATA:
        return "list";
      case CATEGORIZATION:
        return "categorization";
    }
    throw new InvalidParameter("No icon for " + card);
  }

}
