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
  private JEditorPane backToSignupMessage;
  private JButton backToSignupButton;
  private JButton actionButton;

  public ImportCloudSubscriptionErrorPanel(PicsouDialog dialog, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudSubscriptionErrorPanel.splits", repository, localDirectory);

    messageField = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("messageField", messageField);

    actionButton = new JButton();
    builder.add("actionButton", actionButton);

    backToSignupMessage = GuiUtils.createReadOnlyHtmlComponent(Lang.get("import.cloud.subscription.backToSignup.message"));
    builder.add("backToSignupMessage", backToSignupMessage);

    backToSignupButton = new JButton(new AbstractAction(Lang.get("import.cloud.subscription.backToSignup.button")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudSignup();
      }
    });
    builder.add("backToSignupButton", backToSignupButton);

    builder.add("close", new AbstractAction(getCloseLabel()) {
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
      case NO_SUBSCRIPTION:
        messageField.setText(Lang.get("import.cloud.subscription.nosubscription", email));
        actionButton.setAction(new BuySubscriptionAction(localDirectory));
        setBackToSignupVisible(true);
        break;
      case EXPIRED:
        messageField.setText(Lang.get("import.cloud.subscription.expired", email));
        actionButton.setAction(new BuySubscriptionAction(localDirectory));
        setBackToSignupVisible(true);
        break;
      default:
        messageField.setText(Lang.get("import.cloud.subscription.other", email));
        actionButton.setAction(new BuySubscriptionAction(localDirectory));
        setBackToSignupVisible(false);
        break;
    }
  }

  private void setBackToSignupVisible(boolean visible) {
    backToSignupMessage.setVisible(visible);
    backToSignupButton.setVisible(visible);
  }

  public void prepareForDisplay() {

  }


  private class BuySubscriptionAction extends BrowsingAction {

    public BuySubscriptionAction(Directory directory) {
      super(Lang.get("import.cloud.subscription.subscribe"), directory);
    }

    protected String getUrl() {
      return Lang.get("site.buy.sync.url");
    }

  }
}
