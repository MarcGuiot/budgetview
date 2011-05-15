package org.designup.picsou.gui.bank;

import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.model.Bank;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GotoBankWebsiteAction extends AbstractAction implements GlobSelectionListener {

  private SelectionService selectionService;
  private BrowsingService browsingService;

  public GotoBankWebsiteAction(String text, GlobRepository repository, Directory directory) {
    super(text);
    browsingService = directory.get(BrowsingService.class);
    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(this, Bank.TYPE);
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  public void actionPerformed(ActionEvent actionEvent) {
    browsingService.launchBrowser(getSelectedBankUrl());
  }

  private void update() {
    setEnabled(Strings.isNotEmpty(getSelectedBankUrl()));
  }

  public String getSelectedBankUrl() {
    GlobList banks = selectionService.getSelection(Bank.TYPE);
    if (banks.size() != 1) {
      return null;
    }

    Glob bank = banks.getFirst();
    return bank.get(Bank.DOWNLOAD_URL);
  }
}
