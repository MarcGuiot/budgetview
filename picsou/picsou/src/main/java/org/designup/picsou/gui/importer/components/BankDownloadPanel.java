package org.designup.picsou.gui.importer.components;

import org.designup.picsou.gui.bank.BankChooserPanel;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class BankDownloadPanel implements GlobSelectionListener {
  private Window parent;
  private GlobRepository repository;
  private Directory directory;
  private JPanel panel;
  private GotoBankWebsiteAction gotoWebsiteAction;
  private OpenHelpAction openHelpAction;
  private BankChooserPanel bankChooser;

  public BankDownloadPanel(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
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

    gotoWebsiteAction = new GotoBankWebsiteAction();
    builder.add("gotoWebsite", gotoWebsiteAction);

    openHelpAction = new OpenHelpAction();
    builder.add("openHelp", openHelpAction);

    bankChooser = BankChooserPanel.registerComponents(builder, gotoWebsiteAction);

    panel = builder.load();

    SelectionService selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, Bank.TYPE);
    selectionService.select(repository.get(Bank.GENERIC_BANK_KEY));
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList banks = selection.getAll(Bank.TYPE);
    Glob bank = banks.size() == 1 ? banks.getFirst() : null;
    update(bank);
  }

  private void update(Glob bank) {
    gotoWebsiteAction.setBank(bank);
    openHelpAction.setBank(bank);
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
      lastUrl = (bank != null) ? bank.get(Bank.DOWNLOAD_URL)  : null;
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
}
