package com.budgetview.desktop.importer;

import com.budgetview.desktop.accounts.AccountPositionEditionDialog;
import com.budgetview.desktop.accounts.utils.MonthDay;
import com.budgetview.desktop.components.PicsouFrame;
import com.budgetview.desktop.components.dialogs.MessageAndDetailsDialog;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.importer.series.ImportSeriesDialog;
import com.budgetview.desktop.importer.components.RealAccountImporter;
import com.budgetview.desktop.importer.series.SeriesImporter;
import com.budgetview.desktop.importer.steps.*;
import com.budgetview.model.*;
import com.budgetview.shared.cloud.CloudSubscriptionStatus;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.triggers.AutomaticSeriesBudgetTrigger;
import com.budgetview.triggers.SeriesBudgetTrigger;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.SingleComponentLayout;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class ImportDialog implements RealAccountImporter, Disposable {
  private GlobRepository parentRepository;
  private Directory parentDirectory;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;

  private ImportController controller;

  private JPanel mainPanel;
  private PicsouDialog dialog;

  private ImportedFileSelectionPanel fileSelectionPanel;
  private ImportCloudSignupPanel cloudSignupPanel;
  private ImportCloudEditionPanel cloudEditionPanel;
  private ImportCloudValidationPanel cloudValidationPanel;
  private ImportCloudBankSelectionPanel cloudBankSelectionPanel;
  private ImportCloudBankConnectionPanel cloudBankConnectionPanel;
  private ImportCloudFirstDownloadPanel cloudFirstDownloadPanel;
  private ImportCloudDownloadPanel cloudDownloadPanel;
  private ImportCloudSubscriptionErrorPanel cloudBankSubscriptionErrorPanel;
  private ImportCloudErrorPanel cloudErrorPanel;
  private ImportPreviewPanel previewPanel;
  private ImportCompletionPanel completionPanel;
  private ImportAccountPanel importAccountsPanel;
  private ImportStepPanel currentPanel;

  private DisposableGroup disposables = new DisposableGroup();

  public ImportDialog(List<File> files, Glob defaultAccount,
                      final Window owner, final GlobRepository repository, Directory directory,
                      boolean usePreferredPath) {

    this.parentRepository = repository;
    this.parentDirectory = directory;

    loadLocalRepository(repository);

    localDirectory = new DefaultDirectory(directory);
    localDirectory.add(new SelectionService());

    dialog = PicsouDialog.create(this, owner, directory);
    dialog.setOpenRequestIsManaged(true);

    controller = new ImportController(this, repository, localRepository, directory);

    fileSelectionPanel = new ImportedFileSelectionPanel(dialog, controller, usePreferredPath, localRepository, localDirectory);
    importAccountsPanel = new ImportAccountPanel(dialog, controller, localRepository, localDirectory);
    cloudSignupPanel = new ImportCloudSignupPanel(dialog, controller, localRepository, localDirectory);
    cloudEditionPanel = new ImportCloudEditionPanel(dialog, controller, localRepository, localDirectory);
    cloudFirstDownloadPanel = new ImportCloudFirstDownloadPanel(dialog, controller, localRepository, localDirectory);
    cloudDownloadPanel = new ImportCloudDownloadPanel(dialog, controller, localRepository, localDirectory);
    cloudValidationPanel = new ImportCloudValidationPanel(dialog, controller, localRepository, localDirectory);
    cloudBankSelectionPanel = new ImportCloudBankSelectionPanel(dialog, controller, localRepository, localDirectory);
    cloudBankConnectionPanel = new ImportCloudBankConnectionPanel(dialog, controller, localRepository, localDirectory);
    cloudBankSubscriptionErrorPanel = new ImportCloudSubscriptionErrorPanel(dialog, controller, localRepository, localDirectory);
    cloudErrorPanel = new ImportCloudErrorPanel(dialog, controller, localRepository, localDirectory);
    previewPanel = new ImportPreviewPanel(dialog, controller, defaultAccount, repository, localRepository, localDirectory);
    completionPanel = new ImportCompletionPanel(dialog, controller, localRepository, localDirectory);

    disposables.addAll(fileSelectionPanel, importAccountsPanel,
                       cloudSignupPanel, cloudEditionPanel, cloudBankSelectionPanel, cloudBankConnectionPanel, cloudFirstDownloadPanel, cloudDownloadPanel, cloudErrorPanel,
                       previewPanel, completionPanel);

    currentPanel = fileSelectionPanel;
    initMainPanel(currentPanel);

    preselectFiles(files);

    dialog.setContentPane(mainPanel);

    if (defaultAccount != null) {
      Glob bank = Account.getBank(defaultAccount, localRepository);
      localDirectory.get(SelectionService.class).select(bank);
    }

    dialog.registerDisposable(this);
  }

  private void initMainPanel(ImportStepPanel stepPanel) {
    mainPanel = new JPanel();
    mainPanel.setLayout(new SingleComponentLayout(null));
    mainPanel.add(stepPanel.getPanel());
    dialog.setCloseAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
        if (controller != null)
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
    this.dialog.revalidate();
    this.dialog.repaint();
    stepPanel.prepareForDisplay();
  }

  private void loadLocalRepository(GlobRepository repository) {
    GlobType[] globTypes = {Bank.TYPE, BankEntity.TYPE, MonthDay.TYPE,
      Account.TYPE, AccountUpdateMode.TYPE, BudgetArea.TYPE,
      Transaction.TYPE, Month.TYPE, UserPreferences.TYPE, CurrentMonth.TYPE, RealAccount.TYPE,
      Series.TYPE, SubSeries.TYPE, ImportedSeries.TYPE, TransactionImport.TYPE, CsvMapping.TYPE,
      Synchro.TYPE, User.TYPE, CloudDesktopUser.TYPE, CloudProviderConnection.TYPE};

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

  public void showPreview() {
    setCurrentPanel(previewPanel);
  }

  public void showCompleteMessage(Set<Integer> months, int importedTransactionCount, int ignoredTransactionCount, int autocategorizedTransaction) {
    completionPanel.update(months, importedTransactionCount, ignoredTransactionCount, autocategorizedTransaction);
    setCurrentPanel(completionPanel);
  }

  public void show() {
    final PicsouFrame frame = (PicsouFrame) parentDirectory.get(JFrame.class);
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
      currentPanel.prepareForDisplay();
      dialog.setModal(true);
      dialog.setVisible(true);
    }
    else {
      dialog.pack();
      currentPanel.prepareForDisplay();
      dialog.showCentered();
    }
  }

  protected void closeDialog() {
    dialog.setVisible(false);
  }

  public void dispose() {
    disposables.dispose();
    controller.complete();
    controller = null;
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

  public void showCloudSignup() {
    setCurrentPanel(cloudSignupPanel);
  }

  public void showCloudEdition() {
    setCurrentPanel(cloudEditionPanel);
    cloudEditionPanel.start();
  }

  public void showCloudValidation(String email) {
    cloudValidationPanel.setEmail(email);
    setCurrentPanel(cloudValidationPanel);
  }

  public void showCloudBankSelection() {
    setCurrentPanel(cloudBankSelectionPanel);
  }

  public void showCloudBankConnection(Key bank) {
    cloudBankConnectionPanel.showStep1(bank);
    setCurrentPanel(cloudBankConnectionPanel);
  }

  public void showCloudError(Exception e) {
    System.out.println("ImportDialog.showCloudError");
    e.printStackTrace();

    cloudErrorPanel.showException(e);
    setCurrentPanel(cloudErrorPanel);
  }

  public void showCloudSubscriptionError(String email, CloudSubscriptionStatus status) {
    cloudBankSubscriptionErrorPanel.update(email, status);
    setCurrentPanel(cloudBankSubscriptionErrorPanel);
  }

  public void showCloudFirstDownload(Glob providerConnectionKey) {
    cloudFirstDownloadPanel.setConnection(providerConnectionKey);
    setCurrentPanel(cloudFirstDownloadPanel);
  }

  public void showCloudFirstDownload() {
    cloudFirstDownloadPanel.setAllConnections();
    setCurrentPanel(cloudFirstDownloadPanel);
    cloudFirstDownloadPanel.updateAll();
  }

  public void showCloudDownload() {
    setCurrentPanel(cloudDownloadPanel);
    cloudDownloadPanel.start();
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
      if (value == null) {
        value = new DoubleRef();
        accounts.put(account, value);
      }
      value.value += glob.get(Transaction.AMOUNT);
    }
    for (Map.Entry<Integer, DoubleRef> accountAndTotal : accounts.entrySet()) {
      Glob account = localRepository.get(Key.create(Account.TYPE, accountAndTotal.getKey()));
      if (account.get(Account.LAST_IMPORT_POSITION) == null && !AccountCardType.DEFERRED.getId().equals(account.get(Account.CARD_TYPE))) {
        double value = accountAndTotal.getValue().value;
        AccountPositionEditionDialog positionDialog =
          new AccountPositionEditionDialog(account, value, localRepository, localDirectory, this.dialog);
        positionDialog.show();
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

  public boolean askForSeriesImport(Set<Key> newSeries, Glob targetAccount) {

    SeriesImporter.updateToKnownSeries(newSeries, targetAccount, controller.getSessionRepository());
    if (newSeries.isEmpty()) {
      return true;
    }

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
