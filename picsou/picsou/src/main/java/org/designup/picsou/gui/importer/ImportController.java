package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.importer.utils.InvalidFileFormat;
import org.designup.picsou.gui.startup.components.AutoCategorizationFunctor;
import org.designup.picsou.gui.startup.components.OpenRequestManager;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.importer.ImportSession;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.*;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.utils.GlobFieldMatcher;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Ref;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationCancelled;

import javax.swing.*;
import java.io.File;
import java.util.*;

import static org.globsframework.model.FieldValue.value;

public class ImportController {

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
  private GlobList realAccountWithImport = new GlobList();
  private GlobList realAccountWithoutImport = new GlobList();
  private boolean isSynchro = true;


  public ImportController(ImportDialog importDialog,
                          GlobRepository repository, LocalGlobRepository localRepository,
                          Directory directory, boolean isSynchro) {
    this.importDialog = importDialog;
    this.repository = repository;
    this.localRepository = localRepository;
    this.directory = directory;
    this.importSession = new ImportSession(localRepository, directory);
    this.isSynchro = isSynchro;

    initOpenRequestManager(directory);
  }

  private void initOpenRequestManager(Directory directory) {
    openRequestManager = directory.get(OpenRequestManager.class);
    openRequestManager.pushCallback(new InImportOpenStep1Callback());
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
    if (!isSynchro) {
      if (!realAccountWithoutImport.isEmpty()) {
        importDialog.showNoImport(realAccountWithoutImport.remove(0), first);
        Log.write("Import:next: noAccount");
        return;
      }
    }
    for (Glob glob : realAccountWithoutImport) {
      Glob target = localRepository.findLinkTarget(glob, RealAccount.ACCOUNT);
      if (target != null) {
        localRepository.update(target.getKey(),
                               value(Account.POSITION, Amounts.extractAmount(glob.get(RealAccount.POSITION))),
                               value(Account.POSITION_DATE, glob.get(RealAccount.POSITION_DATE)),
                               value(Account.TRANSACTION_ID, null));
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
                                         importSession.getImportedOperationsCount(),
                                         autoCategorizationFunctor.getAutocategorizedTransaction(),
                                         autoCategorizationFunctor.getTransactionCount());
        return false;
      }
      catch (Exception e) {
        Log.write("Exception in Import:nextImport", e);
        return false;
      }
    }

    File file;
    Glob realAccount = null;
    if (!realAccountWithImport.isEmpty()) {
      realAccount = realAccountWithImport.remove(0);
      String fileName = realAccount.get(RealAccount.FILE_NAME);
      file = new File(fileName);
    }
    else {
      synchronized (selectedFiles) {
        file = selectedFiles.remove(0);
      }
    }
    try {
      List<String> dateFormats = importSession.loadFile(file, realAccount, importDialog.getDialog());
      Ref<Integer> accountCount = new Ref<Integer>();
      Ref<Integer> accountNumber = new Ref<Integer>();
      Glob importedAccount = importSession.gotoNextContent(accountNumber, accountCount);
      if (importedAccount != null) {
        importDialog.updateForNextImport(file.getAbsolutePath(), dateFormats, importedAccount, accountNumber.get(), accountCount.get());
        return true;
      }
      String message = Lang.get("import.file.empty", file.getAbsolutePath());
      importDialog.showMessage(message);
      return false;
    }
    catch (OperationCancelled e) {
      return false;
    }
    catch (InvalidFileFormat e) {
      String message = e.getMessage(file.getAbsolutePath());
      Log.write(message, e);
      importDialog.showMessage(message, e.getDetails());
      return false;
    }
    catch (Exception e) {
      String message = Lang.get("import.file.error", file.getAbsolutePath());
      Log.write(message, e);
      importDialog.showMessage(message, e.getMessage());
      return false;
    }
  }

  public void commitAndClose(Set<Integer> months) {
    openRequestManager.popCallback();
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

  public void completeImport(Glob importedAccount, Glob currentAccount, String dateFormat) {
    Set<Key> newSeries = importSession.getNewSeries();
    if (!newSeries.isEmpty()){
      importSession.importSeries(importDialog.askForSeriesImport(newSeries));
    }
    Key importKey = importSession.importTransactions(importedAccount, currentAccount, dateFormat);
    if (importKey != null) {
      importKeys.add(importKey.get(TransactionImport.ID));
    }
    nextImport();
  }

  public void complete() {
    openRequestManager.popCallback();
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
                                    monthIds.add(month.get(Transaction.BANK_MONTH));
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

  public void addRealAccountWithoutImport(Glob realAccount) {
    realAccountWithoutImport.add(realAccount);
  }

  public void addRealAccountWithImport(Glob realAccount) {
    realAccountWithImport.add(realAccount);
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
}
