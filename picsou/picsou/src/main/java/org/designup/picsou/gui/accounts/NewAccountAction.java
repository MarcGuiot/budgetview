package org.designup.picsou.gui.accounts;

import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NewAccountAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;
  private final Window owner;

  public NewAccountAction(GlobRepository repository, Directory directory, Window owner) {
    super(Lang.get("new.account"));
    this.repository = repository;
    this.directory = directory;
    this.owner = owner;
  }

  public void actionPerformed(ActionEvent e) {
    AccountEditionDialog dialog = new AccountEditionDialog(owner, repository, directory);
    dialog.show();
  }
}
