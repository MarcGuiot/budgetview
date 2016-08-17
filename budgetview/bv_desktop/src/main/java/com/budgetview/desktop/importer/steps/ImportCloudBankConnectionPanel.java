package com.budgetview.desktop.importer.steps;

import com.budgetview.budgea.model.BudgeaBank;
import com.budgetview.budgea.model.BudgeaBankField;
import com.budgetview.budgea.model.BudgeaConnection;
import com.budgetview.budgea.model.BudgeaConnectionValue;
import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.desktop.importer.components.CloudConnectionFieldEditor;
import com.budgetview.desktop.importer.components.CloudConnectionFieldEditorFactory;
import com.budgetview.model.Bank;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ImportCloudBankConnectionPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private GlobRepeat fieldRepeat;
  private Action nextAction;
  private Glob currentConnection;

  public ImportCloudBankConnectionPanel(PicsouDialog dialog, String textForCloseButton, ImportController controller, GlobRepository repository, Directory localDirectory) {
    super(dialog, textForCloseButton, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudBankConnectionPanel.splits", repository, localDirectory);

    nextAction = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        processConnection();
      }
    };
    nextAction.setEnabled(false);

    fieldRepeat = builder.addRepeat("fields", BudgeaConnectionValue.TYPE, GlobMatchers.NONE, new FieldValueComparator(), new RepeatComponentFactory<Glob>() {
      public void registerComponents(PanelBuilder cellBuilder, Glob connectionValue) {
        CloudConnectionFieldEditor editor = CloudConnectionFieldEditorFactory.create(connectionValue, repository, localDirectory);
        cellBuilder.add("label", editor.getLabel());
        cellBuilder.add("editor", editor.getEditor());

        cellBuilder.addDisposable(editor);
      }
    });

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

  public void setCurrentBank(Key bankKey) {
    createPanelIfNeeded();

    Glob bank = repository.get(bankKey);
    Integer currentBudgeaBankId = bank.get(Bank.PROVIDER_ID);
    Glob budgeaBank = repository.findOrCreate(Key.create(BudgeaBank.TYPE, currentBudgeaBankId));
    GlobList fields = repository.findLinkedTo(budgeaBank, BudgeaBankField.BANK);
    if (fields.isEmpty()) {
      nextAction.setEnabled(false);
      throw new UnexpectedApplicationState("No fields declared for bank: " + budgeaBank);
    }

    repository.startChangeSet();
    repository.deleteAll(BudgeaConnection.TYPE, BudgeaConnectionValue.TYPE);
    currentConnection = repository.create(Key.create(BudgeaConnection.TYPE, currentBudgeaBankId));
    for (Glob field : fields) {
      repository.create(BudgeaConnectionValue.TYPE,
                        value(BudgeaConnectionValue.CONNECTION, currentBudgeaBankId),
                        value(BudgeaConnectionValue.FIELD, field.get(BudgeaBankField.ID)));
    }
    repository.completeChangeSet();

    fieldRepeat.setFilter(linkedTo(currentConnection, BudgeaConnectionValue.CONNECTION));

    nextAction.setEnabled(true);
  }

  private void processConnection() {
    progressPanel.start();
    GuiUtils.runLater(new Runnable() {
      public void run() {
        cloudService.createConnection(currentConnection, repository, new CloudService.Callback() {
          public void processCompletion() {
            startDownload();
          }

          public void processError() {
            controller.showCloudError();
            progressPanel.stop();
          }
        });
      }
    });

  }

  private void startDownload() {
    GuiUtils.runLater(new Runnable() {
      public void run() {
        cloudService.downloadStatement(currentConnection, repository, new CloudService.DownloadCallback() {
          public void processCompletion(GlobList importedRealAccounts) {
            controller.importAccounts(importedRealAccounts);
            progressPanel.stop();
          }

          public void processError() {
            controller.showCloudError();
            progressPanel.stop();
          }
        });

      }
    });
  }


  public void requestFocus() {

  }

  private class FieldValueComparator implements Comparator<Glob> {
    public int compare(Glob connectionValue1, Glob connectionValue2) {
      Glob field1 = repository.findLinkTarget(connectionValue1, BudgeaConnectionValue.FIELD);
      Glob field2 = repository.findLinkTarget(connectionValue2, BudgeaConnectionValue.FIELD);
      if (field1 == null) {
        return -1;
      }
      if (field2 == null) {
        return 1;
      }
      return Utils.compare(field1.get(BudgeaBankField.FIELD_TYPE), field2.get(BudgeaBankField.FIELD_TYPE));
    }
  }
}
