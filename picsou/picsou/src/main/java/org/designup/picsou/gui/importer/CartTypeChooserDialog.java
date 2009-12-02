package org.designup.picsou.gui.importer;

import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.model.Glob;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.model.AccountCardType;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CartTypeChooserDialog {
  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;

  public CartTypeChooserDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void show(Window parent, GlobList accounts) {
    final LocalGlobRepository localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE, Bank.TYPE, BankEntity.TYPE)
      .get();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/cardTypeChooserDialog.splits",
                                                      repository, directory);
    builder.addRepeat("cardTypeRepeat", accounts, new RepeatComponentFactory<Glob>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, final Glob item) {
        GlobStringifier globStringifier = directory.get(DescriptionService.class).getStringifier(Account.TYPE);
        String label = globStringifier.toString(item, localRepository);
        cellBuilder.add("accountName", new JLabel(label));

        JToggleButton creditButton = new JToggleButton(new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            repository.update(item.getKey(), Account.CARD_TYPE, AccountCardType.CREDIT.getId());
          }
        });
        creditButton.setName(label + ":credit");
        JToggleButton deferredButton = new JToggleButton(new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            repository.update(item.getKey(), Account.CARD_TYPE, AccountCardType.DEFERRED.getId());
          }
        });
        creditButton.setName(label + ":deferred");
        ButtonGroup group = new ButtonGroup();
        group.add(creditButton);
        group.add(deferredButton);

        cellBuilder.add("creditCard", creditButton);
        cellBuilder.add("deferredDebit", deferredButton);
      }
    });

    dialog = PicsouDialog.createWithButtons(parent, directory,
                                            builder.<JPanel>load(),
                                            new ValidateAction(localRepository),
                                            new CancelAction()
    );
    dialog.pack();
    dialog.showCentered();

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
