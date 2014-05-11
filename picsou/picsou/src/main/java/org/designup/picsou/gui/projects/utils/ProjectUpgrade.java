package org.designup.picsou.gui.projects.utils;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.model.SeriesType;
import org.designup.picsou.gui.transactions.utils.MirrorTransactionFinder;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.designup.picsou.triggers.projects.ProjectItemToSeriesTrigger;
import org.designup.picsou.triggers.projects.ProjectToSeriesGroupTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.utils.Log;

import java.util.*;

import static org.designup.picsou.model.ProjectItemType.isExpenses;
import static org.designup.picsou.model.ProjectItemType.isTransfer;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class ProjectUpgrade {

  private List<Functor> functors = new ArrayList<Functor>();
  private Set<Integer> managedSeries = new HashSet<Integer>();
  public void updateProjectSeriesAndGroups(GlobRepository repository) {
    ProjectToSeriesGroupTrigger.createGroupsForProjects(repository);
    setDefaultAccountsForAllProjects(repository);
    createSeriesForExpensesItems(repository);
    createSeriesForTransferItems(repository);
    deleteProjectLevelSeries(repository);
    functors.add(new UpdateSequenceNumbers());
  }

  private void setDefaultAccountsForAllProjects(GlobRepository repository) {
    Integer defaultAccountId = Account.getDefaultMainAccountId(repository);
    for (Glob project : repository.getAll(Project.TYPE)) {
      GlobList transactions = repository.getAll(Transaction.TYPE,
                                                and(fieldEquals(Transaction.SERIES, project.get(Project.SERIES)),
                                                    isFalse(Transaction.PLANNED)));
      if (!transactions.isEmpty()) {
        Integer mostFrequentAccountId = null;
        int maxCount = -1;
        Set<Integer> accountIds = transactions.getValueSet(Transaction.ACCOUNT);
        for (Integer accountId : accountIds) {
          Glob account = repository.find(Key.create(Account.TYPE, accountId));
          if ((account == null) || !Account.isUserCreatedMainAccount(account)) {
            continue;
          }
          GlobList transactionsForAccount = transactions.filter(fieldEquals(Transaction.ACCOUNT, accountId), repository);
          if (transactionsForAccount.size() > maxCount) {
            mostFrequentAccountId = accountId;
            maxCount = transactionsForAccount.size();
          }
        }
        repository.update(project.getKey(), Project.DEFAULT_ACCOUNT, mostFrequentAccountId);
      }
      else {
        repository.update(project.getKey(), Project.DEFAULT_ACCOUNT, defaultAccountId);
      }
    }
  }

  private void createSeriesForExpensesItems(GlobRepository repository) {
    for (Glob item : repository.getAll(ProjectItem.TYPE, isExpenses())) {
      Integer seriesId = item.get(ProjectItem.SERIES);
      if (seriesId == null) {
        Glob project = repository.findLinkTarget(item, ProjectItem.PROJECT);
        seriesId = project.get(Project.SERIES);
      }
      Integer subSeriesId = item.get(ProjectItem.SUB_SERIES);
      GlobList transactions = repository.getAll(Transaction.TYPE,
                                                and(fieldEquals(Transaction.SERIES, seriesId),
                                                    fieldEquals(Transaction.SUB_SERIES, subSeriesId),
                                                    isFalse(Transaction.PLANNED)));
      repository.update(item.getKey(), ProjectItem.SUB_SERIES, null);

      Glob project = repository.findLinkTarget(item, ProjectItem.PROJECT);
      Set<Integer> accountIdSet = transactions.getValueSet(Transaction.ACCOUNT);
      Integer[] accountIds = accountIdSet.toArray(new Integer[accountIdSet.size()]);
      if (accountIds.length < 2) {
        createSeriesForItem(item, repository, transactions);
        repository.update(item.getKey(),
                          value(ProjectItem.ACCOUNT,
                                accountIds.length == 1 ? accountIds[0] : project.get(Project.DEFAULT_ACCOUNT)));
      }
      else {
        String itemLabel = item.get(ProjectItem.LABEL);
        Glob[] itemForAccounts = new Glob[accountIds.length];
        for (int i = 0; i < accountIds.length; i++) {
          itemForAccounts[i] = i == 0 ? item : ProjectItem.duplicate(item, "", project, 0, repository);
          String newItemLabel = itemLabel;
          Integer accountId = accountIds[i];
          if (accountId != null) {
            Glob account = repository.find(Key.create(Account.TYPE, accountId));
            newItemLabel += " - " + account.get(Account.NAME);
          }
          repository.update(itemForAccounts[i].getKey(),
                            value(ProjectItem.ACCOUNT, accountId),
                            value(ProjectItem.LABEL, newItemLabel));
          GlobList accountTransactions = transactions.filter(fieldEquals(Transaction.ACCOUNT, accountId), repository);
          createSeriesForItem(itemForAccounts[i], repository, accountTransactions);
        }
        double[] planned = Amounts.split(ProjectItem.getTotalPlannedAmount(item, repository), accountIds.length);
        for (int i = 0; i < itemForAccounts.length; i++) {
          reducePlannedForItem(itemForAccounts[i], planned[i], repository);
        }
      }

      clearTransactionSeries(transactions, repository);
    }
  }

  private void reducePlannedForItem(Glob item, double planned, GlobRepository repository) {
    if (item.isTrue(ProjectItem.USE_SAME_AMOUNTS)) {
      repository.update(item.getKey(),
                        ProjectItem.PLANNED_AMOUNT,
                        Amounts.normalize(planned / item.get(ProjectItem.MONTH_COUNT)));
    }
    else {
      GlobList amounts = repository.findLinkedTo(item, ProjectItemAmount.PROJECT_ITEM);
      Glob[] amountsArray = amounts.toArray();
      double[] plannedAmounts = Amounts.adjustTotal(amounts.getValues(ProjectItemAmount.PLANNED_AMOUNT), planned);
      for (int i = 0; i < amountsArray.length; i++) {
        repository.update(amountsArray[i].getKey(), ProjectItemAmount.PLANNED_AMOUNT, plannedAmounts[i]);
      }
    }
  }

  private void createSeriesForItem(Glob item, GlobRepository repository, GlobList transactions) {
    Glob itemSeries = ProjectItemToSeriesTrigger.createSeries(item, repository);
    if (!transactions.isEmpty()) {
      managedSeries.add(itemSeries.get(Series.ID));
      functors.add(new BindTransactionsToSeries(itemSeries, transactions));
    }
  }

  private void createSeriesForTransferItems(GlobRepository repository) {
    for (final Glob item : repository.getAll(ProjectItem.TYPE, isTransfer())) {

      repository.update(item.getKey(), ProjectItem.SUB_SERIES, null);

      final Glob series = repository.findLinkTarget(item, ProjectItem.SERIES);
      clearActualStats(series);
      managedSeries.add(series.get(Series.ID));
      Glob mirror = repository.findLinkTarget(series, Series.MIRROR_SERIES);
      if (mirror != null) {
        managedSeries.add(mirror.get(Series.ID));
        clearActualStats(mirror);
      }

      Glob transfer = ProjectTransfer.getTransferFromItem(item, repository);
      if (!ProjectTransfer.usesMainAccounts(transfer, repository)) {
        storeSeriesBinding(series, repository);
        storeSeriesBinding(mirror, repository);
        continue;
      }

      GlobList seriesTransactions = repository.findLinkedTo(series, Transaction.SERIES);
      GlobList mirrorTransactions = repository.findLinkedTo(mirror, Transaction.SERIES);
      GlobList allTransactions = new GlobList();
      allTransactions.addAll(seriesTransactions);
      allTransactions.addAll(mirrorTransactions);

      Glob project = repository.findLinkTarget(item, ProjectItem.PROJECT);
      Integer projectAccountId = project.get(Project.DEFAULT_ACCOUNT);

      LinkField transferField;
      if (ProjectTransfer.isFromAccountAMainAccount(transfer, repository)) {
        transferField = ProjectTransfer.FROM_ACCOUNT;
      }
      else {
        transferField = ProjectTransfer.TO_ACCOUNT;
      }

      Integer[] mainAccountIds = getMainUserCreatedAccounts(allTransactions, repository);
      if (mainAccountIds.length == 0) {
        createTransferItem(item, item.get(ProjectItem.LABEL), project, transferField, projectAccountId, allTransactions, repository);
      }
      else {
        String itemLabel = item.get(ProjectItem.LABEL);
        Glob[] itemForAccounts = new Glob[mainAccountIds.length];
        for (int i = 0; i < mainAccountIds.length; i++) {
          String newItemLabel = itemLabel;
          Integer accountId = mainAccountIds[i];
          if (mainAccountIds.length > 1) {
            Glob account = repository.find(Key.create(Account.TYPE, accountId));
            newItemLabel += " - " + account.get(Account.NAME);
          }
          itemForAccounts[i] = createTransferItem(item, newItemLabel, project, transferField, accountId, allTransactions, repository);
        }
        double[] planned = Amounts.split(ProjectItem.getTotalPlannedAmount(item, repository), mainAccountIds.length);
        for (int i = 0; i < itemForAccounts.length; i++) {
          reducePlannedForItem(itemForAccounts[i], planned[i], repository);
        }
      }

      ProjectItem.deleteAll(item, repository);
      clearTransactionSeries(allTransactions, repository);
    }
  }

  private Glob createTransferItem(Glob item, String itemLabel, Glob project, LinkField transferField, Integer sourceAccountId, GlobList allTransactions, GlobRepository repository) {
    Glob newItem = ProjectItem.duplicate(item, itemLabel, project, 0, repository);
    repository.update(newItem.getKey(),
                      value(ProjectItem.ACCOUNT, sourceAccountId),
                      value(ProjectItem.LABEL, itemLabel));
    Glob newTransfer = ProjectTransfer.getTransferFromItem(newItem, repository);
    repository.update(newTransfer.getKey(), transferField, sourceAccountId);

    LinkField targetField = transferField == ProjectTransfer.FROM_ACCOUNT ? ProjectTransfer.TO_ACCOUNT : ProjectTransfer.FROM_ACCOUNT;
    Integer targetAccountId = newTransfer.get(targetField);

    GlobList sourceTransactions = allTransactions.filter(fieldEquals(Transaction.ACCOUNT, sourceAccountId), repository);
    GlobList mirrorAccountTransactions = findMirrorTransactions(sourceTransactions, targetAccountId, allTransactions, repository);
    functors.add(new BindTransferTransactions(newItem, sourceTransactions, mirrorAccountTransactions));

    return newItem;
  }

  private GlobList findMirrorTransactions(GlobList sourceTransactions, Integer targetAccountId, GlobList allTransactions, GlobRepository repository) {
    return MirrorTransactionFinder.getClosestMirrors(sourceTransactions, targetAccountId, allTransactions, repository);
  }

  private Integer[] getMainUserCreatedAccounts(GlobList transactions, GlobRepository repository) {
    Set<Integer> result = new HashSet<Integer>();
    for (Integer accountId : transactions.getValueSet(Transaction.ACCOUNT)) {
      Glob account = repository.find(Key.create(Account.TYPE, accountId));
      if (account != null && Account.isUserCreatedMainAccount(account)) {
        result.add(accountId);
      }
    }
    return result.toArray(new Integer[result.size()]);
  }

  private void deleteProjectLevelSeries(GlobRepository repository) {
    for (Glob project : repository.getAll(Project.TYPE)) {
      Integer seriesId = project.get(Project.SERIES);
      if (seriesId != null) {
        if (!managedSeries.contains(seriesId)) {
          repository.delete(Transaction.TYPE,
                            and(fieldEquals(Transaction.SERIES, seriesId),
                                isTrue(Transaction.PLANNED)));
          Key seriesKey = Key.create(Series.TYPE, seriesId);
          repository.update(project.getKey(), Project.SERIES, null);
          repository.delete(SeriesStat.TYPE, SeriesStat.linkedToSeries(seriesKey));
          repository.delete(SeriesBudget.TYPE, fieldEquals(SeriesBudget.SERIES, seriesId));
          repository.delete(SubSeries.TYPE, fieldEquals(SubSeries.SERIES, seriesId));
          repository.delete(seriesKey);
          repository.update(project.getKey(), Project.SERIES, null);

          GlobList transactions = repository.getAll(Transaction.TYPE, and(fieldEquals(Transaction.SERIES, seriesId)));
          if (!transactions.isEmpty()) {
            clearTransactionSeries(transactions, repository);
            functors.add(new CreateMiscProjectItem(project, transactions));
          }
        }
      }
    }
  }

  private void clearActualStats(final Glob series) {
    if (series == null) {
      return;
    }
    final Integer seriesId = series.get(Series.ID);
    functors.add(new Functor() {
      public void apply(GlobRepository repository) {
        for (Glob stat : repository.getAll(SeriesStat.TYPE,
                                           and(fieldEquals(SeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()),
                                               fieldEquals(SeriesStat.TARGET, seriesId))
        )) {
          repository.update(stat.getKey(), SeriesStat.ACTUAL_AMOUNT, 0.00);
        }
      }
    });
  }

  private void storeSeriesBinding(Glob series, GlobRepository repository) {
    if (series == null) {
      return;
    }
    GlobList transactions = repository.getAll(Transaction.TYPE,
                                              and(fieldEquals(Transaction.SERIES, series.get(Series.ID)),
                                                  isFalse(Transaction.PLANNED)));
    storeSeriesBinding(series, transactions, repository);
  }

  private void storeSeriesBinding(Glob series, GlobList transactions, GlobRepository repository) {
    functors.add(new BindTransactionsToSeries(series, transactions));
    clearTransactionSeries(transactions, repository);
  }

  private void clearTransactionSeries(GlobList transactions, GlobRepository repository) {
    for (Glob transaction : transactions) {
      repository.update(transaction.getKey(),
                        value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                        value(Transaction.SUB_SERIES, null));
    }
  }

  public void postProcessing(GlobRepository repository) {
    for (Functor functor : functors) {
      functor.apply(repository);
    }
    functors.clear();
  }

  private interface Functor {
    void apply(GlobRepository repository);
  }

  private class BindTransactionsToSeries implements Functor {

    private final Integer seriesId;
    private final GlobList transactions;

    public BindTransactionsToSeries(Glob series, GlobList transactions) {
      this.seriesId = series.get(Series.ID);
      this.transactions = transactions;
    }

    public void apply(GlobRepository repository) {
      for (Glob transaction : transactions) {
        if (transaction.exists()) {
          repository.update(transaction.getKey(),
                            value(Transaction.SERIES, seriesId),
                            value(Transaction.SUB_SERIES, null));
        }
      }
    }
  }

  private class CreateMiscProjectItem implements Functor {
    private final Glob project;
    private final GlobList transactions;

    public CreateMiscProjectItem(Glob project, GlobList transactions) {
      this.project = project;
      this.transactions = transactions;
    }

    public void apply(GlobRepository repository) {

      Set<Integer> accountIds = transactions.getSortedSet(Transaction.ACCOUNT);
      for (Integer accountId : accountIds) {
        String suffix = "";
        if (accountIds.size() > 1) {
          Glob account = repository.find(Key.create(Account.TYPE, accountId));
          if (account != null) {
            suffix = " - " + account.get(Account.NAME);
          }
        }
        processAccount(accountId, suffix, repository);
      }
    }

    private void processAccount(Integer accountId, String suffix, GlobRepository repository) {

      GlobList accountTransactions = transactions.filter(fieldEquals(Transaction.ACCOUNT, accountId), repository);

      SortedSet<Integer> monthIds = accountTransactions.getSortedSet(Transaction.BUDGET_MONTH);
      Integer projectId = project.get(Project.ID);
      Glob item = repository.create(ProjectItem.TYPE,
                                    value(ProjectItem.ITEM_TYPE, ProjectItemType.EXPENSE.getId()),
                                    value(ProjectItem.LABEL, Lang.get("project.upgrade.misc") + suffix),
                                    value(ProjectItem.FIRST_MONTH, monthIds.first()),
                                    value(ProjectItem.PROJECT, projectId),
                                    value(ProjectItem.SEQUENCE_NUMBER, ProjectItem.getNextSequenceNumber(projectId, repository)));
      if (monthIds.size() == 1) {
        repository.update(item.getKey(),
                          value(ProjectItem.MONTH_COUNT, 1),
                          value(ProjectItem.USE_SAME_AMOUNTS, true),
                          value(ProjectItem.PLANNED_AMOUNT, 0.00));
      }
      else {
        ClosedMonthRange range = new ClosedMonthRange(monthIds.first(), monthIds.last());
        repository.update(item.getKey(),
                          value(ProjectItem.MONTH_COUNT, range.length()),
                          value(ProjectItem.USE_SAME_AMOUNTS, false),
                          value(ProjectItem.PLANNED_AMOUNT, null));
        for (Integer month : range) {
          repository.create(ProjectItemAmount.TYPE,
                            value(ProjectItemAmount.MONTH, month),
                            value(ProjectItemAmount.PROJECT_ITEM, item.get(ProjectItem.ID)),
                            value(ProjectItemAmount.PLANNED_AMOUNT, 0.00));
        }
      }

      Glob newSeries = ProjectItemToSeriesTrigger.createSeries(item, repository);

      for (Glob transaction : accountTransactions) {
        repository.update(transaction.getKey(), Transaction.SERIES, newSeries.get(Series.ID));
      }
    }
  }

  private class UpdateSequenceNumbers implements Functor {
    public void apply(GlobRepository repository) {
      for (Glob project : repository.getAll(Project.TYPE)) {
        GlobList items = repository.getAll(ProjectItem.TYPE, linkedTo(project, ProjectItem.PROJECT));
        if (items.containsValue(ProjectItem.SEQUENCE_NUMBER, null)) {
          items.sort(new GlobFieldsComparator(ProjectItem.FIRST_MONTH, true, ProjectItem.LABEL, true));
          int index = 0;
          for (Glob item : items) {
            repository.update(item.getKey(), ProjectItem.SEQUENCE_NUMBER, index++);
          }
        }
      }
    }
  }

  private class BindTransferTransactions implements Functor {
    private Glob item;
    private GlobList seriesTransactions;
    private GlobList mirrorTransactions;

    public BindTransferTransactions(Glob item, GlobList sourceTransactions, GlobList targetTransactions) {
      this.item = item;
      this.seriesTransactions = sourceTransactions;
      this.mirrorTransactions = targetTransactions;
    }

    public void apply(GlobRepository repository) {
      Integer seriesId = item.get(ProjectItem.SERIES);
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      Glob mirror = repository.findLinkTarget(series, Series.MIRROR_SERIES);

      GlobList all = new GlobList();
      all.addAll(seriesTransactions);
      all.addAll(mirrorTransactions);
      for (Glob transaction : all) {
        if (series.get(Series.TARGET_ACCOUNT).equals(transaction.get(Transaction.ACCOUNT))) {
          repository.update(transaction.getKey(), Transaction.SERIES, series.get(Series.ID));
        }
        else if (mirror.get(Series.TARGET_ACCOUNT).equals(transaction.get(Transaction.ACCOUNT))) {
          repository.update(transaction.getKey(), Transaction.SERIES, mirror.get(Series.ID));
        }
        else {
          Log.write("Transaction not in series/mirror target account: " +
                    transaction.get(Transaction.LABEL) + " - date: " +
                    transaction.get(Transaction.DAY) + "/" + transaction.get(Transaction.MONTH) + " - amount: " +
                    transaction.get(Transaction.AMOUNT));
        }
      }
    }
  }
}
