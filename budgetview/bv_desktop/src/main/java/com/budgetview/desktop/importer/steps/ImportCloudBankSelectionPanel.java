package com.budgetview.desktop.importer.steps;

import com.budgetview.desktop.bank.BankChooserPanel;
import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.model.Bank;
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.shared.model.Provider;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.utils.GlobMatchers.*;

public class ImportCloudBankSelectionPanel extends AbstractImportStepPanel implements GlobSelectionListener {

  private final GlobRepository repository;
  private BankChooserPanel bankChooserPanel;
  private CloudService cloudService;
  private SelectionService selectionService;
  private ProgressPanel progressPanel;
  private Action nextAction;

  public ImportCloudBankSelectionPanel(PicsouDialog dialog, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
    this.selectionService = localDirectory.get(SelectionService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudBankSelectionPanel.splits", repository, localDirectory);

    nextAction = new AbstractAction(Lang.get("import.cloud.bankSelection.next")) {
      public void actionPerformed(ActionEvent e) {
        processSelection();
      }
    };

    bankChooserPanel = new BankChooserPanel(repository, localDirectory, nextAction,
                                            and(fieldEquals(Bank.PROVIDER, Provider.BUDGEA.getId()),
                                                isNotNull(Bank.PROVIDER_ID)), dialog);
    builder.add("bankChooserPanel", bankChooserPanel.getPanel());

    builder.add("next", nextAction);
    builder.add("close", new AbstractAction(getCancelLabel()) {
      public void actionPerformed(ActionEvent e) {
        controller.complete();
        controller.closeDialog();
      }
    });

    progressPanel = new ProgressPanel();
    builder.add("progressPanel", progressPanel);

    selectionService.addListener(this, Bank.TYPE);

    return builder;
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList banks = selection.getAll(Bank.TYPE);
    nextAction.setEnabled(banks.size() == 1);
  }

  private void processSelection() {
    Glob bank = selectionService.getSelection(Bank.TYPE).getFirst();
    if (bank == null) {
      return;
    }

    progressPanel.start();
    GuiUtils.runLater(new Runnable() {
      public void run() {
        cloudService.updateBankFields(bank.getKey(), repository, new CloudService.Callback() {
          public void processCompletion() {
            controller.showCloudBankConnection(bank.getKey());
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
    });
  }

  public void prepareForDisplay() {
    createPanelIfNeeded();
    bankChooserPanel.requestFocus();
    nextAction.setEnabled(!selectionService.getSelection(Bank.TYPE).isEmpty());
  }

  public void dispose() {
    super.dispose();
    if (bankChooserPanel != null) {
      bankChooserPanel.dispose();
      bankChooserPanel = null;
    }
    if (selectionService != null) {
      selectionService.removeListener(this);
      selectionService = null;
    }
  }
}
