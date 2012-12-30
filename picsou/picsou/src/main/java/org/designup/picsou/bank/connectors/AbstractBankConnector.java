package org.designup.picsou.bank.connectors;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public abstract class AbstractBankConnector implements BankConnector {
  protected Directory directory;
  protected Integer bankId;
  protected LocalGlobRepository repository;
  protected GlobList accounts = new GlobList();
  private SynchroMonitor monitor = SynchroMonitor.SILENT;

  public AbstractBankConnector(Directory directory, GlobRepository repository, Integer bankId) {
    this.directory = directory;
    this.bankId = bankId;
    this.repository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE, RealAccount.TYPE)
      .get();
  }

  public void init(SynchroMonitor monitor) {
    this.monitor = monitor;
  }

  protected void createOrUpdateRealAccount(String name, String number, String position, Date date, final Integer bankId) {
    if (Strings.isNullOrEmpty(name) && Strings.isNullOrEmpty(number)) {
      return;
    }

    Glob account = repository.getAll(RealAccount.TYPE,
                                     and(fieldEquals(RealAccount.NAME, Strings.toString(name).trim()),
                                         fieldEquals(RealAccount.NUMBER, Strings.toString(number).trim()),
                                         fieldEquals(RealAccount.BANK, bankId)))
      .getFirst();
    if (account == null) {
      account = repository.create(RealAccount.TYPE,
                                  value(RealAccount.NAME, Strings.toString(name).trim()),
                                  value(RealAccount.NUMBER, Strings.toString(number).trim()),
                                  value(RealAccount.BANK, bankId),
                                  value(RealAccount.POSITION_DATE, date),
                                  value(RealAccount.POSITION, Strings.toString(position).trim()),
                                  value(RealAccount.FROM_SYNCHRO, true));
    }
    else {
      repository.update(account.getKey(),
                        value(RealAccount.POSITION_DATE, date),
                        value(RealAccount.POSITION, Strings.toString(position).trim()));
    }
    accounts.add(account);
  }

  protected void createOrUpdateRealAccount(String type, String number,
                                           final String url, String org, String fid) {
    if (Strings.isNotEmpty(number)) {
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
  }

  public static File createQifLocalFile(Glob realAccount, InputStream contentAsStream, String charset) {
    File file;
    try {
      file = File.createTempFile("download", ".qif");
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      fileOutputStream.write(("! accountId=" + realAccount.get(RealAccount.ID) + "\n").getBytes());
      Files.copyInUtf8(contentAsStream, charset, fileOutputStream);
      file.deleteOnExit();
    }
    catch (IOException e) {
      Log.write("Can not create temporary file.");
      return null;
    }
    return file;
  }

  public abstract void downloadFile();

  protected Double extractAmount(String position) {
    return Amounts.extractAmount(position);
  }

  public void doImport() {
    for (Glob account : accounts) {
      repository.update(account.getKey(), RealAccount.FILE_NAME, null);
    }
    try {
      downloadFile();
    }
    catch (Exception e) {
      monitor.errorFound(e.getMessage());
    }
    repository.commitChanges(true);
    monitor.importCompleted(accounts);
  }

  protected void notifyInitialConnection() {
    monitor.initialConnection();
  }

  protected void notifyIdentification() {
    monitor.identificationInProgress();
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
}
