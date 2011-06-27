package org.designup.picsou.gui.importer;

import com.jidesoft.swing.AutoResizingTextArea;
import org.designup.picsou.gui.accounts.CreateAccountAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.importer.additionalactions.*;
import org.designup.picsou.gui.importer.edition.DateFormatSelectionPanel;
import org.designup.picsou.gui.importer.edition.ImportedTransactionDateRenderer;
import org.designup.picsou.gui.importer.edition.ImportedTransactionsTable;
import org.designup.picsou.gui.importer.utils.QifAccountFinder;
import org.designup.picsou.gui.model.CurrentAccountInfo;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.AccountUpdateMode;
import org.designup.picsou.model.ImportedTransaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class ImportPreviewPanel {
  private ImportController controller;
  private GlobRepository repository;
  private LocalGlobRepository localRepository;
  private Directory localDirectory;

  private GlobRepository sessionRepository;
  private DefaultDirectory sessionDirectory;

  private Glob defaultAccount;
  private Key currentlySelectedAccount;
  private JButton newAccountButton;
  private JComboBox accountComboBox;

  private JLabel fileNameLabel = new JLabel();

  private DateFormatSelectionPanel dateFormatSelectionPanel;
  private ImportedTransactionDateRenderer dateRenderer;

  private JEditorPane message = new JEditorPane();

  private List<AdditionalImportAction> additionalImportActions = new ArrayList<AdditionalImportAction>();
  private List<AdditionalImportAction> currentActions;
  private List<AdditionalImportPanel> additionalImportPanels = new ArrayList<AdditionalImportPanel>();
  private List<AdditionalImportPanel> currentPanels;

  private GlobsPanelBuilder builder;
  private Repeat<AdditionalImportAction> additionalActionsRepeat;
  private Repeat<AdditionalImportPanel> additionalPanelsRepeat;
  private ImportedTransactionsTable importedTransactionTable;
  private JPanel panel;
  private CreateAccountAction createAccountAction;

  public ImportPreviewPanel(ImportController controller,
                            Glob defaultAccount,
                            GlobRepository repository,
                            LocalGlobRepository localRepository,
                            Directory localDirectory) {
    this.controller = controller;
    this.repository = repository;
    this.localRepository = localRepository;
    this.localDirectory = localDirectory;
    this.defaultAccount = defaultAccount;
  }

  public void init(PicsouDialog dialog, final String textForCloseButton) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/importexport/importPreviewPanel.splits", localRepository, localDirectory);
    dateRenderer = new ImportedTransactionDateRenderer();
    dateFormatSelectionPanel = new DateFormatSelectionPanel(localRepository, localDirectory,
                                                            new DateFormatSelectionPanel.Callback() {
                                                              public void dateFormatSelected(String format) {
                                                                dateRenderer.changeDateFormat(format);
                                                              }
                                                            }, message);
    builder.add("dateSelectionPanel", dateFormatSelectionPanel.getBuilder());
    sessionDirectory = new DefaultDirectory(localDirectory);
    sessionDirectory.add(new SelectionService());
    sessionDirectory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        showStep2Message("");
        currentlySelectedAccount = selection.getAll(Account.TYPE).isEmpty() ? null :
                                   selection.getAll(Account.TYPE).get(0).getKey();
      }
    }, Account.TYPE);

    sessionRepository = controller.getSessionRepository();

    importedTransactionTable = new ImportedTransactionsTable(sessionRepository, sessionDirectory, dateRenderer);
    builder.add("table", importedTransactionTable.getTable());
    builder.add("fileName", fileNameLabel);

    createAccountAction = new CreateAccountAction(AccountType.MAIN, sessionRepository, sessionDirectory, dialog)
      .setUpdateModeEditable(false);
    newAccountButton = builder.add("newAccount", new JButton(createAccountAction)).getComponent();

    GlobComboView comboView = GlobComboView.init(Account.TYPE, sessionRepository, sessionDirectory)
      .setEmptyOptionLabel(Lang.get("import.account.combo.select"))
      .setFilter(new GlobMatcher() {
        public boolean matches(Glob account, GlobRepository repository) {
          return account != null &&
                 !Account.SUMMARY_ACCOUNT_IDS.contains(account.get(Account.ID)) &&
                 AccountUpdateMode.AUTOMATIC.getId().equals(account.get(Account.UPDATE_MODE));
        }
      });
    accountComboBox = comboView.getComponent();
    builder.add("accountCombo", accountComboBox);

    registerAccountCreationListener(sessionRepository, sessionDirectory);

    loadAdditionalImportActions(dialog);
    loadAdditionalImportPanels();

    builder.add("importMessage", message);

    additionalPanelsRepeat =
      builder.addRepeat("additionalPanels", Collections.<AdditionalImportPanel>emptyList(),
                        new RepeatComponentFactory<AdditionalImportPanel>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder,
                                                         final AdditionalImportPanel item) {
                            cellBuilder.add("additionalPanel", item.getPanel());
                          }
                        });

    additionalActionsRepeat =
      builder.addRepeat("additionalActions", Collections.<AdditionalImportAction>emptyList(),
                        new RepeatComponentFactory<AdditionalImportAction>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder,
                                                         final AdditionalImportAction item) {
                            cellBuilder.add("message", new AutoResizingTextArea(item.getMessage()));
                            cellBuilder.add("action", new AbstractAction(item.getButtonMessage()) {
                              public void actionPerformed(ActionEvent e) {
                                item.getAction().actionPerformed(e);
                                updateAdditionalImportActions();
                                updateAdditionalImportPanels(true);
                              }
                            });
                          }
                        });

    builder.add("skipFile", new SkipFileAction());
    builder.add("finish", new FinishAction());
    builder.add("close", new CancelAction(textForCloseButton));
    this.panel = builder.load();
  }

  private void loadAdditionalImportActions(PicsouDialog dialog) {
    additionalImportActions.addAll(Arrays.asList(
      new ChooseOrCreateAccount(dialog, sessionRepository, sessionDirectory),
      new BankEntityEditionAction(dialog, sessionRepository, sessionDirectory),
      new AccountEditionAction(dialog, sessionRepository, sessionDirectory),
      new CardTypeAction(dialog, sessionRepository, sessionDirectory)
    ));
  }

  private void loadAdditionalImportPanels() {
    additionalImportPanels.addAll(Arrays.asList(
      new AccountTypeSelectionPanel(sessionRepository, sessionDirectory),
      new AccountOrCardTypeSelectionPanel(sessionRepository, sessionDirectory)
    ));
  }

  private void registerAccountCreationListener(final GlobRepository sessionRepository,
                                               final Directory sessionDirectory) {
    sessionRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        Set<Key> createdAccountKeys = changeSet.getCreated(Account.TYPE);
        if (createdAccountKeys.size() == 1) {
          Glob account = sessionRepository.get(createdAccountKeys.iterator().next());
          sessionDirectory.get(SelectionService.class).select(account);
        }
      }
    });
  }

  public void dispose() {
    builder.dispose();
    importedTransactionTable.dispose();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void showStep2Message(String message) {
    this.message.setText(message);
  }

  public void updateForNextImport(boolean isAccountNeeded, List<String> dateFormats) {
    updateAdditionalImportActions();
    updateAdditionalImportPanels(true);
    initQifAccountChooserFields(isAccountNeeded);
    if (dateFormats != null) {
      dateFormatSelectionPanel.init(dateFormats);
    }
    Glob accountInfo = sessionRepository.find(Key.create(CurrentAccountInfo.TYPE, 0));

    if (accountInfo != null){
      createAccountAction.setAccountInfo(accountInfo);
    }
    else {
      createAccountAction.setAccountInfo(null);
    }
  }

  private void updateAdditionalImportActions() {
    currentActions = new ArrayList<AdditionalImportAction>();
    for (AdditionalImportAction action : additionalImportActions) {
      if (action.shouldApplyAction()) {
        currentActions.add(action);
      }
    }
    additionalActionsRepeat.set(currentActions);
  }

  private void updateAdditionalImportPanels(boolean showErrors) {
    currentPanels = new ArrayList<AdditionalImportPanel>();
    for (AdditionalImportPanel panel : additionalImportPanels) {
      if (panel.shouldBeDisplayed(showErrors)) {
        currentPanels.add(panel);
      }
    }
    additionalPanelsRepeat.set(currentPanels);
  }

  private void initQifAccountChooserFields(boolean isAccountNeeded) {
    if (isAccountNeeded) {
      GlobList accounts = sessionRepository.getAll(Account.TYPE);
      for (Integer accountId : Account.SUMMARY_ACCOUNT_IDS) {
        accounts.remove(sessionRepository.get(Key.create(Account.TYPE, accountId)));
      }
      Glob account = null;
      if (accounts.size() != 0) {
        if (defaultAccount != null) {
          account = sessionRepository.get(defaultAccount.getKey());
        }
        else if (accounts.size() == 1) {
          account = accounts.get(0);
        }
        if (account != null) {
          sessionDirectory.get(SelectionService.class).select(account);
        }
      }
      if (account == null) {
        GlobList importedTransactions = sessionRepository.getAll(ImportedTransaction.TYPE);
        Integer accountId = QifAccountFinder.findQifAccount(importedTransactions, repository);
        if (accountId != null) {
          account = sessionRepository.find(Key.create(Account.TYPE, accountId));
          sessionDirectory.get(SelectionService.class).select(account);
        }else {
          sessionDirectory.get(SelectionService.class).clear(Account.TYPE);
        }
      }

      accountComboBox.setVisible(!accounts.isEmpty());
      newAccountButton.setVisible(!accounts.isEmpty());
    }
    else {
      accountComboBox.setVisible(false);
      newAccountButton.setVisible(false);
    }
  }

  public void setFileName(String absolutePath) {
    fileNameLabel.setText(absolutePath);
  }

  private class FinishAction extends AbstractAction {
    public FinishAction() {
      super(Lang.get("import.preview.ok"));
    }

    public void actionPerformed(ActionEvent event) {
      setEnabled(false);
      try {
        showStep2Message("");
        if (!dateFormatSelectionPanel.check()) {
          return;
        }
        if (!currentActions.isEmpty()) {
          return;
        }
        updateAdditionalImportPanels(true);
        if (!currentPanels.isEmpty()) {
          return;
        }
        if (currentlySelectedAccount == null && controller.isAccountNeeded()) {
          showStep2Message(Lang.get("import.no.account"));
          return;
        }
        controller.completeImport(currentlySelectedAccount, dateFormatSelectionPanel.getSelectedFormat());
      }
      finally {
        setEnabled(true);
      }
    }
  }

  private class SkipFileAction extends AbstractAction {
    private SkipFileAction() {
      super(Lang.get("import.skip.file"));
    }

    public void actionPerformed(ActionEvent e) {
      setEnabled(false);
      try {
        controller.skipFile();
      }
      finally {
        setEnabled(true);
      }
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction(String textForCloseButton) {
      super(textForCloseButton);
    }

    public void actionPerformed(ActionEvent e) {
      controller.closeDialog();
    }
  }
}
