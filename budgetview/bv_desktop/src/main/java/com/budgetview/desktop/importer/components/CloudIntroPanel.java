package com.budgetview.desktop.importer.components;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CloudIntroPanel {
  private final PicsouDialog dialog;
  private ImportController controller;
  private final LocalGlobRepository repository;
  private final Directory directory;
  private JPanel panel;

  public CloudIntroPanel(PicsouDialog dialog, ImportController controller, LocalGlobRepository localRepository, Directory localDirectory) {
    this.dialog = dialog;
    this.controller = controller;
    this.repository = localRepository;
    this.directory = localDirectory;
  }

  public JPanel getPanel() {
    if (panel == null) {
      panel = createPanel();
    }
    return panel;
  }

  private JPanel createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/components/cloudIntroPanel.splits", repository, directory);

    builder.add("openCloudSynchro", new AbstractAction("Goto synchro") {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudBankSelection();
      }
    });

    return builder.load();
  }

  public void dispose() {
  }

  public void requestFocus() {

  }
}
