package org.designup.picsou.gui.bank;

import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
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

public class BankChooserDialog implements GlobSelectionListener {
  private Window parent;
  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;
  private Glob selectedBank;
  private BankChooserDialog.ValidateAction validateAction;
  private SelectionService selectionService;

  public BankChooserDialog(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
  }

  public Integer show() {
    LocalGlobRepository localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Bank.TYPE)
      .get();

    Directory localDirectory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
    localDirectory.add(SelectionService.class, selectionService);
    selectionService.addListener(this, Bank.TYPE);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/bank/bankChooserDialog.splits",
                                                      localRepository, localDirectory);

    validateAction = new ValidateAction();

    BankChooserPanel.registerComponents(builder, validateAction);


    dialog = PicsouDialog.create(parent, localDirectory);
    dialog.addPanelWithButtons(builder.<JPanel>load(), validateAction, new CancelAction(dialog));

    dialog.pack();
    dialog.showCentered(true);

    builder.dispose();

    if (selectedBank != null) {
      return selectedBank.get(Bank.ID);
    }
    return null;
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList globList = selection.getAll(Bank.TYPE);
    validateAction.setEnabled(globList.size() == 1);
  }

  private class ValidateAction extends AbstractAction {
    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
      selectedBank = selectionService.getSelection(Bank.TYPE).getFirst();
    }
  }
}
