package org.designup.picsou.bank.connectors;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.webcomponents.WebTableCell;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Files;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class OtherBankConnector extends AbstractBankConnector {
  public static int BANK_ID = Bank.GENERIC_BANK_ID;
  private Map<Key, String> files = new HashMap<Key, String>();
  private JComboBox errorModeCombo;
  private JTextField code;

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount,
                                Glob synchro) {
      return new OtherBankConnector(repository, directory, synchro);
    }
  }

  public OtherBankConnector(GlobRepository repository, Directory directory, Glob synchro) {
    super(BANK_ID, repository, directory, synchro);
  }

  public String getCurrentLocation() {
    return "[current]";
  }

  public void stop() {
  }

  protected JPanel createPanel() {
    final SelectionService selectionService = directory.get(SelectionService.class);
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/bank/connection/otherConnectorPanel.splits", repository, directory);
    code = new JTextField();
    builder.add("code", code);
    code.setText(getSyncCode());
    builder.addEditor("number", RealAccount.NUMBER);
    builder.addEditor("name", RealAccount.NAME);
    builder.addEditor("position", RealAccount.POSITION);
    builder.addCheckBox("isSavings", RealAccount.SAVINGS);
    builder.addEditor("file", RealAccount.FILE_NAME);

    JButton addRealAccount = new JButton("add");
    builder.add("add", addRealAccount);
    addRealAccount.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Glob glob = repository.create(RealAccount.TYPE, FieldValue.value(RealAccount.BANK, BANK_ID),
                                      FieldValue.value(RealAccount.FROM_SYNCHRO, true));
        selectionService.select(glob);
      }
    });
    JButton removeRealAccount = new JButton("remove");
    builder.add("remove", removeRealAccount);
    removeRealAccount.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        repository.delete(selectionService.getSelection(RealAccount.TYPE));
      }
    });

    final GlobTableView table = builder.addTable("table", RealAccount.TYPE, new GlobFieldComparator(RealAccount.ID))
      .setFilter(GlobMatchers.fieldEquals(RealAccount.BANK, BANK_ID))
      .addColumn(RealAccount.NUMBER)
      .addColumn(RealAccount.NAME)
      .addColumn(RealAccount.POSITION)
      .addColumn(RealAccount.FILE_NAME);

    errorModeCombo = new JComboBox(ErrorMode.values());
    builder.add("errorModeCombo", errorModeCombo);

    builder.add("update", new AbstractAction("Update") {
      public void actionPerformed(ActionEvent e) {
        notifyDownloadInProgress();
        if (errorModeSelected(ErrorMode.IDENTIFICATION_FAILED)) {
          notifyIdentificationFailed();
          return;
        }
        GlobList displayedAccounts = table.getGlobs();
        for (Glob account : displayedAccounts) {
          files.put(account.getKey(), account.get(RealAccount.FILE_NAME));
        }
        accounts.clear();
        accounts.addAll(displayedAccounts);
        doImport();
      }
    });

    repository.getAll(RealAccount.TYPE, GlobMatchers.fieldEquals(RealAccount.BANK,
                                                                 OtherBankConnector.BANK_ID))
      .safeApply(new GlobFunctor() {
        public void run(Glob glob, GlobRepository repository) throws Exception {
          repository.update(glob.getKey(), RealAccount.FILE_NAME, null);
          repository.update(glob.getKey(), RealAccount.FILE_CONTENT, null);
        }
      }, repository);
    return builder.load();
  }

  public void panelShown() {
    clearErrorMode();
  }

  public void reset() {
    clearErrorMode();
  }

  public void downloadFile() throws Exception {
    if (errorModeSelected(ErrorMode.CONNECTION_ERROR)) {
      throw new RuntimeException("boom");
    }
    for (Map.Entry<Key, String> entry : files.entrySet()) {
      repository.update(entry.getKey(), FieldValue.value(RealAccount.FILE_CONTENT, Strings.isNotEmpty(entry.getValue()) ?
                                                                                   Files.loadFileToString(entry.getValue()): null));
    }
    for (Glob account : accounts) {
      repository.update(account.getKey(), RealAccount.POSITION_DATE, TimeService.getToday());
    }
  }

  protected Double extractAmount(WebTableCell cell) {
    return Amounts.extractAmount(cell.asText());
  }

  public String getCode() {
    return code.getText();
  }

  private boolean errorModeSelected(ErrorMode mode) {
    return mode.equals(errorModeCombo.getSelectedItem());
  }

  private void clearErrorMode() {
    errorModeCombo.setSelectedItem(ErrorMode.NO_ERROR);
  }

  private enum ErrorMode {
    NO_ERROR("No error"),
    IDENTIFICATION_FAILED("Identification failed"),
    CONNECTION_ERROR("Connection error");

    private String label;

    private ErrorMode(String label) {
      this.label = label;
    }

    public String toString() {
      return label;
    }
  }
}
