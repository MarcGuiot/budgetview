package org.designup.picsou.gui.importer.steps;

import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.bank.connectors.SynchroMonitor;
import org.designup.picsou.gui.components.ProgressPanel;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.importer.ImportController;
import org.designup.picsou.gui.importer.components.RealAccountImporter;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
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

    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importSynchroPanel.splits", repository, localDirectory);

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
    BankConnector connector = bankSynchroService.getConnector(bankId, dialog, repository, localDirectory);
    doUpdate(Collections.singletonList(connector), importer);
  }

  public void update(GlobList realAccounts, RealAccountImporter importer) {
    closed = false;
    doUpdate(bankSynchroService.getConnectors(realAccounts, dialog, repository, localDirectory), importer);
  }

  private void doUpdate(java.util.List<BankConnector> connectors, RealAccountImporter importer) {
    createPanelIfNeeded();
    this.importer = importer;
    this.importedRealAccounts.clear();
    this.currentConnectors.addAll(connectors);
    installNextConnector();
  }

  private void installNextConnector() {
    if (closed) {
      return;
    }
    if (currentConnectors.isEmpty()) {
      importer.importAccounts(importedRealAccounts);
      return;
    }
    currentConnector = currentConnectors.pop();
    currentConnector.init(monitor);
    connectorPanel.removeAll();
    connectorPanel.add(currentConnector.getPanel());
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

    public void downloadInProgress() {
      if (closed) {
        return;
      }
      progressPanel.start();
      progressLabel.setText(Lang.get("import.synchro.progress.downloadInProgress"));
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
      MessageDialog.show("bank.error", dialog, localDirectory, "bank.error.msg", errorMessage);
    }

    public void importCompleted(GlobList realAccounts) {
      if (closed) {
        return;
      }
      importedRealAccounts.addAll(realAccounts);
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
        currentConnector = null;
      }
      dialog.setVisible(false);
    }
  }
}
