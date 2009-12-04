package org.designup.picsou.gui.importer;

import org.designup.picsou.gui.accounts.Day;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class CartTypeChooserDialog {
  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;
  private JLabel creditMessage;

  public CartTypeChooserDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void show(Window parent, final GlobList accounts) {
    final LocalGlobRepository localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE, Bank.TYPE, BankEntity.TYPE, Day.TYPE)
      .get();

    localRepository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(Account.TYPE)) {
          boolean messageVisible = false;
          for (Glob account : accounts) {
            if (repository.get(account.getKey()).get(Account.CARD_TYPE).equals(AccountCardType.CREDIT.getId())) {
              messageVisible = true;
              break;
            }
          }
          creditMessage.setVisible(messageVisible);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    });
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/cardTypeChooserDialog.splits",
                                                      localRepository, directory);
    builder.addRepeat("cardTypeRepeat", accounts, new RepeatComponentFactory<Glob>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, final Glob item) {
        GlobStringifier globStringifier = directory.get(DescriptionService.class).getStringifier(Account.TYPE);
        String label = globStringifier.toString(item, localRepository);
        cellBuilder.add("accountName", new JLabel(label));
        final GlobLinkComboEditor dayCombo = new GlobLinkComboEditor(DeferredCardPeriod.DAY, localRepository, directory);
        dayCombo.setVisible(false);

        JRadioButton creditButton = new JRadioButton(new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            localRepository.update(item.getKey(), Account.CARD_TYPE, AccountCardType.CREDIT.getId());
            localRepository.delete(
              localRepository.getAll(DeferredCardPeriod.TYPE,
                                     GlobMatchers.and(GlobMatchers.fieldEquals(DeferredCardPeriod.ACCOUNT, item.get(Account.ID)),
                                                      GlobMatchers.fieldEquals(DeferredCardPeriod.FROM_MONTH, 0))));
            dayCombo.setVisible(false);
          }
        });
        creditButton.setName("credit : " + label);

        JRadioButton deferredButton = new JRadioButton(new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            localRepository.update(item.getKey(), Account.CARD_TYPE, AccountCardType.DEFERRED.getId());
            Glob glob = localRepository.create(DeferredCardPeriod.TYPE,
                                               FieldValue.value(DeferredCardPeriod.ACCOUNT, item.get(Account.ID)),
                                               FieldValue.value(DeferredCardPeriod.FROM_MONTH, 0));
            directory.get(SelectionService.class).select(glob);
            dayCombo.setVisible(true);
          }
        });
        deferredButton.setName("deferred :" + label);

        ButtonGroup group = new ButtonGroup();
        group.add(creditButton);
        group.add(deferredButton);

        cellBuilder.add("comboOfDay", dayCombo.getComponent());
        cellBuilder.add("creditCard", creditButton);
        cellBuilder.add("deferredDebit", deferredButton);
      }
    });

    creditMessage = new JLabel(Lang.get("cardTypeChooser.credit.message"));
    creditMessage.setVisible(false);
    builder.add("creditMessage", creditMessage);
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
