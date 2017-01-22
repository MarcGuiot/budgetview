package com.budgetview.desktop.importer;

import com.budgetview.desktop.description.stringifiers.RealAccountComparator;
import com.budgetview.desktop.importer.components.RealAccountImporter;
import com.budgetview.desktop.importer.utils.InvalidFileFormat;
import com.budgetview.desktop.startup.components.AutoCategorizationFunctor;
import com.budgetview.desktop.startup.components.OpenRequestManager;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.io.importer.ImportSession;
import com.budgetview.io.importer.utils.TypedInputStream;
import com.budgetview.model.*;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.utils.GlobFieldMatcher;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationCancelled;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ImportController implements RealAccountImporter {

  private GlobRepository repository;
  private LocalGlobRepository localRepository;
  private Directory directory;
  private ImportSession importSession;

  private OpenRequestManager openRequestManager;

  private ImportDialog importDialog;
  private final JTextField fileField = new JTextField();

  private boolean step1 = true;
  private boolean step2 = true;

  private final List<File> selectedFiles = new ArrayList<File>();
  private Set<Integer> importKeys = new HashSet<Integer>();
  private List<AccountWithFile> realAccountWithImport = new ArrayList<AccountWithFile>();
  private GlobList realAccountWithoutImport = new GlobList();
  private int countPush;

  public ImportController(ImportDialog importDialog,
                          GlobRepository repository, LocalGlobRepository localRepository,
                          Directory directory) {
    this.importDialog = importDialog;
    this.repository = repository;
    this.localRepository = localRepository;
    this.directory = directory;
    this.importSession = new ImportSession(localRepository, directory);

    initOpenRequestManager(directory);
  }

  private void initOpenRequestManager(Directory directory) {
    openRequestManager = directory.get(OpenRequestManager.class);
    openRequestManager.pushCallback(new InImportOpenStep1Callback());
    countPush++;
  }

  public void doImport() {
    openRequestManager.popCallback();
    openRequestManager.pushCallback(new InImportOpenStep2Callback());
    step1 = false;
    List<File> file = getInitialFiles();
    if (file != null) {
      synchronized (selectedFiles) {
        selectedFiles.addAll(file);
      }
    }
    next(true);
  }

  public void next() {
    next(false);
  }

  private void next(boolean first) {
    if (!realAccountWithoutImport.isEmpty()) {
      importDialog.showNoImport(realAccountWithoutImport.remove(0), first);
      return;
    }
    for (Glob realAccount : realAccountWithoutImport) {
      Glob target = localRepository.findLinkTarget(realAccount, RealAccount.ACCOUNT);
      if (target != null) {
        String amount = realAccount.get(RealAccount.POSITION);
        if (Strings.isNotEmpty(amount)) {
          localRepository.update(target.getKey(), Account.LAST_IMPORT_POSITION, Amounts.extractAmount(amount));
        }
      }
    }
    if (nextImport()) {
      importDialog.showPreview();
    }
  }

  public boolean nextImport() {
    {
      Ref<Integer> accountCount = new Ref<Integer>();
      Ref<Integer> accountNumber = new Ref<Integer>();
      Glob importedAccount = importSession.gotoNextContent(accountNumber, accountCount);
      if (importedAccount != null) {
        importDialog.updateForNextImport(null, null, importedAccount, accountNumber.get(), accountCount.get());
        return true;
      }
    }

    synchronized (selectedFiles) {
      if (selectedFiles.isEmpty() && realAccountWithImport.isEmpty()) {
        step2 = false;
      }
    }
    if (!step2) {
      try {
        Set<Integer> months = createMonths();
        AutoCategorizationFunctor autoCategorizationFunctor = autocategorize();
        deleteEmptyImport();
        importDialog.showAccountPositionDialogsIfNeeded();
        importDialog.showCompleteMessage(months,
                                         autoCategorizationFunctor.getImportedTransactionCount(),
                                         autoCategorizationFunctor.getIgnoredTransactionCount(importSession.getTotalImportedTransactionsCount()),
                                         autoCategorizationFunctor.getAutocategorizedTransactionCount());
        return false;
      }
      catch (Exception e) {
        Log.write("Exception in Import:nextImport", e);
        return false;
      }
    }

    TypedInputStream stream = null;
    String path = null;
    Glob realAccount = null;
    Integer synchroId = null;
    try {
      if (!realAccountWithImport.isEmpty()) {
        AccountWithFile accountWithFile = realAccountWithImport.remove(0);
        realAccount = accountWithFile.realAccount;
        stream = new TypedInputStream(new ByteArrayInputStream(accountWithFile.fileContent.getBytes("UTF-8")));
        synchroId = accountWithFile.synchroId;
        path = null;
      }
      else {
        synchronized (selectedFiles) {
          File file = selectedFiles.remove(0);
          stream = new TypedInputStream(file);
          path = file.getAbsolutePath();
        }
      }
      List<String> dateFormats = importSession.loadFile(realAccount, synchroId, importDialog.getDialog(), stream);
      Ref<Integer> accountCount = new Ref<Integer>();
      Ref<Integer> accountNumber = new Ref<Integer>();
      Glob importedAccount = importSession.gotoNextContent(accountNumber, accountCount);
      if (importedAccount != null) {
        importDialog.updateForNextImport(path, dateFormats, importedAccount, accountNumber.get(), accountCount.get());
        return true;
      }
      String message;
      if (path == null) {
        message = Lang.get("import.downloaded.empty");
      }
      else {
        message = Lang.get("import.file.empty", path);
      }
      importDialog.showMessage(message);
      return false;
    }
    catch (OperationCancelled e) {
      return false;
    }
    catch (InvalidFileFormat e) {
      String message = e.getMessage(path);
      Log.write(message, e);
      importDialog.showMessage(message, e.getDetails());
      return false;
    }
    catch (Exception e) {
      String message = Lang.get("import.file.error", path == null ? "" : path);
      Log.write(message, e);
      importDialog.showMessage(message, e.getMessage());
      return false;
    }
    finally {
      if (stream != null) {
        try {
          stream.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void commitAndClose(Set<Integer> months) {
    System.out.println("ImportController.commitAndClose");
    openRequestManager.popCallback();
    countPush--;
    localRepository.commitChanges(true);
    importDialog.showLastImportedMonthAndClose(months);
  }

  private void deleteEmptyImport() {
    for (Integer key : importKeys) {
      HasOperationFunctor hasOperations = new HasOperationFunctor();
      localRepository.safeApply(Transaction.TYPE,
                                new GlobFieldMatcher(Transaction.IMPORT, key),
                                hasOperations);
      if (hasOperations.isEmpty()) {
        localRepository.delete(Key.create(TransactionImport.TYPE, key));
      }
    }
  }

  public void skipFile() {
    importSession.discard();
    nextImport();
  }

  public void completeImport(Glob targetAccount, String dateFormat) {
    Set<Key> newSeries = importSession.getNewSeries();
    if (!newSeries.isEmpty()) {
      importSession.setImportSeries(importDialog.askForSeriesImport(newSeries, targetAccount));
    }
    Key importKey = importSession.importTransactions(targetAccount, dateFormat);
    if (importKey != null) {
      importKeys.add(importKey.get(TransactionImport.ID));
    }
    nextImport();
  }

  public void complete() {
    while (countPush > 0) {
      openRequestManager.popCallback();
      countPush--;
    }
  }

  private List<File> getInitialFiles() {
    synchronized (fileField) {
      String path = fileField.getText();
      String[] strings = path.split(";");
      List<File> files = new ArrayList<File>();
      for (String string : strings) {
        File file = new File(string);
        if (!file.isDirectory()) {
          files.add(file);
        }
      }
      if (Strings.isNullOrEmpty(path)) {
        return null;
      }
      return files;
    }
  }

  private Set<Integer> createMonths() {
    localRepository.startChangeSet();
    final SortedSet<Integer> monthIds = new TreeSet<Integer>();
    try {
      localRepository.safeApply(Transaction.TYPE,
                                GlobMatchers.fieldIn(Transaction.IMPORT, importKeys),
                                new GlobFunctor() {
                                  public void run(Glob month, GlobRepository repository) throws Exception {
                                    monthIds.add(month.get(Transaction.POSITION_MONTH));
                                    monthIds.add(month.get(Transaction.MONTH));
                                    monthIds.add(month.get(Transaction.BUDGET_MONTH));
                                  }
                                });
      if (monthIds.isEmpty()) {
        return monthIds;
      }
      int firstMonth = monthIds.first();
      TimeService time = directory.get(TimeService.class);
      int currentMonth = time.getCurrentMonthId();
      List<Integer> futureMonth = Month.createMonths(firstMonth, currentMonth);
      futureMonth.addAll(Month.createMonths(monthIds.last(), currentMonth));
      for (int month : futureMonth) {
        localRepository.findOrCreate(Key.create(Month.TYPE, month));
      }
    }
    finally {
      localRepository.completeChangeSet();
    }
    return monthIds;
  }

  private AutoCategorizationFunctor autocategorize() {
    AutoCategorizationFunctor autoCategorizationFunctor = new AutoCategorizationFunctor(repository);
    localRepository.safeApply(Transaction.TYPE,
                              GlobMatchers.fieldIn(Transaction.IMPORT, importKeys),
                              autoCategorizationFunctor);
    return autoCategorizationFunctor;
  }

  public GlobRepository getSessionRepository() {
    return importSession.getTempRepository();
  }

  public void closeDialog() {
    importDialog.closeDialog();
  }

  public JTextField getFileField() {
    return fileField;
  }

  public void importAccounts(GlobList realAccounts) {
    for (Glob realAccount : realAccounts.sortSelf(new RealAccountComparator())) {
      String content = realAccount.get(RealAccount.FILE_CONTENT);
      if (Strings.isNullOrEmpty(content)) {
        addRealAccountWithoutImport(realAccount);
      }
      else {
        addRealAccountWithImport(realAccount);
      }
    }
    doImport();
  }

  private void addRealAccountWithoutImport(Glob realAccount) {
    realAccountWithoutImport.add(realAccount);
  }

  private void addRealAccountWithImport(Glob realAccount) {
    String fileContent = realAccount.get(RealAccount.FILE_CONTENT);
    for (AccountWithFile accountWithFile : realAccountWithImport) {
      // on est en ofx => on prendra le compte dans le fichier.
      if (Strings.isNotEmpty(fileContent) && Utils.equal(fileContent, accountWithFile.fileContent)) {
        accountWithFile.realAccount = null;
        return;
      }
    }
    realAccountWithImport.add(new AccountWithFile(realAccount, fileContent,
                                                  realAccount.get(RealAccount.SYNCHRO)));
  }

  public void showCloudSignup() {
    importDialog.showCloudSignup();
  }

  public void showCloudEdition() {
    importDialog.showCloudEdition();
  }

  public void showCloudValidation(String email) {
    importDialog.showCloudValidation(email);
  }

  public void showCloudBankSelection() {
    importDialog.showCloudBankSelection();
  }

  public void showCloudBankConnection(Key bank) {
    importDialog.showCloudBankConnection(bank);
  }

  public void showCloudTimeout() {
    importDialog.showCloudTimeout();
  }

  public void showCloudError(Exception e) {
    importDialog.showCloudError(e);
  }

  public void showCloudSubscriptionError(String email, CloudSubscriptionStatus status) {
    if (CloudSubscriptionStatus.UNKNOWN_USER.equals(status)) {
      System.out.println("ImportController.showCloudSubscriptionError: deleting all");
      System.out.println("ImportController.showCloudSubscriptionError: before:");
      GlobPrinter.print(localRepository, CloudProviderConnection.TYPE);

      localRepository.deleteAll(CloudDesktopUser.TYPE, CloudProviderConnection.TYPE);
      localRepository.create(CloudDesktopUser.KEY);

      System.out.println("ImportController.showCloudSubscriptionError: after:");
      GlobPrinter.print(localRepository, CloudProviderConnection.TYPE);

      showCloudSignup();
    }
    else {
      importDialog.showCloudSubscriptionError(email, status);
    }
  }

  public void setReplaceSeries(boolean replace) {
    importSession.setReplaceSeries(replace);
  }

  public void showCloudFirstDownload(Glob providerConnection) {
    importDialog.showCloudFirstDownload(providerConnection);
  }

  public void showCloudDownload() {
    importDialog.showCloudDownload();
  }

  private static class HasOperationFunctor implements GlobFunctor {
    private boolean isEmpty = true;

    public void run(Glob glob, GlobRepository repository) throws Exception {
      isEmpty = false;
    }

    boolean isEmpty() {
      return isEmpty;
    }
  }

  public void saveCloudCredentials() {
    localRepository.commitChanges(false);
  }

  private class InImportOpenStep2Callback implements OpenRequestManager.Callback {
    public boolean accept() {
      synchronized (selectedFiles) {
        if (step2) {
          return true;
        }
      }
      return false;
    }

    public void openFiles(final List<File> files) {
      synchronized (selectedFiles) {
        if (step2) {
          for (File file : files) {
            if (!file.isDirectory()) {
              selectedFiles.add(file);
            }
          }
        }
        else {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              try {
                Thread.sleep(50);
              }
              catch (InterruptedException e) {
                // Ignore
              }
              openRequestManager.openFiles(files);
            }
          });
        }
      }
    }
  }

  private class InImportOpenStep1Callback implements OpenRequestManager.Callback {
    public boolean accept() {
      synchronized (fileField) {
        if (step1) {
          return true;
        }
      }
      return false;
    }

    public void openFiles(final List<File> files) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          synchronized (fileField) {
            if (step1) {
              importDialog.preselectFiles(files);
              importDialog.acceptFiles();
            }
            else {
              openRequestManager.openFiles(files);
            }
          }
        }
      });
    }
  }

  private class AccountWithFile {
    private String fileContent;
    private Glob realAccount;
    private Integer synchroId;

    private AccountWithFile(Glob realAccount, String fileName, Integer synchroId) {
      this.realAccount = realAccount;
      this.fileContent = fileName;
      this.synchroId = synchroId;
    }
  }
}
