package org.designup.picsou.gui.projects.actions;

import org.designup.picsou.gui.accounts.utils.AccountCreation;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CreateProjectAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;


  public CreateProjectAction(GlobRepository repository, Directory directory) {
    super(Lang.get("projectView.create"));
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    if (!AccountCreation.containsUserAccount(repository, directory,
                                             Lang.get("accountCreation.projectCreation.message"))) {
      return;
    }

    SeriesEditor.get(directory).showNewProject();
  }
}
