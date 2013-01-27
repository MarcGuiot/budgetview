package org.designup.picsou.gui.importer.components;

import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.gui.bank.BankChooserPanel;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.help.HelpDialog;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.importer.ImportController;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

import static org.globsframework.model.utils.GlobMatchers.isTrue;

public class BankDownloadPanel implements GlobSelectionListener {
  private Window parent;
  private ImportController controller;
  private GlobRepository repository;
  private Directory directory;
  private JPanel panel;

  private BankChooserPanel bankChooser;
  private CardHandler mainCards;
  private CardHandler selectionCards;
  private JEditorPane manualDownloadMessage;

  private JPanel synchroPanel;
  private Integer bankId;
  private GlobsPanelBuilder builder;
  private Repeat<SynchroAccountGroup> synchroAccountsRepeat;

  public BankDownloadPanel(Window parent,
                           ImportController controller,
                           GlobRepository repository,
                           Directory directory) {
    this.parent = parent;
    this.controller = controller;
    this.repository = repository;
    this.directory = directory;
  }

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  private void createPanel() {
    builder = new GlobsPanelBuilder(getClass(),
                                    "/layout/importexport/components/bankDownloadPanel.splits",
                                    repository, directory);

    mainCards = builder.addCardHandler("mainCards");

    selectionCards = builder.addCardHandler("selectionCards");

    synchroAccountsRepeat =
      builder.addRepeat("synchroAccountGroups", Collections.<SynchroAccountGroup>emptyList(), new SynchroAccountRepeatFactory());
    builder.add("startSynchro", new AbstractAction(Lang.get("import.synchroSelection.synchro.button")) {
      public void actionPerformed(ActionEvent event) {
        controller.showSynchro(repository.getAll(RealAccount.TYPE, GlobMatchers.isTrue(RealAccount.FROM_SYNCHRO)));
      }
    });

    builder.add("showBankDownload", new AbstractAction(Lang.get("import.synchroSelection.bankSelection.button")) {
      public void actionPerformed(ActionEvent event) {
        mainCards.show("bankSelection");
      }
    });

    synchroPanel = new JPanel();
    builder.add("synchroPanel", synchroPanel);
    builder.add("synchronize", new AbstractAction(Lang.get("synchro.open")) {
      public void actionPerformed(ActionEvent actionEvent) {
        controller.showSynchro(bankId);
      }
    });

    builder.add("securityInfo", OfxSecurityInfoButton.create(directory));

    builder.add("gotoBankSelection",
                new AbstractAction(Lang.get("bankDownload.manualDownload.back.button")) {
                  public void actionPerformed(ActionEvent actionEvent) {
                    mainCards.show("bankSelection");
                  }
                });

    AbstractAction gotoManualDownload =
      new AbstractAction(Lang.get("bankDownload.selection.manual.button")) {
        public void actionPerformed(ActionEvent actionEvent) {
          mainCards.show("manualDownload");
        }
      };
    builder.add("gotoManualDownload", gotoManualDownload);

    manualDownloadMessage = GuiUtils.createReadOnlyHtmlComponent();
    HelpDialog.initHtmlEditor(manualDownloadMessage);
    builder.add("manualDownloadMessage", manualDownloadMessage);

    bankChooser = new BankChooserPanel(repository, directory, gotoManualDownload, null, parent);
    builder.add("bankChooserPanel", bankChooser.getPanel());

    final HyperlinkHandler hyperlinkHandler = new HyperlinkHandler(directory, parent);
    hyperlinkHandler.registerLinkAction("manualInput", new GotoTransactionCreationFunctor());
    builder.add("hyperlinkHandler", hyperlinkHandler);

    panel = builder.load();

    SelectionService selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, Bank.TYPE);
    selectionService.clear(Bank.TYPE);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (bankId != null && changeSet.containsChanges(Key.create(Bank.TYPE, bankId))) {
          update(repository.find(Key.create(Bank.TYPE, bankId)));
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update(bankId == null ? null : repository.find(Key.create(Bank.TYPE, bankId)));
      }
    });

    List<SynchroAccountGroup> accountGroups = getSynchroAccountGroups();
    if (accountGroups.isEmpty()) {
      mainCards.show("bankSelection");
    }
    else {
      synchroAccountsRepeat.set(accountGroups);
      mainCards.show("synchroSelection");
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList banks = selection.getAll(Bank.TYPE);
    Glob bank = banks.size() == 1 ? banks.getFirst() : null;
    update(bank);
  }

  private void update(Glob bank) {
    bankId = bank != null ? bank.get(Bank.ID) : null;
    selectionCards.show(bank == null ? "noSelection" : "gotoSite");

    boolean showSynchro = (bank != null) && bank.isTrue(Bank.SYNCHRO_ENABLED) && BankSynchroService.SHOW_SYNCHRO;
    Utils.beginRemove();
    if (bank != null && bank.get(Bank.ID).equals(Bank.GENERIC_BANK_ID)) {
      showSynchro = true;
    }
    Utils.endRemove();
    synchroPanel.setVisible(bank != null && (showSynchro || bank.get(Bank.OFX_DOWNLOAD, false)));

    if (bank != null) {
      String manualDownloadText = getManualDownloadText(bank);
      manualDownloadMessage.setText(manualDownloadText);
      GuiUtils.scrollToTop(manualDownloadMessage);
    }
  }

  private String getManualDownloadText(Glob bank) {
    String bankHelp = directory.get(HelpService.class).getBankHelp(bank);
    if (Strings.isNotEmpty(bankHelp)) {
      return bankHelp;
    }

    String url = bank.get(Bank.URL);
    String bankName = directory.get(DescriptionService.class).getStringifier(Bank.TYPE)
      .toString(bank, repository);
    if (Strings.isNotEmpty(url)) {
      return Lang.get("bankDownload.manualDownload.message.url", bankName, url);
    }

    return Lang.get("bankDownload.manualDownload.message.nourl", bankName);
  }

  public void requestFocus() {
    bankChooser.requestFocus();
  }

  public void dispose() {
    builder.dispose();
    bankChooser.dispose();
  }

  private class GotoTransactionCreationFunctor implements Runnable {
    public void run() {
      controller.complete();
      controller.closeDialog();
      directory.get(NavigationService.class).highlightTransactionCreation();
    }
  }

  private List<SynchroAccountGroup> getSynchroAccountGroups() {
    GlobList realAccounts = repository.getAll(RealAccount.TYPE, isTrue(RealAccount.FROM_SYNCHRO));
    GlobList accounts = realAccounts.getTargets(RealAccount.ACCOUNT, repository);
    System.out.println("BankDownloadPanel.getSynchroAccountGroups: ");
    GlobPrinter.print(repository, RealAccount.TYPE);
    GlobPrinter.print(accounts);
    accounts.sort(new GlobFieldsComparator(Account.ACCOUNT_TYPE, true,
                                           Account.POSITION_DATE, true,
                                           Account.NAME, true));
    List<SynchroAccountGroup> result = new ArrayList<SynchroAccountGroup>();
    Map<Glob, SynchroAccountGroup> groups = new HashMap<Glob, SynchroAccountGroup>();
    for (Glob account : accounts) {
      Glob bank = repository.findLinkTarget(account, Account.BANK);
      SynchroAccountGroup group = groups.get(bank);
      if (group == null) {
        group = new SynchroAccountGroup(bank);
        result.add(group);
        groups.put(bank, group);
      }
      group.add(account);
    }

    return result;
  }

  private class SynchroAccountGroup {
    final Glob bank;
    final GlobList accounts = new GlobList();

    private SynchroAccountGroup(Glob bank) {
      this.bank = bank;
    }

    public void add(Glob account) {
      accounts.add(account);
    }
  }

  private class SynchroAccountRepeatFactory implements RepeatComponentFactory<SynchroAccountGroup> {
    public void registerComponents(RepeatCellBuilder groupCellBuilder, SynchroAccountGroup accountsGroup) {
      groupCellBuilder.add("bankLabel", createBankLabel(accountsGroup));

      groupCellBuilder.addRepeat("synchroAccounts", accountsGroup.accounts, new RepeatComponentFactory<Glob>() {
        public void registerComponents(RepeatCellBuilder cellBuilder, Glob account) {
          cellBuilder.add("synchroAccountLabel", new JLabel(account.get(Account.NAME)));
        }
      });
    }

    private JLabel createBankLabel(SynchroAccountGroup accountsGroup) {
      JLabel bankLabel = new JLabel();
      String iconPath = accountsGroup.bank.get(Bank.ICON);
      if (iconPath != null) {
        bankLabel.setIcon(directory.get(ImageLocator.class).get(iconPath));
        return bankLabel;
      }

      String shortName = accountsGroup.bank.get(Bank.SHORT_NAME);
      if (Strings.isNotEmpty(shortName)) {
        bankLabel.setText(shortName);
        return bankLabel;
      }

      String name = accountsGroup.bank.get(Bank.NAME);
      bankLabel.setText(Strings.cut(name, 30));
      return bankLabel;
    }
  }
}
