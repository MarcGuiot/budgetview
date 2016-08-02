package com.budgetview.desktop.transactions.actions;

import com.budgetview.desktop.card.NavigationService;
import com.budgetview.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowAllTransactionsInAccountViewAction extends AbstractAction {

  private Directory directory;

  public ShowAllTransactionsInAccountViewAction(Directory directory) {
    super(Lang.get("showTransactionsInAccountViewAction.text"));
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    directory.get(NavigationService.class).gotoDataForAll();
  }
}
