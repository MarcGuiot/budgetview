package com.budgetview.bank.connectors;

import com.budgetview.bank.BankConnector;
import com.budgetview.bank.connectors.webcomponents.WebBrowser;
import com.budgetview.bank.connectors.webcomponents.WebTableCell;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;
import com.budgetview.model.Account;
import com.budgetview.model.Bank;
import com.budgetview.model.RealAccount;
import com.budgetview.model.Synchro;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.bank.connectors.utils.WebTraces;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.joda.time.DateTime;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public abstract class AbstractBankConnector implements BankConnector {
  private GlobRepository parentRepository;
  private List<Disposable> disposables = new ArrayList<Disposable>();
  protected Directory directory;
  protected Integer bankId;
  protected LocalGlobRepository localRepository;
  protected GlobList accounts = new GlobList();
  private SynchroMonitor monitor = SynchroMonitor.SILENT;
  private JPanel panel;
  protected Glob synchro;

  public AbstractBankConnector(Integer bankId, GlobRepository parentRepository, Directory directory, Glob synchro) {
    this.parentRepository = parentRepository;
    this.directory = directory;
    this.bankId = bankId;
    this.synchro = synchro;
    this.localRepository = LocalGlobRepositoryBuilder.init(parentRepository)
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

  public void addToBeDisposed(Disposable disposable) {
    disposables.add(disposable);
  }

  public void release() {
    for (Disposable disposable : disposables) {
      disposable.dispose();
    }
  }

  public final JPanel getPanel() {
    if (panel == null) {
      panel = createPanel();
    }
    return panel;
  }

  protected abstract JPanel createPanel();

  public Glob createOrUpdateRealAccount(String name, String number, String position, Date date, final Integer bankId) {
    if (Strings.isNullOrEmpty(name) && Strings.isNullOrEmpty(number)) {
      return null;
    }

    Glob account = findOrCreateRealAccount(name, number, bankId, localRepository);

    localRepository.update(account.getKey(),
                           value(RealAccount.POSITION_DATE, date),
                           value(RealAccount.POSITION, Strings.toString(position).trim()),
                           value(RealAccount.FROM_SYNCHRO, true));
    accounts.add(account);
    return account;
  }

  private static Glob findOrCreateRealAccount(String name, String number, Integer bankId, LocalGlobRepository repository) {
    return RealAccount.findOrCreate(Strings.toString(name).trim(), Strings.toString(number).trim(),
                                    bankId, repository);
  }

  protected void createOrUpdateRealAccount(String type, String number,
                                           final String url, String org, String fid) {
    if (!Strings.isNotEmpty(number)) {
      return;
    }

    Glob account = localRepository.getAll(RealAccount.TYPE,
                                          and(fieldEquals(RealAccount.NUMBER, number),
                                         fieldEquals(RealAccount.ACC_TYPE, type),
                                         fieldEquals(RealAccount.URL, url),
                                         fieldEquals(RealAccount.ORG, org),
                                         fieldEquals(RealAccount.FID, fid)))
      .getFirst();
    if (account == null) {
      account = localRepository.create(RealAccount.TYPE,
                                       value(RealAccount.ACC_TYPE, Strings.toString(type).trim()),
                                       value(RealAccount.NUMBER, Strings.toString(number).trim()),
                                       value(RealAccount.URL, url),
                                       value(RealAccount.ORG, org),
                                       value(RealAccount.BANK, bankId),
                                       value(RealAccount.FID, fid),
                                       value(RealAccount.FROM_SYNCHRO, true));
    }
    else {
      localRepository.update(account.getKey(), RealAccount.FROM_SYNCHRO, Boolean.TRUE);
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
      localRepository.update(account.getKey(), RealAccount.FILE_CONTENT, null);
    }
    try {
      downloadFile();

      GlobList otherSynchros = localRepository.getAll(Synchro.TYPE, GlobMatchers.fieldEquals(Synchro.BANK, bankId));
      if (otherSynchros.size() > 2) {
        for (Glob otherSynchro : otherSynchros) {
          if (!otherSynchro.getKey().equals(synchro.getKey())) {
            if (hasAKnownAccount(otherSynchro)) {
              GlobList linkedTo = localRepository.findLinkedTo(otherSynchro, RealAccount.SYNCHRO);
              for (Glob glob : linkedTo) {
                localRepository.update(glob.getKey(), RealAccount.SYNCHRO, null);
              }
              localRepository.delete(otherSynchros);
            }
          }
        }
      }

      localRepository.update(synchro.getKey(), Synchro.CODE, getCode());
      for (Glob account : accounts) {
        localRepository.update(account.getKey(), RealAccount.SYNCHRO, synchro.get(Synchro.ID));
      }
    }
    catch (final Exception e) {
      monitor.errorFound(e);
      return;
    }
    importCompleted();
  }

  public boolean hasAKnownAccount(final Glob sync) {
    GlobList accountsForPreviousSynchro = localRepository.findLinkedTo(sync, RealAccount.SYNCHRO);
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
    localRepository.commitChanges(true);
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

  protected void notifyIdentificationFailed(WebBrowser browser) {
    monitor.identificationFailed(getLoginPage(browser));
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

  private String getLoginPage(WebBrowser browser) {
    if (browser == null) {
      return "";
    }
    return WebTraces.anonymize(browser.getCurrentPage().asXml());

  }
}
