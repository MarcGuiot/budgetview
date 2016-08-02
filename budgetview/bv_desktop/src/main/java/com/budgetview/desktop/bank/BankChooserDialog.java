package com.budgetview.desktop.bank;

import com.budgetview.desktop.components.dialogs.CancelAction;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.model.Account;
import com.budgetview.model.Bank;
import com.budgetview.model.BankEntity;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class BankChooserDialog implements GlobSelectionListener {
  private Window parent;
  private Directory directory;
  private GlobMatcher matcher;
  private PicsouDialog dialog;
  private Glob selectedBank;
  private BankChooserDialog.ValidateAction validateAction;
  private SelectionService selectionService;
  private LocalGlobRepository localRepository;

  public BankChooserDialog(Window parent, GlobRepository repository, Directory directory) {
    this(parent, repository, directory, null);
  }

  public BankChooserDialog(Window parent, GlobRepository repository, Directory directory, GlobMatcher matcher) {
    this.parent = parent;

    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Bank.TYPE, BankEntity.TYPE, Account.TYPE)
      .get();

    this.directory = directory;
    this.matcher = matcher;
  }

  public Integer show(Integer currentBankId, Set<Integer> excludedAccountIds) {

    Directory localDirectory = new DefaultDirectory(directory);
    selectionService = new SelectionService();
    localDirectory.add(SelectionService.class, selectionService);
    selectionService.addListener(this, Bank.TYPE);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/bank/bankChooserDialog.splits",
                                                      localRepository, localDirectory);

    validateAction = new ValidateAction();

    dialog = PicsouDialog.create(this, parent, localDirectory);

    BankChooserPanel bankChooser =
      new BankChooserPanel(localRepository, localDirectory, validateAction, matcher, dialog);
    bankChooser.setExcludedAccounts(excludedAccountIds);
    builder.add("bankChooserPanel", bankChooser.getPanel());

    dialog.addPanelWithButtons(builder.<JPanel>load(), validateAction, new CancelAction(dialog));
    
    dialog.pack();
    bankChooser.requestFocus();

    if (currentBankId != null) {
      selectionService.select(localRepository.find(Key.create(Bank.TYPE, currentBankId)));
    }

    dialog.showCentered();

    bankChooser.dispose();
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
      localRepository.commitChanges(true);
      dialog.setVisible(false);
      selectedBank = selectionService.getSelection(Bank.TYPE).getFirst();
    }
  }
}
