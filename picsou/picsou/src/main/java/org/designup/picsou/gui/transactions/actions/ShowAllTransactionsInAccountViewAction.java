package org.designup.picsou.gui.transactions.actions;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

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
