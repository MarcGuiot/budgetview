package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.accounts.AccountPositionEditionDialog;
import org.designup.picsou.gui.accounts.utils.MonthDay;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.dialogs.MessageAndDetailsDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.importer.components.ImportSeriesDialog;
import org.designup.picsou.gui.importer.components.RealAccountImporter;
import org.designup.picsou.gui.importer.steps.*;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.AutomaticSeriesBudgetTrigger;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.SingleComponentLayout;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;

import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class ImportDialog implements RealAccountImporter {
  private GlobRepository parentRepository;
  private Directory parentDirectory;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;

  private ImportController controller;

  private JPanel mainPanel;
  private PicsouDialog dialog;

  private ImportedFileSelectionPanel fileSelectionPanel;
  private ImportSynchroPanel importSynchroPanel;
  private ImportPreviewPanel previewPanel;
  private ImportCompletionPanel completionPanel;
  private ImportAccountPanel importAccountsPanel;
  private ImportStepPanel currentPanel;

  public ImportDialog(String textForCloseButton, List<File> files, Glob defaultAccount,
                      final Window owner, final GlobRepository repository, Directory directory,
                      boolean usePreferredPath, boolean isSynchro) {

    this.parentRepository = repository;
    this.parentDirectory = directory;

    loadLocalRepository(repository);

    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());

    dialog = PicsouDialog.create(owner, directory);
    dialog.setOpenRequestIsManaged(true);

    controller = new ImportController(this, repository, localRepository, directory, isSynchro);
    fileSelectionPanel = new ImportedFileSelectionPanel(dialog, textForCloseButton, controller, usePreferredPath, localRepository, localDirectory);
    importAccountsPanel = new ImportAccountPanel(dialog, textForCloseButton, controller, localRepository, localDirectory);
    importSynchroPanel = new ImportSynchroPanel(dialog, controller, localRepository, localDirectory);
    previewPanel = new ImportPreviewPanel(dialog, textForCloseButton, controller, defaultAccount, repository, localRepository, localDirectory);
    completionPanel = new ImportCompletionPanel(dialog, textForCloseButton, controller, localRepository, localDirectory);

    currentPanel = isSynchro ? importSynchroPanel : fileSelectionPanel;
    initMainPanel(currentPanel);

    preselectFiles(files);

    dialog.setContentPane(mainPanel);

    if (defaultAccount != null) {
      Glob bank = Account.getBank(defaultAccount, localRepository);
      localDirectory.get(SelectionService.class).select(bank);
    }
    dialog.registerDisposable(new Disposable() {
      public void dispose() {
        ImportDialog.this.dispose();
      }
    });
  }

  private void initMainPanel(ImportStepPanel stepPanel) {
    mainPanel = new JPanel();
    mainPanel.setLayout(new SingleComponentLayout(null));
    mainPanel.add(stepPanel.getPanel());
    dialog.setCloseAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        controller.complete();
      }
    });
  }

  private void setCurrentPanel(ImportStepPanel stepPanel) {
    this.currentPanel = stepPanel;
    this.mainPanel.removeAll();
    this.mainPanel.add(stepPanel.getPanel());
    this.dialog.pack();
    GuiUtils.center(dialog);
    stepPanel.requestFocus();
  }

  private void loadLocalRepository(GlobRepository repository) {
    GlobType[] globTypes = {Bank.TYPE, BankEntity.TYPE, MonthDay.TYPE,
                            Account.TYPE, AccountUpdateMode.TYPE, BudgetArea.TYPE,
                            Transaction.TYPE, Month.TYPE, UserPreferences.TYPE, CurrentMonth.TYPE, RealAccount.TYPE,
                            Series.TYPE, SubSeries.TYPE, ImportedSeries.TYPE, TransactionImport.TYPE, CsvMapping.TYPE,
                            Synchro.TYPE};

    if (localRepository == null) {
      this.localRepository = LocalGlobRepositoryBuilder.init(repository)
        .copy(globTypes).get();
      this.localRepository.addTrigger(new AutomaticSeriesBudgetTrigger());
      this.localRepository.addTrigger(new SeriesBudgetTrigger(parentRepository));
    }
    else {
      this.localRepository.rollback();
    }
  }

  public PicsouDialog getDialog() {
    return dialog;
  }

  public void importAccounts(GlobList realAccounts) {
    controller.importAccounts(realAccounts);
  }

  public void preselectFiles(List<File> files) {
    fileSelectionPanel.preselectFiles(files);
  }

  public void updateForNextImport(String absolutePath, List<String> dateFormats, final Glob importedAccount,
                                  Integer accountNumber, Integer accountCount) {
    if (absolutePath != null) {
      previewPanel.setFileName(absolutePath);
    }
    previewPanel.updateForNextImport(dateFormats, importedAccount, accountNumber, accountCount);
  }

  public void showSynchro(Integer bankId) {
    importSynchroPanel.update(bankId, this);
    setCurrentPanel(importSynchroPanel);
  }

  public void showSynchro(GlobList synchro) {
    importSynchroPanel.update(synchro, this);
    setCurrentPanel(importSynchroPanel);
  }

  public void showPreview() {
    setCurrentPanel(previewPanel);
  }

  public void showCompleteMessage(Set<Integer> months, int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransaction) {
    completionPanel.update(months, importedTransactionCount, ignoredTransactionCount, autocategorizedTransaction);
    setCurrentPanel(completionPanel);
  }

  public void show() {
    final PicsouFrame frame = (PicsouFrame)parentDirectory.get(JFrame.class);
    if (frame.isIconified()) {
      frame.addWindowListener(new WindowAdapter() {
        public void windowDeiconified(WindowEvent e) {
          frame.removeWindowListener(this);
          dialog.pack();
          dialog.showCentered();
        }
      });
      final JDialog dialog = new JDialog(frame);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          while (!dialog.isVisible()) {
            SwingUtilities.invokeLater(this);
            try {
              Thread.sleep(200);
            }
            catch (InterruptedException e) {
            }
          }
          dialog.setVisible(false);
        }
      });
      currentPanel.requestFocus();
      dialog.setModal(true);
      dialog.setVisible(true);
    }
    else {
      dialog.pack();
      currentPanel.requestFocus();
      dialog.showCentered();
    }
  }

  protected void closeDialog() {
    dialog.setVisible(false);
  }

  private void dispose() {
    importAccountsPanel.dispose();
    fileSelectionPanel.dispose();
    previewPanel.dispose();
    completionPanel.dispose();
  }

  public void showLastImportedMonthAndClose(Set<Integer> months) {
    GlobList monthsToSelect =
      parentRepository.getAll(Month.TYPE, fieldIn(Month.ID, months)).sort(Month.ID);
    if (!monthsToSelect.isEmpty()) {
      SelectionService selectionService = parentDirectory.get(SelectionService.class);
      selectionService.select(monthsToSelect.getLast());
    }
    closeDialog();
  }

  static class DoubleRef {
    double value = 0.;
  }

  public void showAccountPositionDialogsIfNeeded() {
    Set<Key> transactions = localRepository.getCurrentChanges().getCreated(Transaction.TYPE);
    Map<Integer, DoubleRef> accounts = new HashMap<Integer, DoubleRef>();
    for (Key transaction : transactions) {
      Glob glob = localRepository.get(transaction);
      Integer account = glob.get(Transaction.ACCOUNT);
      DoubleRef value = accounts.get(account);
      if (value == null){
        value = new DoubleRef();
        accounts.put(account, value);
      }
      value.value += glob.get(Transaction.AMOUNT);
    }
    for (Map.Entry<Integer, DoubleRef> accountAndTotal : accounts.entrySet()) {
      Glob account = localRepository.get(Key.create(Account.TYPE, accountAndTotal.getKey()));
      if (account.get(Account.LAST_IMPORT_POSITION) == null && !AccountCardType.DEFERRED.getId().equals(account.get(Account.CARD_TYPE))) {
        AccountPositionEditionDialog dialog =
          new AccountPositionEditionDialog(account, accountAndTotal.getValue().value, localRepository, localDirectory, this.dialog);
        dialog.show();
      }
    }
  }

  public void acceptFiles() {
    fileSelectionPanel.acceptFiles();
  }

  public void showMessage(String message) {
    currentPanel.showFileErrorMessage(message);
  }

  public void showMessage(String message, String details) {
    currentPanel.showFileErrorMessage(message, details);
  }

  public void showNoImport(Glob glob, boolean first) {
    if (first) {
      setCurrentPanel(importAccountsPanel);
    }
    importAccountsPanel.setImportedAccountToImport(glob);
  }

  public boolean askForSeriesImport(Set<Key> newSeries) {
    ImportSeriesDialog dialog = new ImportSeriesDialog(ImportDialog.this.dialog,
                                                       controller.getSessionRepository(), localDirectory);
    return dialog.show(newSeries);
  }

  public static void showLastException(String details, Directory directory) {
    MessageAndDetailsDialog dialog = new MessageAndDetailsDialog("import.file.error.title",
                                                                 "import.file.error.message",
                                                                 details,
                                                                 directory.get(JFrame.class),
                                                                 directory);
    dialog.show();
  }
}
