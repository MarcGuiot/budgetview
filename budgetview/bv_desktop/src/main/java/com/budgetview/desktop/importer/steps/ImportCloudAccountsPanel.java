package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.model.CloudProviderAccount;
import com.budgetview.model.CloudProviderConnection;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobComparators;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ImportCloudAccountsPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private JEditorPane message;
  private GlobRepeat repeat;
  private Action backAction;
  private Action applyAction;
  private ProgressPanel progressPanel;
  private Map<Key, Boolean> accountStates = new HashMap<Key, Boolean>();
  private JEditorPane applyMessage;
  private AbstractAction closeAction;

  public ImportCloudAccountsPanel(PicsouDialog dialog, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudEditionPanel.splits", repository, localDirectory);

    message = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("message", message);

    repeat = builder.addRepeat("accounts", CloudProviderAccount.TYPE, GlobMatchers.NONE, GlobComparators.ascending(CloudProviderAccount.NAME), new RepeatComponentFactory<Glob>() {
      public void registerComponents(PanelBuilder cellBuilder, final Glob account) {
        String name = account.get(CloudProviderAccount.NAME);
        JPanel panel = new JPanel();
        cellBuilder.add("accountPanel", panel);
        panel.setName("accountPanel:" + name);

        cellBuilder.add("accountName", new JLabel(name));
        cellBuilder.add("accountNumber", new JLabel(account.get(CloudProviderAccount.NUMBER)));
        JToggleButton toggle = new JToggleButton("", account.isTrue(CloudProviderAccount.ENABLED));
        toggle.addItemListener(new ItemListener() {
          public void itemStateChanged(ItemEvent ev) {
            if (ev.getStateChange() == ItemEvent.SELECTED) {
              toggleAccount(account.getKey(), true);
            }
            else if (ev.getStateChange() == ItemEvent.DESELECTED) {
              toggleAccount(account.getKey(), false);
            }
          }
        });
        cellBuilder.add("toggle", toggle);
      }
    });

    backAction = new AbstractAction(Lang.get("import.cloud.accounts.back")) {
      public void actionPerformed(ActionEvent e) {
        controller.showCloudBankSelection();
      }
    };
    builder.add("back", backAction);

    applyAction = new AbstractAction(Lang.get("import.cloud.accounts.apply")) {
      public void actionPerformed(ActionEvent e) {
        apply();
      }
    };
    builder.add("download", applyAction);

    closeAction = new AbstractAction(getCloseLabel()) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    };
    builder.add("close", closeAction);

    applyMessage = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("applyMessage", applyMessage);

    progressPanel = new ProgressPanel();
    builder.add("progressPanel", progressPanel);

    return builder;
  }

  public void show(Glob cloudProviderConnection) {
    message.setText(Lang.get("import.cloud.accounts.message", cloudProviderConnection.get(CloudProviderConnection.BANK_NAME)));
    accountStates.clear();
    applyAction.setEnabled(false);
    backAction.setEnabled(true);
    closeAction.setEnabled(true);
    applyMessage.setVisible(false);
    repeat.setFilter(linkedTo(cloudProviderConnection, CloudProviderAccount.CONNECTION));
  }

  private void toggleAccount(Key key, boolean enabled) {
    accountStates.put(key, enabled);
    applyAction.setEnabled(accountChanged());
  }

  private boolean accountChanged() {
    for (Map.Entry<Key, Boolean> entry : accountStates.entrySet()) {
      Boolean accountEnabled = repository.get(entry.getKey()).get(CloudProviderAccount.ENABLED);
      if (!Utils.equal(accountEnabled, entry.getValue())) {
        return true;
      }
    }
    return false;
  }

  private void apply() {
    applyMessage.setText(Lang.get("import.cloud.accounts.apply.inprogress"));
    applyMessage.setVisible(true);
    progressPanel.start();
    applyAction.setEnabled(false);
    backAction.setEnabled(false);
    closeAction.setEnabled(false);
    cloudService.updateBankConnections(repository, new CloudService.Callback() {
      public void processCompletion() {
        applyMessage.setText(Lang.get("import.cloud.accounts.apply.completed"));
        applyAction.setEnabled(false);
        backAction.setEnabled(true);
        closeAction.setEnabled(true);
        progressPanel.stop();
      }

      public void processSubscriptionError(CloudSubscriptionStatus status) {
        controller.showCloudSubscriptionError(repository.get(CloudDesktopUser.KEY).get(CloudDesktopUser.EMAIL), status);
        progressPanel.stop();
      }

      public void processError(Exception e) {
        controller.showCloudError(e);
        progressPanel.stop();
      }
    });
  }

  public void prepareForDisplay() {
  }
}
