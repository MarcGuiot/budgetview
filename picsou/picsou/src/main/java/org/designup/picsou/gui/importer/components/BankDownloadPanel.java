package org.designup.picsou.gui.importer.components;

import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.gui.bank.BankChooserPanel;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.help.HelpDialog;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.importer.ImportController;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

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

    synchroPanel = new JPanel();
    builder.add("synchroPanel", synchroPanel);

    builder.add("synchronize", new AbstractAction(Lang.get("synchro.open")) {
      public void actionPerformed(ActionEvent actionEvent) {
        BankSynchroService bankSynchroService = directory.get(BankSynchroService.class);
        GlobList realAccount = bankSynchroService.show(parent, bankId, directory, repository);
        if (!realAccount.isEmpty()) {
          synchronize(realAccount);
        }
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
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList banks = selection.getAll(Bank.TYPE);
    Glob bank = banks.size() == 1 ? banks.getFirst() : null;
    update(bank);
  }

  private void update(Glob bank) {
    bankId = bank != null ? bank.get(Bank.ID) : null;
    selectionCards.show(bank == null ? "noSelection" : "gotoSite");

    boolean synchro = bank != null && (bank.get(Bank.SYNCHRO_ENABLED, false) && BankSynchroService.SHOW_SYNCHRO);
    Utils.beginRemove();
    if (bank != null && bank.get(Bank.ID).equals(Bank.GENERIC_BANK_ID)) {
      synchro = true;
    }
    Utils.endRemove();
    synchroPanel.setVisible(bank != null && (synchro || bank.get(Bank.OFX_DOWNLOAD, false)));

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

  public void synchronize(GlobList realAccount) {
    for (Glob glob : realAccount) {
      String file = glob.get(RealAccount.FILE_NAME);
      if (Strings.isNullOrEmpty(file)) {
        controller.addRealAccountWithoutImport(glob);
      }
      else {
        controller.addRealAccountWithImport(glob);
      }
    }
    controller.doImport();
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
}
