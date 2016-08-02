package com.budgetview.desktop.card.utils;

import com.budgetview.desktop.model.Card;
import org.globsframework.gui.splits.ImageLocator;

import javax.swing.*;
import java.awt.*;

public class NavigationIcons {
  public static final Dimension DIMENSION = new Dimension(45, 45);

  public static ImageIcon get(ImageLocator locator, Card card) {
    return locator.get("cards/" + getName(card) + ".png");
  }

  public static String getName(Card card) {
    return card.getName().toLowerCase();
  }

}
