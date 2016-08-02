package com.budgetview.desktop.transactions.creation;

import com.budgetview.desktop.card.NavigationService;
import com.budgetview.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowCreateTransactionAction extends AbstractAction {

  private NavigationService navigationService;

  public ShowCreateTransactionAction(Directory directory) {
    super(Lang.get("transactionCreation.menu"));
    this.navigationService = directory.get(NavigationService.class);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    navigationService.highlightTransactionCreation();
  }
}
