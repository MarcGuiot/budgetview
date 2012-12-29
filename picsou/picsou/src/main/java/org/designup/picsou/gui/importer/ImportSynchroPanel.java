package org.designup.picsou.gui.importer;

import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.bank.connectors.SynchroMonitor;
import org.designup.picsou.gui.components.ProgressPanel;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.importer.components.RealAccountImporter;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class ImportSynchroPanel {

  private final GlobRepository repository;
  private final Directory directory;
  private Window parent;
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

  public ImportSynchroPanel(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
    this.bankSynchroService = directory.get(BankSynchroService.class);
    this.monitor = new Monitor();
  }

  private void createPanelIfNeeded() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/importexport/importSynchroPanel.splits", repository, directory);

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

  public void update(Integer bankId, RealAccountImporter importer) {
    closed = false;
    BankConnector connector = bankSynchroService.getConnector(bankId, parent, repository, directory);
    doUpdate(Collections.singletonList(connector), importer);
  }

  public void update(GlobList realAccounts, RealAccountImporter importer) {
    closed = false;
    doUpdate(bankSynchroService.getConnectors(realAccounts, parent, repository, directory), importer);
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

    public void identification() {
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
      MessageDialog.show("bank.error", parent, directory, "bank.error.msg", errorMessage);
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
      super(Lang.get("close"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      closed = true;
      if (currentConnector != null) {
        currentConnector.stop();
        currentConnector = null;
      }
      GuiUtils.getEnclosingDialog(panel).setVisible(false);
    }
  }
}
