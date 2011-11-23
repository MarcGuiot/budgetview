package org.designup.picsou.gui.utils.dev;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.designup.picsou.model.User;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ThrowInRepoExceptionAction extends AbstractAction {
  private GlobRepository repository;

  public ThrowInRepoExceptionAction(GlobRepository repository) {
    super("[Throw exception in repository]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    LocalGlobRepository repository1 = LocalGlobRepositoryBuilder.init(repository).get();
    repository1.create(User.KEY);
    repository1.commitChanges(true);
  }
}