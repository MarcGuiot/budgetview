package org.designup.picsou.gui.accounts;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class AccountEditionPanel extends AbstractAccountPanel<GlobRepository> {

  private GlobsPanelBuilder builder;
  private Window owner;

  public AccountEditionPanel(Window owner, final GlobRepository repository, Directory directory, JLabel messageLabel) {
    super(repository, directory, messageLabel);
    this.owner = owner;
    createPanel();
  }

  private void createPanel() {
    builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountEditionPanel.splits",
                                    localRepository, localDirectory);
    super.createComponents(builder, owner);
  }

  public GlobsPanelBuilder getBuilder() {
    return builder;
  }

  public JPanel getPanel() {
    if (panel == null) {
      builder.load();
    }
    return panel;
  }
}
