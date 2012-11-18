package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Calendar;
import java.util.Date;
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
        if (values.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
          updateDeferredCarDayOnAccountChange(repository, repository.find(key));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Account.CARD_TYPE)) {
          if (values.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
            updateDeferredCarDayOnAccountChange(repository, repository.find(key));
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Account.TYPE)) {
      GlobList deferredAccount = repository.getAll(Account.TYPE, GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
      for (Glob account : deferredAccount) {
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

    ReadOnlyGlobRepository.MultiFieldIndexed indexOnDeferredCardDay =
      repository.findByIndex(DeferredCardDate.ACCOUNT_AND_DATE, DeferredCardDate.ACCOUNT, accountId);

    GlobList deferredCardDays = indexOnDeferredCardDay.getGlobs();

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

    while (startMonth <= endMonth) {
      GlobList globs = indexOnDeferredCardDay.findByIndex(DeferredCardDate.MONTH, startMonth).getGlobs();
      if (globs.isEmpty()) {
        repository.create(DeferredCardDate.TYPE,
                          value(DeferredCardDate.ACCOUNT, accountId),
                          value(DeferredCardDate.MONTH, startMonth),
                          value(DeferredCardDate.DAY, Month.getDay(account.get(Account.DEFERRED_DEBIT_DAY), startMonth, calendar)));
      }
      else {
        deferredCardDays.remove(globs.getFirst());
      }
      startMonth = Month.next(startMonth);
    }

    repository.delete(deferredCardDays);

    GlobList newDeferredCardDays = indexOnDeferredCardDay.getGlobs();

    // on met a jour le jour de chaque mois
    ReadOnlyGlobRepository.MultiFieldIndexed seriesIndex = null;
    if (series != null) {
      seriesIndex = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID));
    }
    for (Glob deferredCardDay : newDeferredCardDays) {
      if (seriesIndex != null) {
        Glob transaction = seriesIndex.findByIndex(Transaction.POSITION_MONTH,
                                                   deferredCardDay.get(DeferredCardDate.MONTH)).getGlobs().getFirst();
        if (transaction != null) {
          repository.update(deferredCardDay.getKey(), DeferredCardDate.DAY, transaction.get(Transaction.BANK_DAY));
        }
        else {
          repository.update(deferredCardDay.getKey(), DeferredCardDate.DAY,
                            Month.getDay(account.get(Account.DEFERRED_DEBIT_DAY), deferredCardDay.get(DeferredCardDate.MONTH), calendar));
        }
      }
      else {
        repository.update(deferredCardDay.getKey(), DeferredCardDate.DAY,
                          Month.getDay(account.get(Account.DEFERRED_DEBIT_DAY), deferredCardDay.get(DeferredCardDate.MONTH), calendar));
      }
    }
  }
}
