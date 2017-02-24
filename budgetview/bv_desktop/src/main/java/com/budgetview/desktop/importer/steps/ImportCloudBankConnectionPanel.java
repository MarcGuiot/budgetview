package com.budgetview.desktop.importer.steps;

import com.budgetview.budgea.model.*;
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
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ImportCloudBankConnectionPanel extends AbstractImportStepPanel {

  private final LocalGlobRepository repository;
  private final CloudService cloudService;
  private ProgressPanel progressPanel;
  private GlobRepeat fieldRepeat;
  private Action nextAction;
  private java.util.List<JComponent> components = new ArrayList<JComponent>();
  private Glob currentConnection;
  private JEditorPane message;
  private JEditorPane errorMessage;
  private boolean step2;
  private int providerConnectionId;
  private Glob currentBank;

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

    fieldRepeat = builder.addRepeat("fields", BudgeaConnectionValue.TYPE, GlobMatchers.NONE, new GlobFieldComparator(BudgeaConnectionValue.SEQUENCE_INDEX), new RepeatComponentFactory<Glob>() {
      public void registerComponents(PanelBuilder cellBuilder, Glob connectionValue) {
        CloudConnectionFieldEditor fieldEditor = CloudConnectionFieldEditorFactory.create(connectionValue, repository, localDirectory);
        JLabel label = cellBuilder.add("label", fieldEditor.getLabel()).getComponent();
        final JComponent editor = cellBuilder.add("editor", fieldEditor.getEditor()).getComponent();

        Glob field = repository.findLinkTarget(connectionValue, BudgeaConnectionValue.FIELD);
        final String fieldLabel = field.get(BudgeaBankField.LABEL);

        label.setName("label:" + fieldLabel);
        editor.setName("editor:" + fieldLabel);
        components.add(label);
        components.add(editor);

        cellBuilder.addDisposable(fieldEditor);
        cellBuilder.addDisposable(new Disposable() {
          public void dispose() {
            components.remove(fieldLabel);
            components.remove(editor);
          }
        });
      }
    });

    errorMessage = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("errorMessage", errorMessage);
    errorMessage.setVisible(false);

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

  public void showStep1(Key bankKey) {
    System.out.println("ImportCloudBankConnectionPanel.showStep1");
    createPanelIfNeeded();
    step2 = false;
    providerConnectionId = 0;
    currentBank = repository.get(bankKey);
    message.setText(Lang.get("import.cloud.bankConnection.message", currentBank.get(Bank.NAME)));
    updateFieldEditors();
  }

  private void showStep2(int connectionId) {
    System.out.println("ImportCloudBankConnectionPanel.showStep2");
    step2 = true;
    providerConnectionId = connectionId;
    message.setText(Lang.get("import.cloud.bankConnection.message.step2", currentBank.get(Bank.NAME)));
    updateFieldEditors();
  }

  public void updateFieldEditors() {
    Integer currentBudgeaBankId = currentBank.get(Bank.PROVIDER_ID);
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
                        value(BudgeaConnectionValue.FIELD, field.get(BudgeaBankField.ID)),
                        value(BudgeaConnectionValue.SEQUENCE_INDEX, field.get(BudgeaBankField.SEQUENCE_INDEX)));
    }
    repository.completeChangeSet();

    fieldRepeat.setFilter(linkedTo(currentConnection, BudgeaConnectionValue.CONNECTION));
    nextAction.setEnabled(true);
    errorMessage.setVisible(false);
  }

  private void processConnection() {
    System.out.println("ImportCloudBankConnectionPanel.processConnection");

    for (Glob fieldValue : repository.getAll(BudgeaConnectionValue.TYPE).sort(BudgeaConnectionValue.SEQUENCE_INDEX)) {
      Glob field = repository.findLinkTarget(fieldValue, BudgeaConnectionValue.FIELD);
      String value = fieldValue.get(BudgeaConnectionValue.VALUE);
      if (Strings.isNullOrEmpty(value)) {
        showErrorMessage("import.cloud.bankConnection.message.emptyField", field.get(BudgeaBankField.LABEL));
        return;
      }
      String regex = field.get(BudgeaBankField.REGEX);
      if (Strings.isNotEmpty(regex)) {
        Pattern pattern  = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        if (!matcher.matches()) {
          showErrorMessage("import.cloud.bankConnection.message.invalidValue", field.get(BudgeaBankField.LABEL));
          return;
        }
      }
    }

    errorMessage.setVisible(false);
    progressPanel.start();
    setAllEnabled(false);
    CloudService.BankConnectionCallback callback = new CloudService.BankConnectionCallback() {
      public void processCompletion(Glob providerConnection) {
        System.out.println("ImportCloudBankConnectionPanel.processCompletion");
        repository.deleteAll(BudgeaModel.get().asArray());
        repository.commitChanges(false);
        controller.showCloudFirstDownload(providerConnection);
        progressPanel.stop();
      }

      public void processSecondStepResponse(int connectionId) {
        System.out.println("ImportCloudBankConnectionPanel.processSecondStepResponse");
        showStep2(connectionId);
        progressPanel.stop();
      }

      public void processSubscriptionError(CloudSubscriptionStatus status) {
        repository.deleteAll(BudgeaModel.get().asArray());
        controller.showCloudSubscriptionError(repository.get(CloudDesktopUser.KEY).get(CloudDesktopUser.EMAIL), status);
        progressPanel.stop();
      }

      public void processCredentialsRejected() {
        showErrorMessage("import.cloud.bankConnection.message.rejected");
        progressPanel.stop();
        setAllEnabled(true);
      }

      public void processError(Exception e) {
        repository.deleteAll(BudgeaModel.get().asArray());
        controller.showCloudError(e);
        progressPanel.stop();
      }
    };
    if (!step2) {
      cloudService.addBankConnection(currentBank, currentConnection, repository, callback);
    }
    else {
      cloudService.addBankConnectionStep2(providerConnectionId, currentConnection, repository, callback);
    }
  }

  public void showErrorMessage(String key, String... args) {
    String text = Lang.get(key, args).replace("<html>", "").replace("</html>", "");
    errorMessage.setText("<html><font color=red>" + text + "</font></html>");
    errorMessage.setVisible(true);
  }

  public void prepareForDisplay() {
    setAllEnabled(true);
  }

  private void setAllEnabled(boolean enabled) {
    nextAction.setEnabled(enabled);
    for (JComponent component : components) {
      component.setEnabled(enabled);
    }
  }
}
