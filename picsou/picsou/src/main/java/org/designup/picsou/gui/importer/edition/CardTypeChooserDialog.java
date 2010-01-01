package org.designup.picsou.gui.importer.edition;

import com.jidesoft.swing.AutoResizingTextArea;
import org.designup.picsou.gui.accounts.utils.Day;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.DefaultChangeSetListener;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CardTypeChooserDialog {
  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;

  public CardTypeChooserDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void show(Window parent, final GlobList accounts) {
    final LocalGlobRepository localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE, AccountCardType.TYPE, Bank.TYPE, BankEntity.TYPE, Day.TYPE)
      .get();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/cardTypeChooserDialog.splits",
                                                      localRepository, directory);

    builder.addRepeat("cardTypeRepeat", accounts, new RepeatFactory(localRepository));

    dialog = PicsouDialog.createWithButtons(parent, directory,
                                            builder.<JPanel>load(),
                                            new ValidateAction(localRepository),
                                            new CancelAction());
    dialog.pack();
    dialog.showCentered();
  }

  private String getAccountName(Glob account, LocalGlobRepository localRepository) {
    GlobStringifier globStringifier = directory.get(DescriptionService.class).getStringifier(Account.TYPE);
    return globStringifier.toString(account, localRepository);
  }

  private class ValidateAction extends AbstractAction {
    private LocalGlobRepository repository;

    public ValidateAction(LocalGlobRepository repository) {
      super(Lang.get("ok"));
      this.repository = repository;
      this.repository.addChangeListener(new DefaultChangeSetListener() {
        public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
          updateState();
        }
      });
      updateState();
    }

    private void updateState() {
      setEnabled(!repository.contains(Account.TYPE,
                                      fieldIn(Account.CARD_TYPE, AccountCardType.UNDEFINED.getId(), null)));
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

  private class RepeatFactory implements RepeatComponentFactory<Glob> {
    private final LocalGlobRepository localRepository;

    public RepeatFactory(LocalGlobRepository localRepository) {
      this.localRepository = localRepository;
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob account) {
      String accountName = getAccountName(account, localRepository);
      cellBuilder.add("accountName", new JLabel(accountName));

      final JPanel accountPanel = new JPanel();
      accountPanel.setName("accountPanel:" + accountName);
      cellBuilder.add("accountPanel", accountPanel);

      final GlobLinkComboEditor dayCombo =
        GlobLinkComboEditor.init(DeferredCardPeriod.DAY, localRepository, directory);
      cellBuilder.add("dayCombo", dayCombo.getComponent());
      dayCombo.setVisible(false);

      final JTextArea creditMessage = new AutoResizingTextArea(Lang.get("cardTypeChooser.credit.message"));
      cellBuilder.add("creditMessage", creditMessage);
      creditMessage.setVisible(false);

      final Key accountKey = account.getKey();
      GlobLinkComboEditor comboEditor =
        GlobLinkComboEditor.init(Account.CARD_TYPE, localRepository, directory)
          .forceSelection(accountKey)
          .setName("cardType:" + accountName)
          .setEmptyOptionLabel(Lang.get("cardTypeChooser.selectType"))
          .setFilter(fieldIn(AccountCardType.ID,
                             AccountCardType.CREDIT.getId(),
                             AccountCardType.DEFERRED.getId()));
      cellBuilder.add("cardTypeCombo", comboEditor.getComponent());

      final DefaultChangeSetListener accountUpdateListener = new DefaultChangeSetListener() {
        public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
          if (!changeSet.containsChanges(accountKey)) {
            return;
          }
          Glob account = repository.get(accountKey);
          AccountCardType cardType = AccountCardType.get(account.get(Account.CARD_TYPE));
          switch (cardType) {
            case CREDIT:
              localRepository.delete(DeferredCardPeriod.TYPE,
                                     and(fieldEquals(DeferredCardPeriod.ACCOUNT, account.get(Account.ID)),
                                         fieldEquals(DeferredCardPeriod.FROM_MONTH, 0)));
              break;

            case DEFERRED:
              Glob period = localRepository.create(DeferredCardPeriod.TYPE,
                                                   value(DeferredCardPeriod.ACCOUNT, account.get(Account.ID)),
                                                   value(DeferredCardPeriod.FROM_MONTH, 0));
              dayCombo.forceSelection(period.getKey());
              break;
          }
          creditMessage.setVisible(AccountCardType.CREDIT.equals(cardType));
          dayCombo.setVisible(AccountCardType.DEFERRED.equals(cardType));
          GuiUtils.revalidate(accountPanel);
        }
      };

      localRepository.addChangeListener(accountUpdateListener);

      cellBuilder.addDisposeListener(new Disposable() {
        public void dispose() {
          localRepository.removeChangeListener(accountUpdateListener);
        }
      });
    }
  }
}
