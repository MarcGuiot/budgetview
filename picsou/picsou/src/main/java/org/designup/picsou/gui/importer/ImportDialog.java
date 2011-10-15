package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.accounts.AccountPositionEditionDialog;
import org.designup.picsou.gui.accounts.utils.MonthDay;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.dialogs.MessageAndDetailsDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.importer.components.ImportSeriesDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.AutomaticSeriesBudgetTrigger;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.SingleComponentLayout;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportDialog {
  private GlobRepository parentRepository;
  private Directory parentDirectory;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;

  private ImportController controller;

  private JPanel mainPanel;
  private PicsouDialog dialog;

  private ImportedFileSelectionPanel fileSelectionPanel;
  private ImportPreviewPanel previewPanel;
  private ImportCompletionPanel completionPanel;
  private ImportAccountPanel importAccountsPanel;
  private MessageHandler messageLabel;

  public ImportDialog(String textForCloseButton, List<File> files, Glob defaultAccount,
                      final Window owner, final GlobRepository repository, Directory directory,
                      boolean usePreferredPath, boolean isSynchro) {

    this.parentRepository = repository;
    this.parentDirectory = directory;

    loadLocalRepository(repository);

    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());

    controller = new ImportController(this, repository, localRepository, directory, isSynchro);
    fileSelectionPanel = new ImportedFileSelectionPanel(controller, usePreferredPath, localRepository, localDirectory);
    importAccountsPanel = new ImportAccountPanel(controller, localRepository, localDirectory);
    previewPanel = new ImportPreviewPanel(controller, defaultAccount, repository, localRepository, localDirectory);
    completionPanel = new ImportCompletionPanel(controller, localRepository, localDirectory);

    dialog = PicsouDialog.create(owner, directory);
    dialog.setOpenRequestIsManaged(true);

    importAccountsPanel.init(dialog, textForCloseButton);
    fileSelectionPanel.init(dialog, textForCloseButton);
    previewPanel.init(dialog, textForCloseButton);
    completionPanel.init(dialog, textForCloseButton);
    initMainPanel();

    preselectFiles(files);

    dialog.setContentPane(mainPanel);

    if (defaultAccount != null) {
      Glob bank = Account.getBank(defaultAccount, localRepository);
      localDirectory.get(SelectionService.class).select(bank);
    }
    messageLabel = fileSelectionPanel;
  }

  private void initMainPanel() {
    mainPanel = new JPanel();
    mainPanel.setLayout(new SingleComponentLayout(null));
    mainPanel.add(fileSelectionPanel.getPanel());
    dialog.setCloseAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        controller.complete();
      }
    });
  }

  private void loadLocalRepository(GlobRepository repository) {
    GlobType[] globTypes = {Bank.TYPE, BankEntity.TYPE, MonthDay.TYPE,
                            Account.TYPE, AccountUpdateMode.TYPE, BudgetArea.TYPE,
                            Transaction.TYPE, Month.TYPE, UserPreferences.TYPE, CurrentMonth.TYPE, RealAccount.TYPE,
                            Series.TYPE, SubSeries.TYPE, ImportedSeries.TYPE};

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

  public void synchronize(GlobList importedAccount) {
    this.fileSelectionPanel.synchronize(importedAccount);
  }

  public void preselectFiles(List<File> files) {
    fileSelectionPanel.preselectFiles(files);
  }

  public void updateForNextImport(String absolutePath, List<String> dateFormats, final Glob importedAccount,
                                  Integer accountNum, Integer accountCount) {
    if (absolutePath != null) {
      previewPanel.setFileName(absolutePath);
    }
    previewPanel.updateForNextImport(dateFormats, importedAccount, accountNum, accountCount);
  }

  public void showPreview() {
    messageLabel = previewPanel;
    setCurrentPanel(previewPanel.getPanel());
  }

  public void showCompleteMessage(Set<Integer> months, int importedTransactionCount, int autocategorizedTransaction, int transactionCount) {
    completionPanel.update(months, importedTransactionCount, autocategorizedTransaction, transactionCount);
    setCurrentPanel(completionPanel.getPanel());
  }

  private void setCurrentPanel(JPanel panel) {
    mainPanel.removeAll();
    mainPanel.add(panel);
    dialog.pack();
    GuiUtils.center(dialog);
  }

  public void show() {
    final PicsouFrame frame = (PicsouFrame)parentDirectory.get(JFrame.class);
    if (frame.isIconified()) {
      frame.addWindowListener(new WindowAdapter() {
        public void windowDeiconified(WindowEvent e) {
          frame.removeWindowListener(this);
          dialog.pack();
          dialog.showCentered();
          dispose();
        }
      });
      final JDialog dialog = new JDialog(frame);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          while (!dialog.isVisible()) {
            SwingUtilities.invokeLater(this);
          }
          dialog.setVisible(false);
        }
      });
      fileSelectionPanel.requestFocus();
      dialog.setModal(true);
      dialog.setVisible(true);
    }
    else {
      dialog.pack();
      fileSelectionPanel.requestFocus();
      dialog.showCentered();
      dispose();
    }
  }

  protected void closeDialog() {
    dialog.setVisible(false);
  }

  private void dispose() {
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

  public void showAccountPositionDialogsIfNeeded() {
    Set<Key> transactions = localRepository.getCurrentChanges().getCreated(Transaction.TYPE);
    Set<Integer> accounts = new HashSet<Integer>();
    for (Key transaction : transactions) {
      accounts.add(localRepository.get(transaction).get(Transaction.ACCOUNT));
    }
    for (Integer accountId : accounts) {
      Glob account = localRepository.get(Key.create(Account.TYPE, accountId));
      if (account.get(Account.POSITION) == null && !AccountCardType.DEFERRED.getId().equals(account.get(Account.CARD_TYPE))) {
        AccountPositionEditionDialog dialog =
          new AccountPositionEditionDialog(account, true, localRepository, localDirectory, this.dialog);
        dialog.show();
      }
    }
  }

  public void acceptFiles() {
    fileSelectionPanel.acceptFiles();
  }

  public void showMessage(String message) {
    messageLabel.showFileErrorMessage(message);
  }

  public void showMessage(String message, Exception exception) {
    messageLabel.showFileErrorMessage(message, exception);
  }

  public void showNoImport(Glob glob, boolean first) {
    if (first) {
      setCurrentPanel(importAccountsPanel.getPanel());
    }
    importAccountsPanel.setImportedAccountToImport(glob);
  }

  public boolean askForSeriesImport(Set<Key> newSeries) {
    ImportSeriesDialog dialog = new ImportSeriesDialog(ImportDialog.this.dialog,
                                                       controller.getSessionRepository(), localDirectory);
    return dialog.show(newSeries);
  }

  public static void showLastException(Exception exception, Directory directory) {
    MessageAndDetailsDialog dialog = new MessageAndDetailsDialog("import.file.error.title",
                                                                 "import.file.error.message",
                                                                 Strings.toString(exception),
                                                                 directory.get(JFrame.class),
                                                                 directory);
    dialog.show();
  }
}
