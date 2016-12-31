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
import com.budgetview.model.CloudDesktopUser;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
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

  private final LocalGlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private GlobRepeat fieldRepeat;
  private Action nextAction;
  private Glob currentConnection;
  private JEditorPane message;

  public ImportCloudBankConnectionPanel(PicsouDialog dialog, ImportController controller, LocalGlobRepository repository, Directory localDirectory) {
    super(dialog, controller, localDirectory);
    this.repository = repository;
    this.cloudService = localDirectory.get(CloudService.class);
  }

  protected GlobsPanelBuilder createPanelBuilder() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importCloudBankConnectionPanel.splits", repository, localDirectory);

    nextAction = new AbstractAction(Lang.get("import.cloud.bankConnection.next")) {
      public void actionPerformed(ActionEvent e) {
        processConnection();
      }
    };
    nextAction.setEnabled(false);

    message = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("message", message);

    fieldRepeat = builder.addRepeat("fields", BudgeaConnectionValue.TYPE, GlobMatchers.NONE, new FieldValueComparator(), new RepeatComponentFactory<Glob>() {
      public void registerComponents(PanelBuilder cellBuilder, Glob connectionValue) {
        CloudConnectionFieldEditor fieldEditor = CloudConnectionFieldEditorFactory.create(connectionValue, repository, localDirectory);
        SplitsNode<JLabel> label = cellBuilder.add("label", fieldEditor.getLabel());
        SplitsNode<JComponent> editor = cellBuilder.add("editor", fieldEditor.getEditor());

        Integer id = connectionValue.get(BudgeaConnectionValue.ID);
        label.getComponent().setName("label:" + id);
        editor.getComponent().setName("editor:" + id);

        cellBuilder.addDisposable(fieldEditor);
      }
    });

    builder.add("next", nextAction);
    builder.add("close", new AbstractAction(getCancelLabel()) {
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

    message.setText(Lang.get("import.cloud.bankConnection.message", bank.get(Bank.NAME)));

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
    cloudService.addBankConnection(currentConnection, repository, new CloudService.BankConnectionCallback() {
      public void processCompletion(Glob providerConnection) {
        repository.commitChanges(false);
        controller.showCloudFirstDownload(providerConnection);
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
