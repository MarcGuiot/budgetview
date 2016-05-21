package com.budgetview.gui.bank.actions;

import com.budgetview.gui.bank.BankEditionDialog;
import com.budgetview.model.Bank;
import com.budgetview.utils.Lang;
import org.globsframework.gui.actions.SingleSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

public class EditBankAction extends SingleSelectionAction {

  private Window owner;

  public EditBankAction(Window owner, GlobRepository repository, Directory directory) {
    super(Lang.get("bank.edit.action"),
          Bank.TYPE,
          GlobMatchers.isTrue(Bank.USER_CREATED),
          repository, directory);
    this.owner = owner;
  }

  protected void process(Glob bank, GlobRepository repository, Directory directory) {
    BankEditionDialog dialog = new BankEditionDialog(owner, repository, directory);
    dialog.show(bank);
  }
}
