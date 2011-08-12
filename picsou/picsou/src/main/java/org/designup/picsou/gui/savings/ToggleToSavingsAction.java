package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ToggleToSavingsAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public ToggleToSavingsAction(GlobRepository repository, Directory directory) {
    super(Lang.get("savingsView.toggleToSavings"));
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    directory.get(NavigationService.class).gotoBudgetForSavingsAccounts();
  }
}
