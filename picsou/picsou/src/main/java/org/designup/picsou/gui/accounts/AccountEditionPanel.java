package org.designup.picsou.gui.accounts;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class AccountEditionPanel extends AbstractAccountPanel<GlobRepository> {

  private GlobsPanelBuilder builder;

  public AccountEditionPanel(final GlobRepository repository, Directory directory, JLabel messageLabel) {
    super(repository, directory, messageLabel);
    createPanel();
  }

  private void createPanel() {
    builder = new GlobsPanelBuilder(getClass(), "/layout/accountEditionPanel.splits",
                                    localRepository, localDirectory);

    createComponents(builder);
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
