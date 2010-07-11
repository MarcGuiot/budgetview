package org.designup.picsou.gui.bank;

import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
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
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class BankGuideDialog {
  private Window parent;
  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;

  public BankGuideDialog(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
  }

  public void show() {
    LocalGlobRepository localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Bank.TYPE)
      .get();

    Directory localDirectory = new DefaultDirectory(directory);
    SelectionService selectionService = new SelectionService();
    localDirectory.add(SelectionService.class, selectionService);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/bank/bankGuideDialog.splits",
                                                      localRepository, localDirectory);

    OpenHelpAction openHelpAction = new OpenHelpAction(localDirectory);

    BankChooserPanel.registerComponents(builder, openHelpAction);
    builder.add("openHelp", openHelpAction);

    dialog = PicsouDialog.create(parent, localDirectory);
    dialog.addPanelWithButton(builder.<JPanel>load(), new CloseAction(dialog));

    dialog.pack();
    dialog.showCentered();
  }

  public class OpenHelpAction extends AbstractAction implements GlobSelectionListener {
    private HelpService helpService;
    private Glob lastBank;

    public OpenHelpAction(Directory localDirectory) {
      super(Lang.get("bank.guide.button"));
      setEnabled(false);
      helpService = localDirectory.get(HelpService.class);
      localDirectory.get(SelectionService.class).addListener(this, Bank.TYPE);
    }

    public void selectionUpdated(GlobSelection selection) {
      GlobList banks = selection.getAll(Bank.TYPE);
      if (banks.size() == 1) {
        lastBank = banks.getFirst();
      }
      else {
        lastBank = null;
      }
      setEnabled((lastBank != null));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);

      if (helpService.hasBankHelp(lastBank)) {
        helpService.showBankHelp(lastBank, parent);
      }
      else {
        helpService.show("import", parent);
      }
    }
  }
}