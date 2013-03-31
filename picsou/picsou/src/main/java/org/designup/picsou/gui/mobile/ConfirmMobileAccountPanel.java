package org.designup.picsou.gui.mobile;

import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ConfirmMobileAccountPanel {

  private final GlobRepository repository;
  private final Directory directory;
  private JPanel panel;

  public ConfirmMobileAccountPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/mobile/confirmMobileAccountPanel.splits",
                                                      repository, directory);

    builder.addLabel("emailLabelReminder", UserPreferences.MAIL_FOR_MOBILE).getComponent();
    builder.addLabel("passwordLabelReminder", UserPreferences.PASSWORD_FOR_MOBILE).getComponent();

    panel = builder.load();
  }
}
