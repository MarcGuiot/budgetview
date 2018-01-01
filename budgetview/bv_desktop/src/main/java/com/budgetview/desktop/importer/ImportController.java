package com.budgetview.desktop.importer;

import com.budgetview.desktop.cloud.CloudService;
import com.budgetview.desktop.description.stringifiers.RealAccountComparator;
import com.budgetview.desktop.importer.components.RealAccountImporter;
import com.budgetview.desktop.importer.utils.InvalidFileFormat;
import com.budgetview.desktop.startup.components.AutoCategorizationFunctor;
import com.budgetview.desktop.startup.components.OpenRequestManager;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.desktop.undo.UndoRedoService;
import com.budgetview.io.importer.ImportSession;
import com.budgetview.io.importer.utils.TypedInputStream;
import com.budgetview.model.*;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.shared.utils.Amounts;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
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

import static org.globsframework.model.FieldValue.value;

public class ImportController implements RealAccountImporter, Disposable {

  private GlobRepository repository;
  private LocalGlobRepository localRepository;
  private Directory directory;
  private ImportSession importSession;

  private OpenRequestManager openRequestManager;

  private ImportDisplay display;
  private JTextField fileField = new JTextField();

  private boolean step1 = true;
  private boolean step2 = true;

  private List<File> selectedFiles = new ArrayList<File>();
  private Set<Integer> importKeys = new HashSet<Integer>();
  private List<AccountWithFile> realAccountWithImport = new ArrayList<AccountWithFile>();
  private GlobList realAccountWithoutImport = new GlobList();
  private int countPush;

  public ImportController(ImportDisplay importDisplay,
                          GlobRepository repository, LocalGlobRepository localRepository,
                          Directory directory) {
    this.display = importDisplay;
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
      display.showNoImport(realAccountWithoutImport.remove(0), first);
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
      display.showPreview();
    }
  }

  private boolean nextImport() {
    {
      Ref<Integer> accountCount = new Ref<Integer>();
      Ref<Integer> accountNumber = new Ref<Integer>();
      Glob realAccount = importSession.gotoNextContent(accountNumber, accountCount);
      if (realAccount != null) {
        String filePath = realAccount.get(RealAccount.FILE_NAME);
        display.updateForNextImport(filePath, null, realAccount, accountNumber.get(), accountCount.get());
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
        display.showAccountPositionDialogsIfNeeded();
        display.showImportCompleted(months,
                                    autoCategorizationFunctor.getImportedTransactionCount(),
                                    autoCategorizationFunctor.getIgnoredTransactionCount(importSession.getTotalImportedTransactionsCount()),
                                    autoCategorizationFunctor.getAutocategorizedTransactionCount());
        return false;
      }
      catch (Exception e) {
        Log.write("[Import] Exception in Import:nextImport", e);
        return false;
      }
    }

    TypedInputStream stream = null;
    String filePath = null;
    Glob realAccount = null;
    try {
      if (!realAccountWithImport.isEmpty()) {
        AccountWithFile accountWithFile = realAccountWithImport.remove(0);
        realAccount = accountWithFile.realAccount;
        String fileName = realAccount != null ? realAccount.get(RealAccount.FILE_NAME) : null;
        stream = new TypedInputStream(new ByteArrayInputStream(accountWithFile.fileContent.getBytes("UTF-8")), fileName);
        filePath = realAccount.get(RealAccount.FILE_NAME);
      }
      else {
        synchronized (selectedFiles) {
          File file = selectedFiles.remove(0);
          stream = new TypedInputStream(file);
          filePath = file.getAbsolutePath();
        }
      }
      List<String> dateFormats = importSession.loadFile(realAccount, stream, display.getParentWindow());
      Ref<Integer> accountCount = new Ref<Integer>();
      Ref<Integer> accountNumber = new Ref<Integer>();
      Glob importedAccount = importSession.gotoNextContent(accountNumber, accountCount);
      if (importedAccount != null) {
        display.updateForNextImport(filePath, dateFormats, importedAccount, accountNumber.get(), accountCount.get());
        return true;
      }
      String message;
      if (filePath == null) {
        message = Lang.get("import.downloaded.empty");
      }
      else {
        message = Lang.get("import.file.empty", filePath);
      }
      display.showMessage(message);
      return false;
    }
    catch (OperationCancelled e) {
      return false;
    }
    catch (InvalidFileFormat e) {
      String message = e.getMessage(filePath);
      Log.write(message, e);
      display.showMessage(message, e.getDetails());
      return false;
    }
    catch (Exception e) {
      String message = Lang.get("import.file.error", filePath == null ? "" : filePath);
      Log.write(message, e);
      display.showMessage(message, e.getMessage());
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
    openRequestManager.popCallback();
    countPush--;
    updateCloudAccounts();
    localRepository.commitChanges(true);
    display.showLastImportedMonthAndClose(months);
  }

  public void updateCloudAccounts() {
    directory.get(CloudService.class).updateAccounts(localRepository.getCurrentChanges(), localRepository, new CloudService.Callback() {
      public void processCompletion() {
      }

      public void processSubscriptionError(CloudSubscriptionStatus status) {
      }

      public void processError(Exception e) {
        Log.write("Failed to update account statuses", e);
      }
    });
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

  public void completeImportForAccount(Glob targetAccount, String dateFormat) {
    Set<Key> newSeries = importSession.getNewSeries();
    if (!newSeries.isEmpty()) {
      importSession.setImportSeries(display.askForSeriesImport(newSeries, targetAccount));
    }
    Key importKey = importSession.completeImport(targetAccount, dateFormat);
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
    System.out.println("ImportController.autocategorize: " + importKeys);
    localRepository.safeApply(Transaction.TYPE,
                              GlobMatchers.fieldIn(Transaction.IMPORT, importKeys),
                              autoCategorizationFunctor);
    return autoCategorizationFunctor;
  }

  public GlobRepository getSessionRepository() {
    return importSession.getTempRepository();
  }

  public void closeDialog() {
    display.closeDialog();
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
    realAccountWithImport.add(new AccountWithFile(realAccount, fileContent));
  }

  public void showCloudSignup() {
    display.showCloudSignup();
  }

  public void showModifyCloudEmail() {
    display.showModifyCloudEmail();
  }

  public void showCloudEdition() {
    display.showCloudEdition();
  }

  public void showCloudAccounts(Glob cloudProviderConnection) {
    display.showCloudAccounts(cloudProviderConnection);
  }

  public void showCloudValidationForSignup(String email) {
    display.showCloudValidationForSignup(email);
  }

  public void showCloudValidationForEmailModification(String email) {
    display.showCloudValidationForEmailModification(email);
  }

  public void showCloudEmailModificationCompleted(String newEmail) {
    display.showCloudEmailModificationCompleted(newEmail);
  }

  public void showCloudBankSelection() {
    display.showCloudBankSelection();
  }

  public void showCloudBankConnection(Key bank) {
    display.showCloudBankConnection(bank);
  }

  public void updatePassword(Glob cloudProviderConnection) {
    display.updatePassword(cloudProviderConnection);
  }

  public void showCloudUnsubscription() {
    display.showCloudUnsubscription();
  }

  public void showCloudError(Exception e) {
    display.showCloudError(e);
  }

  public void showCloudSubscriptionError(String email, CloudSubscriptionStatus status) {
    display.showCloudSubscriptionError(email, status);
  }

  public void showCloudFirstDownload(Glob providerConnection) {
    display.showCloudFirstDownload(providerConnection);
  }

  public void showCloudDownload() {
    display.showCloudDownload();
  }

  public void setReplaceSeries(boolean replace) {
    importSession.setReplaceSeries(replace);
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

  public void saveCloudUnsubscription() {
    localRepository.startChangeSet();
    localRepository.update(CloudDesktopUser.KEY,
                           value(CloudDesktopUser.EMAIL, null),
                           value(CloudDesktopUser.DEVICE_TOKEN, null),
                           value(CloudDesktopUser.LAST_UPDATE, null),
                           value(CloudDesktopUser.REGISTERED, false),
                           value(CloudDesktopUser.SYNCHRO_ENABLED, false));
    localRepository.deleteAll(CloudProviderConnection.TYPE);
    localRepository.completeChangeSet();
    localRepository.commitChanges(false);
    directory.get(UndoRedoService.class).removeLastUndo();
  }

  public void saveCloudCredentials() {
    localRepository.commitChanges(false);
    directory.get(UndoRedoService.class).removeLastUndo();
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
              display.preselectFiles(files);
              display.acceptFiles();
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

    private AccountWithFile(Glob realAccount, String fileName) {
      this.realAccount = realAccount;
      this.fileContent = fileName;
    }
  }

  public void dispose() {
    repository = null;
    localRepository = null;
    directory = null;
    importSession = null;
    openRequestManager = null;
    display = null;
    fileField = null;
    selectedFiles = null;
    importKeys = null;
    realAccountWithImport = null;
    realAccountWithoutImport = null;
  }
}
