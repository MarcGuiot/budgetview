package org.designup.picsou.gui.card.widgets;

import org.designup.picsou.gui.card.NavigationWidget;
import org.designup.picsou.gui.card.utils.GotoCardAction;
import org.designup.picsou.gui.card.utils.NavigationIcons;
import org.designup.picsou.gui.model.Card;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public abstract class AbstractNavigationWidget implements NavigationWidget {
  private String name;
  private String title;
  private Icon icon;
  private Icon rolloverIcon;
  protected Action action;
  private List<NavigationWidgetListener> listeners = new ArrayList<NavigationWidgetListener>();
  protected final GlobRepository repository;
  protected final Directory directory;
  private boolean highlighted;

  public AbstractNavigationWidget(Card card,
                                  GlobRepository repository,
                                  Directory directory) {
    this(card.getName(),
         card.getLabel(),
         NavigationIcons.getLarge(directory.get(ImageLocator.class), card),
         NavigationIcons.getLargeWithRollover(directory.get(ImageLocator.class), card),
         new GotoCardAction(card, directory),
         repository, directory);
  }

  public AbstractNavigationWidget(String name,
                                  String title,
                                  String icon,
                                  String rolloverIcon,
                                  Action action,
                                  GlobRepository repository,
                                  Directory directory) {
    this(name,
         title,
         directory.get(ImageLocator.class).get(icon),
         directory.get(ImageLocator.class).get(rolloverIcon),
         action, repository, directory);
  }

  private AbstractNavigationWidget(String name,
                                   String title,
                                   Icon icon,
                                   Icon rolloverIcon,
                                   Action action,
                                   GlobRepository repository,
                                   Directory directory) {
    this.name = name;
    this.title = title;
    this.icon = icon;
    this.rolloverIcon = rolloverIcon;
    this.action = action;
    this.repository = repository;
    this.directory = directory;
  }

  public void addListener(NavigationWidgetListener listener) {
    listeners.add(listener);
  }

  public void removeListener(NavigationWidgetListener listener) {
    listeners.remove(listener);
  }

  protected void setHighlighted(boolean highlighted) {
    this.highlighted = highlighted;
    for (NavigationWidgetListener listener : listeners) {
      listener.highlightingChanged(highlighted);
    }
  }

  public boolean isHighlighted() {
    return highlighted;
  }

  public String getName() {
    return name;
  }

  public String getTitle() {
    return title;
  }

  public Icon getIcon() {
    return icon;
  }

  public Icon getRolloverIcon() {
    return rolloverIcon;
  }

  public Action getAction() {
    return action;
  }


}
