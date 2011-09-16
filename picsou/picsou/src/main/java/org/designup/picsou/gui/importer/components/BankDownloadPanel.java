package org.designup.picsou.gui.importer.components;

import org.designup.picsou.gui.bank.BankChooserPanel;
import org.designup.picsou.gui.bank.BankChooserDialog;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.importer.ImportController;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.bank.BankSynchroService;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Strings;
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
  private GotoBankWebsiteAction gotoWebsiteAction;
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

    gotoWebsiteAction = new GotoBankWebsiteAction();
    builder.add("gotoWebsite", gotoWebsiteAction);

    openHelpAction = new OpenHelpAction();
    builder.add("openHelp", openHelpAction);

    bankChooser = BankChooserPanel.registerComponents(builder, gotoWebsiteAction, null);

    final HyperlinkHandler hyperlinkHandler = new HyperlinkHandler(directory, parent);

    hyperlinkHandler.registerLinkAction("synchronize", new Runnable() {
      public void run() {
        BankSynchroService bankSynchroService = directory.get(BankSynchroService.class);
        GlobList realAccount = bankSynchroService.show(bankId, directory, repository);
        if (!realAccount.isEmpty()){
          synchronize(realAccount);
        }
      }
    });

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
    gotoWebsiteAction.setBank(bank);
    openHelpAction.setBank(bank);
    cards.show(bank == null ? "noSelection" : "gotoSite");
    synchroPanel.setVisible(bank != null && (bank.get(Bank.SYNCHRO_ENABLE, false) || bank.get(Bank.OFX_DOWNLOAD, false)));
  }

  public void requestFocus() {
    bankChooser.requestFocus();
  }

  public class GotoBankWebsiteAction extends AbstractAction {

    private String lastUrl;

    public GotoBankWebsiteAction() {
      super(Lang.get("bankDownload.gotoWebsite.label"));
    }

    public void setBank(Glob bank) {
      setEnabled(bank != null);
      lastUrl = (bank != null) ? bank.get(Bank.URL) : null;
    }

    public void actionPerformed(ActionEvent actionEvent) {
      BrowsingService browsingService = directory.get(BrowsingService.class);
      String url = Strings.isNotEmpty(lastUrl) ? lastUrl : Lang.get("bankDownload.gotoWebsite.default.url");
      browsingService.launchBrowser(url);
    }
  }

  public class OpenHelpAction extends AbstractAction {
    private Glob lastBank;

    public OpenHelpAction() {
      super(Lang.get("bankDownload.guide.button"));
    }

    public void setBank(Glob bank) {
      this.lastBank = bank;
    }

    public void actionPerformed(ActionEvent e) {
      HelpService helpService = directory.get(HelpService.class);
      if ((lastBank != null) && helpService.hasBankHelp(lastBank)) {
        helpService.showBankHelp(lastBank, parent);
      }
      else {
        helpService.show("import", parent);
      }
    }
  }

  public void synchronize(GlobList realAccount) {
    for (Glob glob : realAccount) {
      String file = glob.get(RealAccount.FILE_NAME);
      if (Strings.isNullOrEmpty(file)){
        controller.addRealAccountWithoutImport(glob);
      }
      else {
        controller.addRealAccountWithImport(glob);
      }
    }
    controller.doImport();
  }


}
