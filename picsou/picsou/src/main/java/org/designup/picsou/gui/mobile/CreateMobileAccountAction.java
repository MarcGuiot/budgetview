package org.designup.picsou.gui.mobile;

import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CreateMobileAccountAction extends AbstractAction {
  private final Directory directory;
  private final GlobRepository repository;

  public CreateMobileAccountAction(Directory directory, GlobRepository repository) {
    super(Lang.get("mobile.user.create.action.name"));
    this.directory = directory;
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    CreateMobileAccountDialog dialog = new CreateMobileAccountDialog(directory, repository);
    dialog.show();
  }
}
