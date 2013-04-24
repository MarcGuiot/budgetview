package org.designup.picsou.bank.connectors;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.connectors.webcomponents.WebTableCell;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.Synchro;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.model.*;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.joda.time.DateTime;

import javax.swing.*;
import java.util.Date;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public abstract class AbstractBankConnector implements BankConnector {
  private GlobRepository parentRepository;
  protected Directory directory;
  protected Integer bankId;
  protected LocalGlobRepository repository;
  protected GlobList accounts = new GlobList();
  private SynchroMonitor monitor = SynchroMonitor.SILENT;
  private JPanel panel;
  protected Glob synchro;

  public AbstractBankConnector(Integer bankId, GlobRepository parentRepository, Directory directory, Glob synchro) {
    this.parentRepository = parentRepository;
    this.directory = directory;
    this.bankId = bankId;
    this.synchro = synchro;
    this.repository = LocalGlobRepositoryBuilder.init(parentRepository)
      .copy(Account.TYPE, RealAccount.TYPE, Synchro.TYPE)
      .get();
  }

  public String getLabel() {
    return getBank().get(Bank.NAME);
  }

  public Icon getIcon() {
    String iconPath = getBank().get(Bank.ICON);
    if (Strings.isNullOrEmpty(iconPath)) {
      return null;
    }
    return directory.get(ImageLocator.class).get(iconPath);
  }

  private Glob getBank() {
    return parentRepository.get(Key.create(Bank.TYPE, bankId));
  }

  public void init(SynchroMonitor monitor) {
    this.monitor = new SwingSynchroMonitor(monitor);
  }

  public final JPanel getPanel() {
    if (panel == null) {
      panel = createPanel();
    }
    return panel;
  }

  protected abstract JPanel createPanel();

  protected Glob createOrUpdateRealAccount(String name, String number, String position, Date date, final Integer bankId) {
    if (Strings.isNullOrEmpty(name) && Strings.isNullOrEmpty(number)) {
      return null;
    }

    Glob account = findOrCreateRealAccount(name, number, bankId);

    repository.update(account.getKey(),
                      value(RealAccount.POSITION_DATE, date),
                      value(RealAccount.POSITION, Strings.toString(position).trim()),
                      value(RealAccount.FROM_SYNCHRO, true));
    accounts.add(account);
    return account;
  }

  public Glob findOrCreateRealAccount(String name, String number, Integer bankId) {
    return RealAccount.findOrCreate(Strings.toString(name).trim(), Strings.toString(number).trim(),
                                    bankId, repository);
  }

  protected void createOrUpdateRealAccount(String type, String number,
                                           final String url, String org, String fid) {
    if (!Strings.isNotEmpty(number)) {
      return;
    }

    Glob account = repository.getAll(RealAccount.TYPE,
                                     and(fieldEquals(RealAccount.NUMBER, number),
                                         fieldEquals(RealAccount.ACC_TYPE, type),
                                         fieldEquals(RealAccount.URL, url),
                                         fieldEquals(RealAccount.ORG, org),
                                         fieldEquals(RealAccount.FID, fid)))
      .getFirst();
    if (account == null) {
      account = repository.create(RealAccount.TYPE,
                                  value(RealAccount.ACC_TYPE, Strings.toString(type).trim()),
                                  value(RealAccount.NUMBER, Strings.toString(number).trim()),
                                  value(RealAccount.URL, url),
                                  value(RealAccount.ORG, org),
                                  value(RealAccount.BANK, bankId),
                                  value(RealAccount.FID, fid),
                                  value(RealAccount.FROM_SYNCHRO, true));
    }
    else {
      repository.update(account.getKey(), RealAccount.FROM_SYNCHRO, Boolean.TRUE);
    }

    accounts.add(account);
  }

  public abstract void downloadFile() throws Exception;

  protected Double extractAmount(WebTableCell cell) throws WebParsingError {
    return Amounts.extractAmount(cell.asText());
  }

  public abstract String getCode();

  public String getSyncCode() {
    return synchro.get(Synchro.CODE);
  }

  public void doImport() {
    for (Glob account : accounts) {
      repository.update(account.getKey(), RealAccount.FILE_CONTENT, null);
    }
    try {
      downloadFile();
      if (Strings.isNullOrEmpty(synchro.get(Synchro.CODE))) {
        repository.update(synchro.getKey(), Synchro.CODE, getCode());
        for (Glob account : accounts) {
          repository.update(account.getKey(), RealAccount.SYNCHO, synchro.get(Synchro.ID));
        }
      }
      else if (!getCode().equals(synchro.get(Synchro.CODE))) {
        if (hasAKnownAccount()) {
          repository.update(synchro.getKey(), Synchro.CODE, getCode());
        }
        else {
          // autre telechargement
          Glob otherSynchro = repository.getAll(Synchro.TYPE,
                                                GlobMatchers.and(GlobMatchers.fieldEquals(Synchro.CODE, getCode()),
                                                                 GlobMatchers.fieldEquals(Synchro.BANK, bankId))).getFirst();
          if (otherSynchro == null) {
            otherSynchro = repository.create(Synchro.TYPE, FieldValue.value(Synchro.CODE, getCode()),
                                             FieldValue.value(Synchro.BANK, bankId));
          }
          for (Glob account : accounts) {
            repository.update(account.getKey(), RealAccount.SYNCHO, otherSynchro.get(Synchro.ID));
          }
        }
      }
    }
    catch (final Exception e) {
      monitor.errorFound(e);
      return;
    }
    importCompleted();
  }

  private boolean hasAKnownAccount() {
    GlobList accountsForPreviousSynchro = repository.findLinkedTo(synchro, RealAccount.SYNCHO);
    Set<Key> accountsForPreviousSynchroKeySet = accountsForPreviousSynchro.getKeySet();
    // changement de code
    for (Glob account : accounts) {
      if (accountsForPreviousSynchroKeySet.contains(account.getKey())) {
        // probablement un changement de code car au moins un compte en commun.
        return true;
      }
    }
    return false;
  }

  protected void importCompleted() {
    repository.commitChanges(true);
    monitor.importCompleted(accounts);
  }

  protected String getAccountName(Glob account) {
    return account.get(RealAccount.NAME) + " " + account.get(RealAccount.NUMBER);
  }

  protected void notifyInitialConnection() {
    monitor.initialConnection();
  }

  protected void notifyPreparingAccount(String accountName) {
    monitor.downloadingAccount(accountName);
  }

  protected void notifyDownloadForAccount(String accountName) {
    monitor.downloadingAccount(accountName);
  }

  protected void notifyIdentificationInProgress() {
    monitor.identificationInProgress();
  }

  protected void notifyIdentificationFailed() {
    monitor.identificationFailed();
  }

  protected void notifyDownloadInProgress() {
    monitor.downloadInProgress();
  }

  protected void notifyWaitingForUser() {
    monitor.waitingForUser();
  }

  protected void notifyErrorFound(String message) {
    monitor.errorFound(message);
  }

  protected void notifyInfo(String message) {
    monitor.info(message);
  }

  protected void notifyErrorFound(Throwable exception) {
    monitor.errorFound(exception);
  }

  protected Date getYesterdaysDate() {
    DateTime today = new DateTime();
    return today.minusDays(1).toDate();
  }
}
