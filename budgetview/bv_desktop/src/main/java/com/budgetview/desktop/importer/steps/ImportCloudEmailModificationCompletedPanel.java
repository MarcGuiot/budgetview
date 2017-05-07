package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportCloudEmailModificationCompletedPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private JLabel emailLabel = new JLabel();

  public ImportCloudEmailModificationCompletedPanel(PicsouDialog dialog, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudEmailModificationCompletedPanel.splits", repository, localDirectory);

    builder.add("emailLabel", emailLabel);

    builder.add("close", new AbstractAction(getCloseLabel()) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    return builder;
  }

  public void setNewEmail(String newEmail) {
    emailLabel.setText(newEmail);
  }

  public void prepareForDisplay() {
  }
}
