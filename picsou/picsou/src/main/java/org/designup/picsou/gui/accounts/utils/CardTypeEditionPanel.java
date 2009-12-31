package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.components.dialogs.MonthChooserDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.jidesoft.swing.AutoResizingTextArea;

public class CardTypeEditionPanel {
  private CardHandler cardHandler;
  private int firstMonth;
  private int lastMonth;
  private Set<Integer> monthToIgnore = new HashSet<Integer>();
  private Window owner;
  private GlobRepository repository;
  private Directory directory;
  private Glob currentAccount;
  private GlobRepeat periods;

  public CardTypeEditionPanel(Window owner, GlobRepository repository, Directory directory) {
    this.owner = owner;
    this.repository = repository;
    this.directory = directory;
  }

  public GlobsPanelBuilder createComponent() {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/cardTypeEditionPanel.splits",
                                                      repository, directory);

    cardHandler = builder.addCardHandler("CardTypes");

    repository.addTrigger(new UpdatePeriodOnAccountDateChange());
    repository.addTrigger(new OnCardTypeChangeListener());

    periods = builder.addRepeat("deferredPeriodRepeat", DeferredCardPeriod.TYPE, GlobMatchers.NONE,
                                new GlobFieldComparator(DeferredCardPeriod.FROM_MONTH),
                                new DeferredPeriodRepeatComponentFactory());

    builder.add("createNewPeriod", new CreateNewPeriodAction());

    final JTextArea creditMessage = new AutoResizingTextArea(Lang.get("cardTypeChooser.credit.message"));
    builder.add("creditMessage", creditMessage);

    return builder;
  }

  private String getLabel(Integer monthId) {
    String labelForMonth;
    if (monthId == 0) {
      labelForMonth = Lang.get("account.deferred.repeat.label.first");
    }
    else {
      String month = Month.getFullMonthLabel(monthId);
      if (Lang.find("account.deferred.repeat.label." + Month.toMonth(monthId)) != null) {
        labelForMonth = Lang.get("account.deferred.repeat.label." + Month.toMonth(monthId), Integer.toString(Month.toYear(monthId)));
      }
      else {
        labelForMonth = Lang.get("account.deferred.repeat.label", month, Integer.toString(Month.toYear(monthId)));
      }
    }
    return labelForMonth;
  }

  public void setAccount(Glob account) {
    currentAccount = account;
    monthToIgnore.clear();
    if (account != null) {
      updateMonthsFromAccount();
      periods.setFilter(fieldEquals(DeferredCardPeriod.ACCOUNT, account.get(Account.ID)));
    }
    Integer cardTypeId = (account == null) ? AccountCardType.NOT_A_CARD.getId() : account.get(Account.CARD_TYPE);
    AccountCardType cardType = AccountCardType.get(cardTypeId);
    cardHandler.show(cardType.getName());
  }

  private boolean isDisplayed(AccountCardType cardType) {
    return AccountCardType.CREDIT.equals(cardType) || AccountCardType.DEFERRED.equals(cardType);
  }

  private void updateMonthsFromAccount() {
    ExistingMonthsCollector existingMonthsCollector = new ExistingMonthsCollector();
    repository.safeApply(Month.TYPE, GlobMatchers.ALL, existingMonthsCollector);

    firstMonth = existingMonthsCollector.firstMonth;
    lastMonth = existingMonthsCollector.lastMonth;

    Date openDate = currentAccount.get(Account.OPEN_DATE);
    if (openDate != null) {
      firstMonth = Math.max(Month.getMonthId(openDate), firstMonth);
    }

    Date closeDate = currentAccount.get(Account.CLOSED_DATE);
    if (closeDate != null) {
      lastMonth = Math.min(Month.getMonthId(closeDate), lastMonth);
    }
  }

  private class DeferredDateChooserAction extends AbstractAction {
    private Key key;
    private Integer monthId;

    public DeferredDateChooserAction(Key key, Integer monthId) {
      this.key = key;
      this.monthId = monthId;
    }

    public void actionPerformed(ActionEvent e) {
      MonthChooserDialog monthChooser = new MonthChooserDialog(owner, directory);
      int newMonth = monthChooser.show(monthId, firstMonth, lastMonth, monthToIgnore);
      if (newMonth != -1) {
        repository.update(key, DeferredCardPeriod.FROM_MONTH, newMonth);
        monthToIgnore.add(newMonth);
      }
    }
  }

  private class RemovePeriod extends AbstractAction {
    private Glob deferredPeriod;

    public RemovePeriod(Glob deferredPeriod) {
      this.deferredPeriod = deferredPeriod;
    }

    public void actionPerformed(ActionEvent e) {
      monthToIgnore.remove(deferredPeriod.get(DeferredCardPeriod.FROM_MONTH));
      repository.delete(deferredPeriod.getKey());
    }
  }

  private static class ExistingMonthsCollector implements GlobFunctor {
    public int firstMonth = Integer.MAX_VALUE;
    public int lastMonth = Integer.MIN_VALUE;

    public void run(Glob glob, GlobRepository repository) throws Exception {
      Integer currentMonthId = glob.get(Month.ID);
      if (currentMonthId < firstMonth) {
        firstMonth = currentMonthId;
      }
      if (currentMonthId > lastMonth) {
        lastMonth = currentMonthId;
      }
    }
  }

  private class UpdatePeriodOnAccountDateChange implements ChangeSetListener {
    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (currentAccount == null) {
        return;
      }
      if (changeSet.containsChanges(currentAccount.getKey(), Account.OPEN_DATE, Account.CLOSED_DATE)) {
        if (!currentAccount.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
          return;
        }
        updateMonthsFromAccount();
        if (currentAccount.get(Account.OPEN_DATE) != null) {
          int openMonthId = firstMonth;
          GlobList deferredPeriods =
            repository.getAll(DeferredCardPeriod.TYPE,
                              fieldEquals(DeferredCardPeriod.ACCOUNT, currentAccount.get(Account.ID)))
              .sort(DeferredCardPeriod.FROM_MONTH);
          int lastDay = 0;
          for (Glob period : deferredPeriods) {
            if (period.get(DeferredCardPeriod.FROM_MONTH) != 0 &&
                period.get(DeferredCardPeriod.FROM_MONTH) < openMonthId) {
              lastDay = period.get(DeferredCardPeriod.DAY);
              repository.delete(period.getKey());
            }
          }
          if (lastDay != 0) {
            repository.update(deferredPeriods.getFirst().getKey(), DeferredCardPeriod.DAY, lastDay);
          }
        }
        if (currentAccount.get(Account.CLOSED_DATE) != null) {
          int closedMonthId = lastMonth;
          GlobList deferredPeriods =
            repository.getAll(DeferredCardPeriod.TYPE,
                              fieldEquals(DeferredCardPeriod.ACCOUNT, currentAccount.get(Account.ID)))
              .sort(DeferredCardPeriod.FROM_MONTH);

          while (deferredPeriods.getLast().get(DeferredCardPeriod.FROM_MONTH) > closedMonthId) {
            repository.delete(deferredPeriods.getLast().getKey());
            deferredPeriods.remove(deferredPeriods.getLast());
          }
        }
      }
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    }
  }

  private class OnCardTypeChangeListener extends DefaultChangeSetListener {
    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      if (currentAccount != null && changeSet.containsChanges(currentAccount.getKey(), Account.CARD_TYPE)) {
        Glob cardType = repository.findLinkTarget(currentAccount, Account.CARD_TYPE);
        cardHandler.show(cardType.get(AccountCardType.NAME));

        if (cardType.get(AccountCardType.ID).equals(AccountCardType.DEFERRED.getId())) {
          repository.create(DeferredCardPeriod.TYPE, value(DeferredCardPeriod.DAY, 31),
                            value(DeferredCardPeriod.ACCOUNT, currentAccount.get(Account.ID)),
                            value(DeferredCardPeriod.FROM_MONTH, 0));
        }
        else {
          repository.delete(DeferredCardPeriod.TYPE,
                            fieldEquals(DeferredCardPeriod.ACCOUNT,
                                        currentAccount.get(Account.ID)));
        }
      }
    }
  }

  private class DeferredPeriodRepeatComponentFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob item) {
      Integer monthId = item.get(DeferredCardPeriod.FROM_MONTH);
      String labelForMonth = getLabel(monthId);
      final JLabel swingLabel = new JLabel(labelForMonth);
      cellBuilder.add("deferredPeriodLabel", swingLabel);

      JButton deferredDateChooserButton =
        cellBuilder.add("changeDeferredMonthAction",
                        new JButton(new DeferredDateChooserAction(item.getKey(), monthId)))
          .getComponent();
      deferredDateChooserButton.setEnabled(monthId != 0);
      GlobLinkComboEditor dayCombo =
        GlobLinkComboEditor.init(DeferredCardPeriod.DAY, repository, directory)
          .setComparator(new GlobFieldComparator(Day.ID))
          .forceSelection(item.getKey());
      cellBuilder.add("dayChooser", dayCombo.getComponent());
      JButton removePeriod = new JButton(new RemovePeriod(item));
      cellBuilder.add("removePeriod", removePeriod);
      removePeriod.setEnabled(monthId != 0);
      if (monthId == 0) {
        monthToIgnore.add(firstMonth);
      }
      else {
        monthToIgnore.add(monthId);
      }
      final DefaultChangeSetListener listener = new DefaultChangeSetListener() {
        Key key = item.getKey();

        public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
          Glob item = repository.find(key);
          if (item == null) {
            return;
          }
          if (changeSet.containsChanges(key, DeferredCardPeriod.FROM_MONTH)) {
            Integer monthId = item.get(DeferredCardPeriod.FROM_MONTH);
            String s = getLabel(monthId);
            swingLabel.setText(s);
          }
        }
      };
      repository.addChangeListener(listener);
      cellBuilder.addDisposeListener(new Disposable() {
        public void dispose() {
          repository.removeChangeListener(listener);
        }
      });
    }
  }

  private class CreateNewPeriodAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);
      Glob currentMonth = repository.get(CurrentMonth.KEY);
      Glob month = repository.findLinkTarget(currentMonth, CurrentMonth.CURRENT_MONTH);
      while (true) {
        Glob glob =
          repository.getAll(DeferredCardPeriod.TYPE,
                            and(fieldEquals(DeferredCardPeriod.ACCOUNT, currentAccount.get(Account.ID)),
                                fieldEquals(DeferredCardPeriod.FROM_MONTH, month.get(Month.ID))))
            .getFirst();
        if (glob == null && month.get(Month.ID) != firstMonth) {
          repository.create(DeferredCardPeriod.TYPE,
                            value(DeferredCardPeriod.ACCOUNT, currentAccount.get(Account.ID)),
                            value(DeferredCardPeriod.FROM_MONTH, month.get(Month.ID)),
                            value(DeferredCardPeriod.DAY, 31));
          monthToIgnore.add(month.get(Month.ID));
          return;
        }
        months.remove(month);
        if (months.isEmpty()) {
          return;
        }
        Integer newMonthId = Month.next(month.get(Month.ID));
        month = repository.find(Key.create(Month.TYPE, newMonthId));
        if (month == null) {
          month = months.getFirst();
        }
      }
    }
  }
}
