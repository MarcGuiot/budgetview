package com.budgetview.desktop.importer.steps;

import com.budgetview.bank.BankConnector;
import com.budgetview.bank.BankSynchroService;
import com.budgetview.bank.connectors.SynchroMonitor;
import com.budgetview.bank.connectors.utils.WebTraces;
import com.budgetview.desktop.components.ProgressPanel;
import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.ImportController;
import com.budgetview.desktop.importer.components.RealAccountImporter;
import com.budgetview.desktop.importer.components.SynchroErrorDialog;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Stack;

public class ImportSynchroPanel extends AbstractImportStepPanel {

  private final GlobRepository repository;
  private JPanel panel;
  private BankSynchroService bankSynchroService;
  private JPanel connectorPanel = new JPanel();
  private SynchroMonitor monitor;
  private ProgressPanel progressPanel = new ProgressPanel();
  private JLabel progressLabel = new JLabel();
  private JLabel bankLabel = new JLabel();

  private Stack<BankConnector> currentConnectors = new Stack<BankConnector>();
  private GlobList importedRealAccounts = new GlobList();
  private RealAccountImporter importer;
  private ImportSynchroPanel.CloseDialogAction closeAction;
  private boolean closed = false;
  private BankConnector currentConnector;
  private GlobsPanelBuilder builder;

  public ImportSynchroPanel(PicsouDialog dialog,
                            ImportController controller,
                            GlobRepository repository,
                            Directory directory) {
    super(dialog, Lang.get("close"), controller, directory);
    this.repository = repository;
    this.bankSynchroService = directory.get(BankSynchroService.class);
    this.monitor = new Monitor();
  }

  protected void createPanelIfNeeded() {
    if (builder != null) {
      return;
    }

    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importsteps/importSynchroPanel.splits", repository, localDirectory);

    builder.add("bankLabel", bankLabel);

    builder.add("connectorPanel", connectorPanel);

    builder.add("progressPanel", progressPanel);
    builder.add("progressLabel", progressLabel);

    closeAction = new CloseDialogAction();
    builder.add("close", closeAction);

    panel = builder.load();
  }

  public JPanel getPanel() {
    createPanelIfNeeded();
    return panel;
  }

  public void requestFocus() {
    createPanelIfNeeded();
    if (currentConnector != null) {
      currentConnector.panelShown();
    }
  }

  public void update(Integer bankId, RealAccountImporter importer) {
    closed = false;
    System.out.println("ImportSynchroPanel.update");
    BankConnector connector = bankSynchroService.getConnector(bankId, dialog, repository, localDirectory);
    doUpdate(Collections.singletonList(connector), importer);
  }

  public void update(GlobList synchro, RealAccountImporter importer) {
    closed = false;
    System.out.println("ImportSynchroPanel.update");
    doUpdate(bankSynchroService.getConnectors(synchro, dialog, repository, localDirectory), importer);
  }

  private void doUpdate(java.util.List<BankConnector> connectors, RealAccountImporter importer) {
    System.out.println("ImportSynchroPanel.doUpdate");
    createPanelIfNeeded();
    this.importer = importer;
    this.importedRealAccounts.clear();
    this.currentConnectors.addAll(connectors);
    installNextConnector();
  }

  private void installNextConnector() {
    System.out.println("ImportSynchroPanel.installNextConnector");
    if (closed) {
      return;
    }
    if (currentConnector != null) {
      currentConnector.release();
      currentConnector = null;
    }
    if (currentConnectors.isEmpty()) {
      currentConnector = null;
      importer.importAccounts(importedRealAccounts);
      return;
    }
    currentConnector = currentConnectors.pop();
    currentConnector.init(monitor);
    connectorPanel.removeAll();
    connectorPanel.add(currentConnector.getPanel());
    bankLabel.setText(currentConnector.getLabel());
    bankLabel.setIcon(currentConnector.getIcon());
  }

  public void dispose() {
    if (builder != null) {
      builder.dispose();
      builder = null;
    }
  }

  private class Monitor implements SynchroMonitor {
    public void initialConnection() {
      if (closed) {
        return;
      }
      progressPanel.start();
      progressLabel.setText(Lang.get("import.synchro.progress.initialConnection"));
    }

    public void identificationInProgress() {
      if (closed) {
        return;
      }
      progressPanel.start();
      progressLabel.setText(Lang.get("import.synchro.progress.identificationInProgress"));
    }

    public void identificationFailed(String page) {
      if (closed) {
        return;
      }
      progressPanel.stop();
      progressLabel.setText("");
      SynchroErrorDialog.show(WebTraces.dump(page, currentConnector),
                              SynchroErrorDialog.Mode.LOGIN,
                              dialog, localDirectory);
    }

    public void downloadInProgress() {
      if (closed) {
        return;
      }
      progressPanel.start();
      progressLabel.setText(Lang.get("import.synchro.progress.downloadInProgress"));
    }

    public void preparingAccount(String accountName) {
      progressLabel.setText(Lang.get("import.synchro.progress.preparingAccount", Strings.cut(accountName, 30)));
    }

    public void downloadingAccount(String accountName) {
      progressLabel.setText(Lang.get("import.synchro.progress.downloadForAccount", Strings.cut(accountName, 30)));
    }

    public void waitingForUser() {
      if (closed) {
        return;
      }
      progressPanel.stop();
      progressLabel.setText("");
    }

    public void errorFound(String errorMessage) {
      if (closed) {
        return;
      }
      MessageDialog.show("bank.error", MessageType.ERROR, dialog, localDirectory, "bank.error.msg", errorMessage);
    }

    public void errorFound(Throwable exception) {
      if (closed) {
        return;
      }
      SynchroErrorDialog.show(WebTraces.dump(exception, currentConnector),
                              SynchroErrorDialog.Mode.OTHER,
                              dialog, localDirectory);
    }

    public void info(String message) {
      progressLabel.setText(message);
    }

    public void importCompleted(GlobList realAccounts) {
      if (closed) {
        return;
      }
      importedRealAccounts.addAll(realAccounts);
      System.out.println("Monitor.importCompleted for " + realAccounts);
      installNextConnector();
    }
  }

  private class CloseDialogAction extends AbstractAction {
    public CloseDialogAction() {
      super(textForCloseButton);
    }

    public void actionPerformed(ActionEvent actionEvent) {
      closed = true;
      if (currentConnector != null) {
        currentConnector.stop();
        currentConnector.release();
        currentConnector = null;
      }
      dialog.setVisible(false);
    }
  }
}
