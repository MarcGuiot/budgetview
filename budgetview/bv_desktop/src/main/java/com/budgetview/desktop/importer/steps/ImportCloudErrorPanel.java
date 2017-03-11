package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.cloud.InvalidCloudAPIVersion;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.ConfirmationDialog;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.help.HyperlinkHandler;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.net.UnknownHostException;

public class ImportCloudErrorPanel extends AbstractImportStepPanel {

  private JEditorPane errorMessage = GuiUtils.createReadOnlyHtmlComponent();
  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private JLabel detailsTitle = new JLabel();
  private JEditorPane detailsText = GuiUtils.createReadOnlyHtmlComponent();

  public ImportCloudErrorPanel(PicsouDialog dialog, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudErrorPanel.splits", repository, localDirectory);

    builder.add("errorMessage", errorMessage);

    builder.add("close", new AbstractAction(getCloseLabel()) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    builder.add("detailsTitle", detailsTitle);
    builder.add("detailsText", detailsText);

    builder.add("hyperlinkHandler", new HyperlinkHandler(localDirectory));

    progressPanel = new ProgressPanel();
    builder.add("progressPanel", progressPanel);

    return builder;
  }

  public void showException(Exception e) {

    if (e == null) {
      errorMessage.setText(Lang.get("import.cloud.error.message.default"));
      detailsTitle.setVisible(false);
      detailsText.setVisible(false);
      return;
    }

    if (e instanceof UnknownHostException || e instanceof org.apache.http.conn.HttpHostConnectException) {
      errorMessage.setText(Lang.get("import.cloud.error.message.unknownHost"));
      detailsTitle.setVisible(false);
      detailsText.setVisible(false);
      return;
    }

    if (e instanceof InvalidCloudAPIVersion) {
      errorMessage.setText(Lang.get("import.cloud.error.message.apiVersion"));
      detailsTitle.setVisible(false);
      detailsText.setVisible(false);
      return;
    }

    errorMessage.setText(Lang.get("import.cloud.error.message.default"));
    detailsTitle.setVisible(true);
    detailsText.setVisible(true);
    detailsText.setText(Utils.toString(e));
    GuiUtils.scrollToTop(detailsText);
  }

  public void prepareForDisplay() {
  }
}
