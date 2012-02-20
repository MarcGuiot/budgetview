package org.designup.picsou.gui.importer.components;

import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.gui.bank.BankChooserPanel;
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
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class BankDownloadPanel implements GlobSelectionListener {
  private Window parent;
  private ImportController controller;
  private GlobRepository repository;
  private Directory directory;
  private JPanel panel;
  private OpenHelpAction openHelpAction;
  private BankChooserPanel bankChooser;
  private CardHandler cards;
  private JPanel synchroPanel;
  private Integer bankId;

  public BankDownloadPanel(Window parent, ImportController controller, GlobRepository repository, Directory directory) {
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

    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(),
                            "/layout/importexport/components/bankDownloadPanel.splits",
                            repository, directory);

    cards = builder.addCardHandler("cards");

    synchroPanel = new JPanel();
    builder.add("synchroPanel", synchroPanel);

    builder.add("synchronize", new AbstractAction(Lang.get("synchro.open")) {
      public void actionPerformed(ActionEvent actionEvent) {
        BankSynchroService bankSynchroService = directory.get(BankSynchroService.class);
        GlobList realAccount = bankSynchroService.show(bankId, directory, repository);
        if (!realAccount.isEmpty()) {
          synchronize(realAccount);
        }
      }
    });

    builder.add("securityInfo", OfxSecurityInfoButton.create(directory));

    openHelpAction = new OpenHelpAction();
    builder.add("openHelp", openHelpAction);

    bankChooser = BankChooserPanel.registerComponents(builder, openHelpAction, null);

    final HyperlinkHandler hyperlinkHandler = new HyperlinkHandler(directory, parent);
    builder.add("hyperlinkHandler", hyperlinkHandler);

    panel = builder.load();

    SelectionService selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, Bank.TYPE);
    selectionService.clear(Bank.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList banks = selection.getAll(Bank.TYPE);
    Glob bank = banks.size() == 1 ? banks.getFirst() : null;
    update(bank);
  }

  private void update(Glob bank) {
    bankId = bank != null ? bank.get(Bank.ID) : null;
    openHelpAction.setBank(bank);
    cards.show(bank == null ? "noSelection" : "gotoSite");
    boolean synchro = bank != null && (bank.get(Bank.SYNCHRO_ENABLE, false) && BankSynchroService.SHOW_SYNCHRO);
    Utils.beginRemove();
    if (bank != null && bank.get(Bank.ID).equals(Bank.GENERIC_BANK_ID)){
      synchro = true;
    }
    Utils.endRemove();
    synchroPanel.setVisible(bank != null && (synchro || bank.get(Bank.OFX_DOWNLOAD, false)));
  }

  public void requestFocus() {
    bankChooser.requestFocus();
  }

  public class OpenHelpAction extends AbstractAction {
    private Glob lastBank;
    private HelpService helpService;

    public OpenHelpAction() {
      helpService = directory.get(HelpService.class);
      setBank(null);
    }

    public void setBank(Glob bank) {
      this.lastBank = bank;
      putValue(Action.NAME, getLabel());
    }

    public void actionPerformed(ActionEvent e) {
      if (hasSpecificHelp()) {
        helpService.showBankHelp(lastBank, parent);
      }
      else {
        helpService.show("manualDownload", parent);
      }
    }

    private Object getLabel() {
      if (hasSpecificHelp()) {
        return Lang.get("bankDownload.guide.button.specific", lastBank.get(Bank.NAME));
      }
      else {
        return Lang.get("bankDownload.guide.button");
      }
    }

    private boolean hasSpecificHelp() {
      return (lastBank != null) && helpService.hasBankHelp(lastBank);
    }
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


}
