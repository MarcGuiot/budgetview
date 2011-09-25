package org.designup.picsou.gui.accounts;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class AccountEditionPanel extends AbstractAccountPanel {

  public AccountEditionPanel(Window owner, GlobRepository repository, Directory parentDirectory) {
    super(repository, parentDirectory);
    createPanel(owner);
  }

  private void createPanel(Window owner) {
    GlobsPanelBuilder accountBuilder =
      new GlobsPanelBuilder(getClass(), "/layout/accounts/accountEditionPanel.splits", localRepository,
                            localDirectory);
    createComponents(accountBuilder, owner);
    accountBuilder.load();
  }

  public JPanel getPanel() {
    return panel;
  }
}
