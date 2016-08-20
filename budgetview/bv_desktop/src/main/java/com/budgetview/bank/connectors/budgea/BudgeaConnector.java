package com.budgetview.bank.connectors.budgea;

import com.budgetview.bank.BankConnector;
import com.budgetview.bank.BankConnectorFactory;
import com.budgetview.bank.connectors.AbstractBankConnector;
import com.budgetview.bank.connectors.webcomponents.utils.UserAndPasswordPanel;
import com.budgetview.model.RealAccount;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import static org.globsframework.model.FieldValue.value;

public class BudgeaConnector extends AbstractBankConnector {

  public static final int BANK_ID = -999;

  public static class Factory implements BankConnectorFactory {

    private BudgeaConnection connection;

    public BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount, Glob synchro) {
      if (connection == null) {
        connection = new BudgeaConnection();
      }
      return new BudgeaConnector(BANK_ID, connection, repository, directory, synchro);
    }
  }

  private BudgeaConnection connection;
  private UserAndPasswordPanel userAndPasswordPanel;

  public BudgeaConnector(Integer bankId, BudgeaConnection connection, GlobRepository repository, Directory directory, Glob synchro) {
    super(bankId, repository, directory, synchro);
    this.connection = connection;
  }

  public String getLabel() {
    return "Connecteur de test";
  }

  protected JPanel createPanel() {
    userAndPasswordPanel = new UserAndPasswordPanel(new ConnectAction(), directory);
    JPanel panel = userAndPasswordPanel.createPanel(this);
    userAndPasswordPanel.setUserCode(getSyncCode());
    userAndPasswordPanel.setEnabled(true);
    return panel;
  }

  public String getCode() {
    return userAndPasswordPanel.getUser();
  }

  private class ConnectAction extends AbstractAction {

    public void actionPerformed(ActionEvent event) {
      notifyIdentificationInProgress();
      userAndPasswordPanel.setEnabled(false);
      userAndPasswordPanel.setFieldsEnabled(false);

      directory.get(ExecutorService.class).submit(new Runnable() {
        public void run() {
          try {
            doImport();
          }
          catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                notifyErrorFound(e);
              }
            });
          }
        }
      });
    }
  }

  public void downloadFile() throws Exception {
    GlobList realAccounts = connection.loadRealAccounts(bankId, new BudgeaConnection.AccountFactory() {
      public Glob findOrCreateAccount(String name, String number, String position, Date date, String budgeaId) {
        Glob realAccount = BudgeaConnector.this.createOrUpdateRealAccount(name, number, position, date, BANK_ID);
        localRepository.update(realAccount, value(RealAccount.PROVIDER_ACCOUNT_ID, Integer.parseInt(budgeaId)));
        return realAccount;
      }
    });

    for (Glob realAccount : realAccounts) {
      connection.loadTransactionFiles(realAccount, localRepository);
    }

  }

  public void panelShown() {
    userAndPasswordPanel.requestFocus();
  }

  public String getCurrentLocation() {
    return null;
  }

  public void stop() {

  }

  public void reset() {

  }
}
