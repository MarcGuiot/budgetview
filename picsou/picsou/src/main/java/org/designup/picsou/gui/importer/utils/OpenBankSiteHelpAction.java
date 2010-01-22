package org.designup.picsou.gui.importer.utils;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.model.Bank;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenBankSiteHelpAction extends AbstractAction implements GlobSelectionListener {
  private HelpService helpService;
  private Glob lastBank;
  private PicsouDialog owner;

  public OpenBankSiteHelpAction(Directory directory, PicsouDialog owner) {
    super(Lang.get("import.step1.openBankSiteHelp"));
    this.owner = owner;
    setEnabled(false);
    helpService = directory.get(HelpService.class);
    directory.get(SelectionService.class).addListener(this, Bank.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList banks = selection.getAll(Bank.TYPE);
    if (banks.size() == 1) {
      lastBank = banks.getFirst();
    }
    else {
      lastBank = null;
    }
    setEnabled((lastBank != null) && helpService.hasBankHelp(lastBank));
  }

  public void actionPerformed(ActionEvent e) {
    helpService.showBankHelp(lastBank, owner);
  }
}
