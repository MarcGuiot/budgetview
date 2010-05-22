package org.designup.picsou.gui.importer.additionalactions;

import org.designup.picsou.gui.accounts.utils.Day;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AccountTypeChooserDialog {
  private Window parent;
  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;

  public AccountTypeChooserDialog(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
  }

  public void show() {

    final LocalGlobRepository localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE, AccountType.TYPE)
      .get();


    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountTypeChooserDialog.splits",
                                                      localRepository, directory);

    GlobList accounts = localRepository.getAll(Account.TYPE, GlobMatchers.isNull(Account.ACCOUNT_TYPE));
    builder.addRepeat("accoutType", accounts, new RepeatComponentFactory<Glob>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, Glob item) {
        DescriptionService descriptionService = directory.get(DescriptionService.class);
        String accountName = descriptionService.getStringifier(Account.TYPE).toString(item, localRepository);
        cellBuilder.add("accountName", new JLabel(accountName));
        final GlobLinkComboEditor linkComboEditor = GlobLinkComboEditor.init(Account.ACCOUNT_TYPE, localRepository, directory);
        JComboBox jComboBox = linkComboEditor
          .setShowEmptyOption(false)
          .forceSelection(item.getKey())
          .setName("Combo : " + accountName)
          .getComponent();
        cellBuilder.add("accountType", jComboBox);
        cellBuilder.addDisposeListener(new Disposable() {
          public void dispose() {
            linkComboEditor.dispose();
          }
        });
      }
    });

    dialog = PicsouDialog.createWithButtons(parent, directory,
                                            builder.<JPanel>load(),
                                            new ValidateAction(localRepository),
                                            new CancelAction());
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
  }

  private class ValidateAction extends AbstractAction {
    private LocalGlobRepository repository;

    public ValidateAction(LocalGlobRepository repository) {
      super(Lang.get("ok"));
      this.repository = repository;
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
      repository.commitChanges(true);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }

}
