package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.gui.startup.AutoCategorizationFunctor;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.importer.ImportSession;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionImport;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.GlobFieldMatcher;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.io.File;
import java.util.*;

public class ImportController {

  private GlobRepository repository;
  private LocalGlobRepository localRepository;
  private Directory directory;
  private ImportSession importSession;

  private OpenRequestManager openRequestManager;

  private ImportDialog importDialog;
  private final JTextField fileField;

  private boolean step1 = true;
  private boolean step2 = true;
  private boolean completed = false;

  private final List<File> selectedFiles = new ArrayList<File>();
  private Set<Integer> importKeys = new HashSet<Integer>();

  public ImportController(ImportDialog importDialog, JTextField fileField,
                          GlobRepository repository, LocalGlobRepository localRepository, Directory directory) {
    this.importDialog = importDialog;
    this.fileField = fileField;
    this.repository = repository;
    this.localRepository = localRepository;
    this.directory = directory;
    this.importSession = new ImportSession(localRepository, directory);

    initOpenRequestManager(directory);
  }

  private void initOpenRequestManager(Directory directory) {
    openRequestManager = directory.get(OpenRequestManager.class);
    openRequestManager.pushCallback(new OpenRequestManager.Callback() {
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
                importDialog.updateFileField(files);
                importDialog.acceptFiles();
              }
              else {
                openRequestManager.openFiles(files);
              }
            }
          }
        });
      }
    });
  }

  public void doImport() {
    openRequestManager.popCallback();
    openRequestManager.pushCallback(new OpenRequestManager.Callback() {
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
    });
    step1 = false;
    List<File> file = getInitialFiles();
    synchronized (selectedFiles) {
      selectedFiles.addAll(file);
    }
    if (nextImport()) {
      importDialog.showStep2();
    }
  }

  public boolean nextImport() {

    synchronized (selectedFiles) {
      if (selectedFiles.isEmpty()) {
        step2 = false;
      }
    }
    if (completed) {
      return true;
    }
    if (!step2) {
      try {
        completed = true;
        Set<Integer> months = createMonths();
        AutoCategorizationFunctor autoCategorizationFunctor = autocategorize();
        deleteEmptyImport();
        importDialog.showPositionDialog();

        importDialog.showCompleteMessage(importSession.getImportedOperationsCount(),
                                         autoCategorizationFunctor.getAutocategorizedTransaction(),
                                         autoCategorizationFunctor.getTransactionCount());
        openRequestManager.popCallback();
        localRepository.commitChanges(true);
        importDialog.showLastImportedMonthAndClose(importSession.getImportedOperationsCount() != 0, months);
        return true;
      }
      catch (Exception e) {
        Log.write("nextImport", e);
        return false;
      }
    }

    File file;
    synchronized (selectedFiles) {
      file = selectedFiles.remove(0);
    }
    try {
      importDialog.setFileName(file.getAbsolutePath());
      List<String> dateFormats = importSession.loadFile(file);
      importDialog.updateForNextImport(isAccountNeeded(), dateFormats);
      return true;
    }
    catch (NoOperations e) {
      String message = Lang.get("import.file.empty");
      importDialog.showStep1Message(message);
      return false;
    }
    catch (Exception e) {
      String message = Lang.get("import.file.error");
      Log.write(message, e);
      importDialog.showStep1Message(message, e);
      return false;
    }
  }

  private void deleteEmptyImport() {
    for (Integer key : importKeys) {
      HasOperationFunctor hasOperations = new HasOperationFunctor();
      localRepository.safeApply(Transaction.TYPE,
                                new GlobFieldMatcher(Transaction.IMPORT, key),
                                hasOperations);
      if (hasOperations.isEmpty()){
        localRepository.delete(Key.create(TransactionImport.TYPE, key));
      }
    }
  }

  public void skipFile() {
    importSession.discard();
    nextImport();
  }

  public void finish(Key currentAccount, String dateFormat) {
    Key importKey = importSession.importTransactions(currentAccount, dateFormat);
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

  public boolean isAccountNeeded() {
    return importSession.isAccountNeeded();
  }

  private static class HasOperationFunctor implements GlobFunctor {
    private boolean isEmpty = true;

    public void run(Glob glob, GlobRepository repository) throws Exception {
      isEmpty = false;
    }

    boolean isEmpty(){
      return isEmpty;
    }
  }
}
