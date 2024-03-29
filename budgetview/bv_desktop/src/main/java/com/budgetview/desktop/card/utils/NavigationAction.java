package com.budgetview.desktop.card.utils;

import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.model.Card;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class NavigationAction extends AbstractAction implements GlobSelectionListener {
  private NavigationService navigationService;

  public NavigationAction(Directory directory) {
    navigationService = directory.get(NavigationService.class);
    directory.get(SelectionService.class).addListener(this, Card.TYPE);
    updateState();
  }

  public void actionPerformed(ActionEvent e) {
    apply(navigationService);
  }

  public void selectionUpdated(GlobSelection selection) {
    updateState();
  }

  private void updateState() {
    setEnabled(getEnabledState(navigationService));
  }

  protected abstract boolean getEnabledState(NavigationService navigationService);

  protected abstract void apply(NavigationService navigationService);
}
