package org.designup.picsou.gui.importer.utils;

import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenBankUrlAction extends AbstractAction implements GlobSelectionListener {

  private String lastUrl;
  private BrowsingService browsingService;

  public OpenBankUrlAction(Directory directory) {
    super(Lang.get("import.fileSelection.openBankUrl"));
    this.browsingService = directory.get(BrowsingService.class);
    setEnabled(false);
    directory.get(SelectionService.class).addListener(this, Bank.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList banks = selection.getAll(Bank.TYPE);
    if (banks.size() == 1) {
      lastUrl = banks.getFirst().get(Bank.DOWNLOAD_URL);
    }
    else {
      lastUrl = null;
    }
    setEnabled(Strings.isNotEmpty(lastUrl));
  }

  public void actionPerformed(ActionEvent e) {
    browsingService.launchBrowser(lastUrl);
  }
}
