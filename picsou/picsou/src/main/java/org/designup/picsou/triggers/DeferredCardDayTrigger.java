package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

public class DeferredCardDayTrigger extends DefaultChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Month.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        GlobList deferredAccount = repository.getAll(Account.TYPE, GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
        for (Glob account : deferredAccount) {
          updateDeferredCarDayOnAccountChange(repository, account);
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList deferredAccount = repository.getAll(Account.TYPE, GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
        for (Glob account : deferredAccount) {
          updateDeferredCarDayOnAccountChange(repository, account);
        }
      }
    });

    changeSet.safeVisit(Account.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        updateDeferredCarDayOnAccountChange(repository, repository.find(key));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        updateDeferredCarDayOnAccountChange(repository, repository.find(key));
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        repository.delete(DeferredCardPeriod.TYPE,
                          GlobMatchers.fieldEquals(DeferredCardPeriod.ACCOUNT, key.get(Account.ID)));
        repository.delete(repository.findByIndex(DeferredCardDate.ACCOUNT_AND_DATE,
                                                 DeferredCardDate.ACCOUNT, key.get(Account.ID)).getGlobs());
      }
    });
    changeSet.safeVisit(DeferredCardPeriod.TYPE, new PeriodChangeSetVisitor(repository));
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Account.TYPE)) {
      GlobList deferredAccount = repository.getAll(Account.TYPE, GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
      for (Glob account : deferredAccount) {
        updateDeferredCarDayOnAccountChange(repository, account);
      }
    }
  }

  private class PeriodChangeSetVisitor implements ChangeSetVisitor {
    private GlobRepository repository;

    public PeriodChangeSetVisitor(GlobRepository repository) {
      this.repository = repository;
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      Integer accountId = values.get(DeferredCardPeriod.ACCOUNT);
      updateDeferredCarDayOnAccountChange(repository, repository.find(Key.create(Account.TYPE, accountId)));
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      Integer accountId = repository.find(key).get(DeferredCardPeriod.ACCOUNT);
      updateDeferredCarDayOnAccountChange(repository, repository.find(Key.create(Account.TYPE, accountId)));
    }

    public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      Integer accountId = previousValues.get(DeferredCardPeriod.ACCOUNT);
      Glob account = repository.find(Key.create(Account.TYPE, accountId));
      if (account != null) {
        updateDeferredCarDayOnAccountChange(repository, account);
      }
    }
  }

  private void updateDeferredCarDayOnAccountChange(GlobRepository repository, final Glob account) {

    Glob series = repository.getAll(Series.TYPE, GlobMatchers.and(GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                                                                  GlobMatchers.fieldEquals(Series.FROM_ACCOUNT, account.get(Account.ID))))
      .getFirst();
    Calendar calendar = Calendar.getInstance();
    Integer accountId = account.get(Account.ID);
    GlobList periods = repository.getAll(DeferredCardPeriod.TYPE,
                                         GlobMatchers.fieldEquals(DeferredCardPeriod.ACCOUNT, accountId))
      .sort(DeferredCardPeriod.FROM_MONTH);

    ReadOnlyGlobRepository.MultiFieldIndexed indexOnDeferredCardDay =
      repository.findByIndex(DeferredCardDate.ACCOUNT_AND_DATE, DeferredCardDate.ACCOUNT, accountId);

    GlobList deferredCardDays = indexOnDeferredCardDay.getGlobs();

    Iterator<Glob> iterator = periods.iterator();

    // on detruit tout les deferredCardDays si pas periode.
    if (!iterator.hasNext()) {
      repository.delete(deferredCardDays);
      return;
    }

    // on creer/detruit des DeferredCardDay suivant les changements sur les mois ou sur
    // l'overture/fermeture du compte
    GlobList months = repository.getAll(Month.TYPE).sort(Month.ID);

    int startMonth = months.getFirst().get(Month.ID);
    Date openDate = account.get(Account.OPEN_DATE);
    if (openDate != null) {
      startMonth = Math.max(Month.getMonthId(openDate), startMonth);
    }

    int endMonth = months.getLast().get(Month.ID);
    Date closedDate = account.get(Account.CLOSED_DATE);
    if (closedDate != null) {
      endMonth = Math.min(Month.getMonthId(closedDate), endMonth);
    }

    while (startMonth != endMonth) {
      GlobList globs = indexOnDeferredCardDay.findByIndex(DeferredCardDate.MONTH, startMonth).getGlobs();
      if (globs.isEmpty()) {
        repository.create(DeferredCardDate.TYPE,
                          value(DeferredCardDate.ACCOUNT, accountId),
                          value(DeferredCardDate.MONTH, startMonth),
                          value(DeferredCardDate.DAY, 31));
      }
      else {
        deferredCardDays.remove(globs.getFirst());
      }
      startMonth = Month.next(startMonth);
    }

    repository.delete(deferredCardDays);

    deferredCardDays = indexOnDeferredCardDay.getGlobs();

    // on met a jour le jour de chaque mois
    Glob currentPeriod = iterator.next();
    int day = currentPeriod.get(DeferredCardPeriod.DAY);
    currentPeriod = iterator.hasNext() ? iterator.next() : null;
    ReadOnlyGlobRepository.MultiFieldIndexed seriesIndex = null;
    if (series != null) {
      seriesIndex = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID));
    }
    for (Glob deferredCardDay : deferredCardDays) {
      if (currentPeriod != null &&
          deferredCardDay.get(DeferredCardDate.MONTH) >= currentPeriod.get(DeferredCardPeriod.FROM_MONTH)) {
        day = currentPeriod.get(DeferredCardPeriod.DAY);
        currentPeriod = iterator.hasNext() ? iterator.next() : null;
      }
      if (seriesIndex != null) {
        Glob transaction = seriesIndex.findByIndex(Transaction.POSITION_MONTH,
                                                   deferredCardDay.get(DeferredCardDate.MONTH)).getGlobs().getFirst();
        if (transaction != null){
          repository.update(deferredCardDay.getKey(), DeferredCardDate.DAY, transaction.get(Transaction.BANK_DAY));
        }
        else {
          repository.update(deferredCardDay.getKey(), DeferredCardDate.DAY,
                            Month.getDay(day, deferredCardDay.get(DeferredCardDate.MONTH), calendar));
        }
      }
      else {
        repository.update(deferredCardDay.getKey(), DeferredCardDate.DAY,
                          Month.getDay(day, deferredCardDay.get(DeferredCardDate.MONTH), calendar));
      }
    }
  }
}
