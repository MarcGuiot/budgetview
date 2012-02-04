package org.designup.picsou.gui.transactions.creation;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CreateTransactionMenuAction extends AbstractAction {

  private NavigationService navigationService;

  public CreateTransactionMenuAction(Directory directory) {
    super(Lang.get("transactionCreation.menu"));
    this.navigationService = directory.get(NavigationService.class);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    navigationService.highlightTransactionCreation();
  }
}
