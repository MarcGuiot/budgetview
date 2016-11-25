package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.browsing.BrowsingAction;
import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ImportCloudSubscriptionErrorPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private JEditorPane messageField;
  private JButton actionButton;

  public ImportCloudSubscriptionErrorPanel(PicsouDialog dialog, String textForCloseButton, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, textForCloseButton, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudSubscriptionErrorPanel.splits", repository, localDirectory);

    Action nextAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        processNext();
      }
    };

    messageField = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("messageField", messageField);

    actionButton = new JButton();
    builder.add("actionButton", actionButton);

    builder.add("next", nextAction);
    builder.add("close", new AbstractAction(textForCloseButton) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    progressPanel = new ProgressPanel();
    builder.add("progressPanel", progressPanel);

    return builder;
  }

  public void update(String email, CloudSubscriptionStatus status) {
    createPanelIfNeeded();
    switch (status) {
      case UNKNOWN:
        messageField.setText(Lang.get("import.cloud.subscription.unknown", email));
        actionButton.setAction(new BuySubscriptionAction(localDirectory));
        break;
      case EXPIRED:
        messageField.setText(Lang.get("import.cloud.subscription.expired", email));
        actionButton.setAction(new BuySubscriptionAction(localDirectory));
        break;
      default:
        messageField.setText(Lang.get("import.cloud.subscription.other", email));
        actionButton.setAction(new BuySubscriptionAction(localDirectory));
        break;
    }


  }

  private void processNext() {

  }

  public void prepareForDisplay() {

  }


  private class BuySubscriptionAction extends BrowsingAction {

    public BuySubscriptionAction(Directory directory) {
      super(Lang.get("import.cloud.subscription.subscribe"), directory);
    }

    protected String getUrl() {
      return Lang.get("site.buy.cloud.url");
    }

  }
}
