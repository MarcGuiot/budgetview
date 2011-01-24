package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;

public class AccountPositionEditionDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private Date balanceDate;
  private AccountPositionEditionPanel editionPanel;
  private GlobsPanelBuilder builder;

  public AccountPositionEditionDialog(Glob account, boolean accountInitialization,
                                      GlobRepository repository, Directory directory, Window parent) {

    this.localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Account.TYPE)
        .get();

    builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountPositionEditionDialog.splits",
                                                      localRepository, directory);

    ValidateAction validateAction = new ValidateAction();

    editionPanel = new AccountPositionEditionPanel(validateAction,
                                                   localRepository, directory);
    builder.add("editionPanel", editionPanel.getPanel());

    editionPanel.setAccount(account, repository);
    editionPanel.setInitialMessageVisible(accountInitialization);

    if (accountInitialization) {
      dialog = PicsouDialog.createWithButton(parent, builder.<JPanel>load(), validateAction, directory);
      dialog.disableEscShortcut();
      editionPanel.setText("0.0");
      dialog.setPreferredSize(new Dimension(400, 350));
    }
    else {
      dialog = PicsouDialog.create(parent, directory);
      dialog.addPanelWithButtons(builder.<JPanel>load(), validateAction, new CancelAction(dialog));
      dialog.setPreferredSize(new Dimension(400, 300));
    }

    dialog.setAutoFocusOnOpen(editionPanel.getEditor());

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
