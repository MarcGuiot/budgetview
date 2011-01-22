package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.accounts.AccountPositionEditionDialog;
import org.designup.picsou.gui.accounts.utils.Day;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.*;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.SingleComponentLayout;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImportDialog {

  private GlobRepository repository;
  private Directory directory;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;

  private ImportController controller;

  private JPanel mainPanel;
  private PicsouDialog dialog;

  private ImportedFileSelectionPanel step1Panel;
  private ImportPreviewPanel step2Panel;

  public ImportDialog(String textForCloseButton, List<File> files, Glob defaultAccount,
                      final Window owner, final GlobRepository repository, Directory directory,
                      boolean usePreferredPath) {

    this.repository = repository;
    this.directory = directory;

    loadLocalRepository(repository);

    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());

    controller = new ImportController(this, repository, localRepository, directory);
    step1Panel = new ImportedFileSelectionPanel(controller, usePreferredPath, localRepository, localDirectory);
    step2Panel = new ImportPreviewPanel(controller, defaultAccount, repository, localRepository, localDirectory);

    dialog = PicsouDialog.create(owner, directory);
    dialog.setOpenRequestIsManaged(true);

    step1Panel.init(dialog, textForCloseButton);
    step2Panel.init(dialog, textForCloseButton);
    initMainPanel();

    updateFileField(files);

    dialog.setContentPane(mainPanel);

    if (defaultAccount != null) {
      Glob bank = Account.getBank(defaultAccount, localRepository);
      localDirectory.get(SelectionService.class).select(bank);
    }
  }

  private void initMainPanel() {
    mainPanel = new JPanel();
    mainPanel.setLayout(new SingleComponentLayout(null));
    mainPanel.add(step1Panel.getPanel());
    dialog.setCloseAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        controller.complete();
      }
    });
  }

  protected void contentChanged() {
    dialog.pack();
    GuiUtils.center(dialog);
  }

  private void loadLocalRepository(GlobRepository repository) {
    GlobType[] globTypes = {Bank.TYPE, BankEntity.TYPE, Day.TYPE,
                            Account.TYPE, AccountUpdateMode.TYPE,
                            Transaction.TYPE, Month.TYPE, UserPreferences.TYPE, CurrentMonth.TYPE};

    if (localRepository == null) {
      this.localRepository = LocalGlobRepositoryBuilder.init(repository)
        .copy(globTypes).get();
    }
    else {
      this.localRepository.rollback();
    }
  }

  public void updateFileField(List<File> files) {
    step1Panel.updateFieldField(files);
  }

  public void setFileName(String absolutePath) {
    step2Panel.setFileName(absolutePath);
  }


  public void updateForNextImport(boolean isAccountNeeded, List<String> dateFormats) throws IOException {
    step2Panel.updateForNextImport(isAccountNeeded, dateFormats);
  }

  protected void closeDialog() {
    dialog.setVisible(false);
  }

  public void show() {
    final PicsouFrame frame = (PicsouFrame)directory.get(JFrame.class);
    if (frame.isIconified()) {
      frame.addWindowListener(new WindowAdapter() {
        public void windowDeiconified(WindowEvent e) {
          frame.removeWindowListener(this);
          dialog.pack();
          dialog.showCentered();
          step1Panel.dispose();
          step2Panel.dispose();
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
      step1Panel.requestFocus();
      dialog.setModal(true);
      dialog.setVisible(true);
    }
    else {
      dialog.pack();
      step1Panel.requestFocus();
      dialog.showCentered();
      step1Panel.dispose();
      step2Panel.dispose();
    }
  }

  public void showLastImportedMonthAndClose(Set<Integer> months) {
    GlobList monthsToSelect =
      repository.getAll(Month.TYPE, GlobMatchers.fieldIn(Month.ID, months)).sort(Month.ID);
    if (!monthsToSelect.isEmpty()) {
      SelectionService selectionService = directory.get(SelectionService.class);
      selectionService.select(monthsToSelect.getLast());
    }
    closeDialog();
  }

  public void showStep2() {
    mainPanel.removeAll();
    mainPanel.add(step2Panel.getPanel());
    contentChanged();
  }

  public void showPositionDialog() {
    Set<Key> transactions = localRepository.getCurrentChanges().getCreated(Transaction.TYPE);
    Set<Integer> accounts = new HashSet<Integer>();
    for (Key transaction : transactions) {
      accounts.add(localRepository.get(transaction).get(Transaction.ACCOUNT));
    }
    for (Integer accountId : accounts) {
      Glob account = localRepository.get(Key.create(Account.TYPE, accountId));
      if (account.get(Account.POSITION) == null) {
        AccountPositionEditionDialog dialog =
          new AccountPositionEditionDialog(account, true, localRepository, localDirectory, this.dialog);
        dialog.show();
      }
    }
  }

  public void showCompleteMessage(int importedTransactionCount, int autocategorizedTransaction, int transactionCount) {
    String messageKey = "close";
    if ((transactionCount > 0) && repository.contains(Series.TYPE, Series.USER_SERIES_MATCHER)) {
      messageKey = "import.end.button";
    }
    MessageDialog.showWithButtonMessage("import.end.info.title",
                                        getEndOfImportMessageKey(importedTransactionCount, transactionCount, autocategorizedTransaction),
                                        dialog, localDirectory,
                                        messageKey,
                                        Integer.toString(transactionCount),
                                        Integer.toString(autocategorizedTransaction),
                                        Integer.toString(importedTransactionCount));

  }

  public static String getEndOfImportMessageKey(int importedTransactionCount, int transactionCount, int autocategorizedTransactions) {
    if (transactionCount == 0) {
      return "import.end.info.operations.none.none." + normalize(importedTransactionCount);
    }
    else {
      if ((transactionCount > 1) && (transactionCount == autocategorizedTransactions)) {
        return "import.end.info.operations.many.all";
      }
      return "import.end.info.operations." + normalize(transactionCount) + "." + normalize(autocategorizedTransactions);
    }
  }

  private static String normalize(int count) {
    if (count == 0) {
      return "none";
    }
    if (count == 1) {
      return "one";
    }
    return "many";
  }

  public void acceptFiles() {
    step1Panel.acceptFiles();
  }

  public void showStep1Message(String message) {
    step1Panel.showMessage(message);
  }

  public void showStep1Message(String message, Exception exception) {
    step1Panel.showMessage(message, exception);
  }
}
