package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class DeferredCardDayTrigger extends DefaultChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Month.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        GlobList deferredAccount = repository.getAll(Account.TYPE, GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
        for (Glob account : deferredAccount) {
          updateDeferredCarDay(repository, account);
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList deferredAccount = repository.getAll(Account.TYPE, GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()));
        for (Glob account : deferredAccount) {
          updateDeferredCarDay(repository, account);
        }
      }
    });

    changeSet.safeVisit(Account.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        updateDeferredCarDay(repository, repository.find(key));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        updateDeferredCarDay(repository, repository.find(key));
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

  private class PeriodChangeSetVisitor implements ChangeSetVisitor {
    private GlobRepository repository;

    public PeriodChangeSetVisitor(GlobRepository repository) {
      this.repository = repository;
    }

    public void visitCreation(Key key, FieldValues values) throws Exception {
      Integer accountId = values.get(DeferredCardPeriod.ACCOUNT);
      updateDeferredCarDay(repository, repository.find(Key.create(Account.TYPE, accountId)));
    }

    public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      Integer accountId = repository.find(key).get(DeferredCardPeriod.ACCOUNT);
      updateDeferredCarDay(repository, repository.find(Key.create(Account.TYPE, accountId)));
    }

    public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      Integer accountId = previousValues.get(DeferredCardPeriod.ACCOUNT);
      Glob account = repository.find(Key.create(Account.TYPE, accountId));
      if (account != null) {
        updateDeferredCarDay(repository, account);
      }
    }
  }

  private void updateDeferredCarDay(GlobRepository repository, final Glob account) {
    Calendar calendar = Calendar.getInstance();
    Integer accountId = account.get(Account.ID);
    GlobList periods = repository.getAll(DeferredCardPeriod.TYPE,
                                         GlobMatchers.fieldEquals(DeferredCardPeriod.ACCOUNT, accountId))
      .sort(DeferredCardPeriod.FROM_MONTH);

    ReadOnlyGlobRepository.MultiFieldIndexed indexOnDeferredCardDay = repository.findByIndex(DeferredCardDate.ACCOUNT_AND_DATE, DeferredCardDate.ACCOUNT, accountId);

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
    for (Glob deferredCardDay : deferredCardDays) {
      if (currentPeriod == null ||
          deferredCardDay.get(DeferredCardDate.MONTH) < currentPeriod.get(DeferredCardPeriod.FROM_MONTH)) {
        repository.update(deferredCardDay.getKey(), DeferredCardDate.DAY,
                          Month.getDay(day, deferredCardDay.get(DeferredCardDate.MONTH), calendar));
      }
      else {
        day = currentPeriod.get(DeferredCardPeriod.DAY);
        currentPeriod = iterator.hasNext() ? iterator.next() : null;
      }
    }
  }

}
