package org.designup.picsou.gui.actions;

import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.model.*;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.utils.ChangeSetAggregator;
import org.crossbowlabs.globs.utils.Log;
import org.crossbowlabs.globs.utils.MultiMap;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.importer.PicsouImportService;
import org.designup.picsou.importer.TypedInputStream;
import org.designup.picsou.importer.analyzer.TransactionAnalyzer;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;
import java.util.Map;

public class ImportFileAction extends AbstractAction {

  private static final String DEFAULT_ACCOUNT_ID = "0";
  private static final String DEFAULT_ACCOUNT_NAME = "Compte principal";
  public static final int DEFAULT_BANK_ID = 99999;
  public static final int DEFAULT_BANK_ENTITY_ID = 30003;

  private GlobRepository repository;
  private Directory directory;
  private Frame frame;
  private PicsouImportService importService;

  public ImportFileAction(GlobRepository repository, Directory directory) {
    super(Lang.get("import"));
    this.frame = directory.get(JFrame.class);
    this.repository = repository;
    this.directory = directory;
    this.importService = directory.get(PicsouImportService.class);
  }

  public void actionPerformed(ActionEvent event) {
    File[] file = queryFile(frame);
    if (file != null) {
      for (int i = 0; i < file.length; i++) {
        processFile(file[i], i == file.length - 1);
      }
    }
  }

  public static File[] queryFile(Component parent) {
    JFileChooser chooser = new JFileChooser();
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(true);
    chooser.addChoosableFileFilter(new FileFilter() {
      public boolean accept(File file) {
        return file.getName().endsWith("ofx") || file.getName().endsWith("qif") || file.isDirectory();
      }

      public String getDescription() {
        return Lang.get("bank.file.format");
      }
    });
    int returnVal = chooser.showOpenDialog(parent);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      File[] selectedFiles = chooser.getSelectedFiles();
      if (selectedFiles == null || selectedFiles.length == 0) {
        System.out.println("no file selected ");
      }
      else {
        for (int i = 0; i < selectedFiles.length; i++) {
          File selectedFile = selectedFiles[i];
          if (!selectedFile.exists()) {
            System.out.println("erreur : file " + selectedFile.getName() + " not found");
          }
        }
      }
      return selectedFiles;
    }
    return null;
  }

  public void processFile(File file, boolean isLastFile) {
    String path = file.getPath();
    if (!path.endsWith(".ofx") && !path.endsWith(".qif")) {
      JOptionPane.showMessageDialog(frame, Lang.get("import.invalid.extension", path));
      return;
    }

    try {
      repository.enterBulkDispatchingMode();
      GlobRepository targetRepository =
        GlobRepositoryBuilder.init(repository.getIdGenerator())
          .add(repository.getAll(Bank.TYPE))
          .add(repository.getAll(BankEntity.TYPE))
          .add(repository.getAll(Account.TYPE))
          .add(repository.getAll(Category.TYPE))
          .get();

      ChangeSetAggregator importChangeSetAggregator = new ChangeSetAggregator(targetRepository);
      targetRepository.enterBulkDispatchingMode();

      final Key importKey = importService.run(new TypedInputStream(file), repository, targetRepository);
      targetRepository.completeBulkDispatchingMode();
      ChangeSet importChangeSet = importChangeSetAggregator.dispose();

      ChangeSetAggregator updateImportAggregator = new ChangeSetAggregator(targetRepository);
      targetRepository.enterBulkDispatchingMode();
//      importChangeSet.getCreated(BankEntity.TYPE);
      if (file.getName().toLowerCase().endsWith("qif")) {
        if (isLastFile) {
          showQifDialog(targetRepository, importKey);
        }
        Integer accountId = createDefaultAccountIfNeeded(targetRepository).get(Account.ID);
        for (Key key : importChangeSet.getCreated(Transaction.TYPE)) {
          targetRepository.update(key, Transaction.ACCOUNT, accountId);
        }
      }
      TransactionAnalyzer transactionAnalyzer = directory.get(TransactionAnalyzerFactory.class).getAnalyzer();
      MultiMap<Integer, Glob> transactionByAccountId = new MultiMap<Integer, Glob>();
      for (Key key : importChangeSet.getCreated(Transaction.TYPE)) {
        Glob transaction = targetRepository.get(key);
        transactionByAccountId.put(transaction.get(Transaction.ACCOUNT), transaction);
      }
      for (Map.Entry<Integer, List<Glob>> accountIdAndTransactions : transactionByAccountId.values()) {
        Glob account = targetRepository.get(Key.create(Account.TYPE, accountIdAndTransactions.getKey()));
        Glob bankEntity = targetRepository.findLinkTarget(account, Account.BANK_ENTITY);
        Glob bank = targetRepository.findLinkTarget(bankEntity, BankEntity.BANK);
        Integer id = Bank.UNKNOWN_BANK_ID;
        if (bank != null) {
          id = bank.get(Bank.ID);
        }
        transactionAnalyzer.processTransactions(id, accountIdAndTransactions.getValue(), targetRepository);
      }
      targetRepository.completeBulkDispatchingMode();
      ChangeSet updateImportChangeSet = updateImportAggregator.dispose();
      repository.apply(importChangeSet);
      repository.apply(updateImportChangeSet);
      AllocationLearningService learningService = directory.get(AllocationLearningService.class);
      for (Map.Entry<Integer, List<Glob>> transactions : transactionByAccountId.values()) {
        learningService.setCategories(transactions.getValue(), repository);
      }
    }
    catch (Exception e) {
      String message = Lang.get("import.file.error", path);
      Log.write(message, e);
      JOptionPane.showMessageDialog(frame, message);
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  private void showQifDialog(GlobRepository targetRepository, Key importKey) {
    QifBalancePanel detailPanel =
      new QifBalancePanel(targetRepository, directory, importKey);
    directory.get(SelectionService.class)
      .select(targetRepository.get(importKey));
    detailPanel.showDialog(frame);
  }

  public static Glob createDefaultAccountIfNeeded(GlobRepository globRepository) {
    Glob account = globRepository.findUnique(Account.TYPE, value(Account.BANK_ENTITY, DEFAULT_BANK_ENTITY_ID));
    if (account == null) {
      return globRepository.create(Key.create(Account.TYPE, 0),
                                   value(Account.NUMBER, DEFAULT_ACCOUNT_ID),
                                   value(Account.NAME, DEFAULT_ACCOUNT_NAME),
                                   value(Account.BANK_ENTITY, DEFAULT_BANK_ENTITY_ID));
    }
    return account;
  }

}
