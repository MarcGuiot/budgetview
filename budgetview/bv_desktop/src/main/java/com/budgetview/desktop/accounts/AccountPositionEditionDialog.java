package com.budgetview.desktop.accounts;

import com.budgetview.desktop.components.dialogs.CancelAction;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.model.Account;
import com.budgetview.shared.utils.AmountFormat;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AccountPositionEditionDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private AccountPositionEditionPanel editionPanel;
  private GlobsPanelBuilder builder;


  public AccountPositionEditionDialog(Glob account, GlobRepository repository, Directory directory, Window parent) {
    this(account, null, repository, directory, parent);
  }

  public AccountPositionEditionDialog(Glob account, Double value,
                                      GlobRepository repository, Directory directory, Window parent) {

    this.localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Account.TYPE)
        .get();

    builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountPositionEditionDialog.splits",
                          localRepository, directory);

    ValidateAction validateAction = new ValidateAction();

    boolean accountInitialization = value != null;

    editionPanel = new AccountPositionEditionPanel(validateAction, localRepository, directory,
                                                   accountInitialization ? Account.LAST_IMPORT_POSITION : Account.PAST_POSITION);
    builder.add("editionPanel", editionPanel.getPanel());

    editionPanel.setAccount(account, repository);
    editionPanel.setInitialMessageVisible(accountInitialization);

    if (accountInitialization) {
      dialog = PicsouDialog.createWithButton(this, parent, builder.<JPanel>load(), validateAction, directory);
      dialog.disableEscShortcut();
      editionPanel.setText(AmountFormat.DECIMAL_FORMAT.format(value));
      dialog.setPreferredSize(new Dimension(400, 350));
    }
    else {
      dialog = PicsouDialog.create(this, parent, directory);
      dialog.addPanelWithButtons(builder.<JPanel>load(), validateAction, new CancelAction(dialog));
      dialog.setPreferredSize(new Dimension(400, 300));
    }

    dialog.setAutoFocusOnOpen(editionPanel.getEditor());
    dialog.registerDisposable(new Disposable() {
      public void dispose() {
        builder.dispose();
        editionPanel.dispose();
      }
    });
    dialog.pack();
  }

  public void show() {
    dialog.showCentered();
  }

  private class ValidateAction extends AbstractAction {
    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      editionPanel.apply();
      localRepository.commitChanges(true);
      dialog.setVisible(false);
    }
  }
}
