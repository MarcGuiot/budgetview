package org.designup.picsou.gui.projects.upgrade;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.accounts.utils.AccountMatchers;
import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.model.SeriesType;
import org.designup.picsou.gui.transactions.utils.MirrorTransactionFinder;
import org.designup.picsou.gui.upgrade.BindTransactionsToSeries;
import org.designup.picsou.gui.upgrade.PostProcessor;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.designup.picsou.triggers.projects.ProjectItemToSeriesTrigger;
import org.designup.picsou.triggers.projects.ProjectTransferToSeriesTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import static org.designup.picsou.model.ProjectItemType.isExpenses;
import static org.designup.picsou.model.ProjectItemType.isTransfer;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class ProjectUpgradeV40 {

  private final GlobRepository repository;
  private final PostProcessor postProcessor;
  private final Integer defaultMainAccount;
  private Set<Integer> managedSeries = new HashSet<Integer>();

  public static void run(GlobRepository repository, PostProcessor postProcessor) {
    ProjectUpgradeV40 upgrade = new ProjectUpgradeV40(repository, postProcessor);
    upgrade.updateProjectSeriesAndGroups();
  }

  private ProjectUpgradeV40(GlobRepository repository, PostProcessor postProcessor) {
    this.repository = repository;
    this.postProcessor = postProcessor;
    GlobList accounts = repository.getAll(Account.TYPE, AccountMatchers.userCreatedMainAccounts()).sortSelf(new AccountComparator());
    this.defaultMainAccount = accounts.isEmpty() ? null : accounts.getFirst().get(Account.ID);
  }

  public void updateProjectSeriesAndGroups() {
    createMissingGroupsForProjects(repository);
    upgradeSeriesForExpensesItems(repository);
    upgradeSeriesForTransferItems(repository);
    deleteProjectLevelSeries(repository);
    fixIncoherentFromToInTransferSeries(repository);
    postProcessor.add(new UpdateSequenceNumbers());
  }

  private void createMissingGroupsForProjects(GlobRepository repository) {
    for (Glob project : repository.getAll(ProjectItem.TYPE, isExpenses()).getTargets(ProjectItem.PROJECT, repository)) {
      if (repository.findLinkTarget(project, Project.SERIES_GROUP) == null) {
        Glob group = repository.create(SeriesGroup.TYPE,
                                       value(SeriesGroup.BUDGET_AREA, BudgetArea.EXTRAS.getId()),
                                       value(SeriesGroup.NAME, project.get(Project.NAME)),
                                       value(SeriesGroup.EXPANDED, false));
        repository.update(project.getKey(), Project.SERIES_GROUP, group.get(SeriesGroup.ID));
      }
    }
  }

  private void upgradeSeriesForExpensesItems(GlobRepository repository) {
    for (Glob item : repository.getAll(ProjectItem.TYPE, isExpenses())) {
      Glob project = repository.findLinkTarget(item, ProjectItem.PROJECT);
      Integer projectSeriesId = project.get(Project.SERIES);

      Integer itemSeriesId = item.get(ProjectItem.SERIES);
      if ((itemSeriesId != null) && !Utils.equal(projectSeriesId, itemSeriesId)) {
        repository.update(Key.create(Series.TYPE, itemSeriesId), Series.GROUP, project.get(Project.SERIES_GROUP));
        repository.update(item.getKey(),
                          value(ProjectItem.SUB_SERIES, null),
                          value(ProjectItem.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID));
        return;
      }

      GlobList transactions = repository.getAll(Transaction.TYPE,
                                                and(fieldEquals(Transaction.SERIES, projectSeriesId),
                                                    fieldEquals(Transaction.SUB_SERIES, item.get(ProjectItem.SUB_SERIES)),
                                                    isFalse(Transaction.PLANNED)));
      repository.update(item.getKey(),
                        value(ProjectItem.SUB_SERIES, null),
                        value(ProjectItem.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID));
      Glob itemSeries = ProjectItemToSeriesTrigger.createSeries(item, repository);
      if (!transactions.isEmpty()) {
        managedSeries.add(itemSeries.get(Series.ID));
        postProcessor.add(new BindTransactionsToSeries(itemSeries, transactions));
        clearTransactionSeries(transactions, repository);
      }
    }
  }

  private void upgradeSeriesForTransferItems(GlobRepository repository) {
    for (final Glob item : repository.getAll(ProjectItem.TYPE, isTransfer())) {

      repository.update(item.getKey(), ProjectItem.SUB_SERIES, null);

      GlobList allTransactions = new GlobList();
      Glob transfer = ProjectTransfer.getTransferFromItem(item, repository);
      Glob project = repository.findLinkTarget(item, ProjectItem.PROJECT);

      Glob series = repository.findLinkTarget(item, ProjectItem.SERIES);
      if (series == null) {
        series = ProjectTransferToSeriesTrigger.createSavingsSeriesIfComplete(transfer.getKey(), repository);
        if (series != null) {
          repository.update(item.getKey(), ProjectItem.SERIES, series.get(Series.ID));
        }
      }
      else {
        clearActualStats(series);
        managedSeries.add(series.get(Series.ID));
        Glob mirror = repository.findLinkTarget(series, Series.MIRROR_SERIES);
        if (mirror != null) {
          managedSeries.add(mirror.get(Series.ID));
          clearActualStats(mirror);
        }

        if (!ProjectTransfer.usesMainAccounts(transfer, repository)) {
          storeSeriesBinding(series, repository);
          storeSeriesBinding(mirror, repository);
          continue;
        }

        GlobList seriesTransactions = repository.findLinkedTo(series, Transaction.SERIES);
        GlobList mirrorTransactions = repository.findLinkedTo(mirror, Transaction.SERIES);
        allTransactions.addAll(seriesTransactions);
        allTransactions.addAll(mirrorTransactions);
      }


      LinkField transferField;
      if (ProjectTransfer.isFromAccountAMainAccount(transfer, repository)) {
        transferField = ProjectTransfer.FROM_ACCOUNT;
      }
      else {
        transferField = ProjectTransfer.TO_ACCOUNT;
      }

      Integer[] mainAccountIds = getMainUserCreatedAccounts(allTransactions, repository);
      if (mainAccountIds.length == 0) {
        Integer accountId = transfer.get(transferField) == null ? null : defaultMainAccount;
        createTransferItem(item, item.get(ProjectItem.LABEL), project, transferField, accountId, allTransactions, repository);
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

      Series.delete(repository.findLinkTarget(item, ProjectItem.SERIES), repository);
      ProjectItem.deleteAll(item, repository);
      clearTransactionSeries(allTransactions, repository);
    }
  }

  private void fixIncoherentFromToInTransferSeries(GlobRepository repository) {
    for (Glob transfer : repository.getAll(ProjectTransfer.TYPE)) {
      Glob item = ProjectTransfer.getItemFromTransfer(transfer, repository);
      Glob series = repository.findLinkTarget(item, ProjectItem.SERIES);
      if (series == null) {
        continue;
      }
      Integer from = series.get(Series.FROM_ACCOUNT);
      Integer to = series.get(Series.TO_ACCOUNT);
      Integer target = series.get(Series.TARGET_ACCOUNT);
      for (Glob seriesBudget : repository.findLinkedTo(series, SeriesBudget.SERIES)) {
        Double amount = seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0.00);
        if (((amount < 0) && Utils.equal(target, to)) ||
            ((amount > 0) && Utils.equal(target, from))) {
          repository.update(seriesBudget.getKey(), SeriesBudget.PLANNED_AMOUNT, -amount);
        }
      }
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
    postProcessor.add(new BindTransferTransactions(newItem, sourceTransactions, mirrorAccountTransactions));

    ProjectTransferToSeriesTrigger.createSavingsSeriesIfComplete(newTransfer.getKey(), repository);

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
          repository.delete(SeriesStat.TYPE, SeriesStat.summariesLinkedToSeries(seriesKey));
          repository.delete(SeriesBudget.TYPE, fieldEquals(SeriesBudget.SERIES, seriesId));
          repository.delete(SubSeries.TYPE, fieldEquals(SubSeries.SERIES, seriesId));
          repository.delete(seriesKey);
          repository.update(project.getKey(), Project.SERIES, null);

          GlobList transactions = repository.getAll(Transaction.TYPE, and(fieldEquals(Transaction.SERIES, seriesId)));
          if (!transactions.isEmpty()) {
            clearTransactionSeries(transactions, repository);
            postProcessor.add(new CreateMiscProjectItem(project, transactions));
          }
        }
        repository.update(project, Project.SERIES, null);
      }
    }
  }

  private void clearActualStats(final Glob series) {
    if (series == null) {
      return;
    }
    final Integer seriesId = series.get(Series.ID);
    postProcessor.add(new PostProcessor.Functor() {
      public void apply(GlobRepository repository) {
        for (Glob stat : repository.getAll(SeriesStat.TYPE,
                                           and(fieldEquals(SeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()),
                                               fieldEquals(SeriesStat.TARGET, seriesId)))) {
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
    postProcessor.add(new BindTransactionsToSeries(series, transactions));
    clearTransactionSeries(transactions, repository);
  }

  private void clearTransactionSeries(GlobList transactions, GlobRepository repository) {
    Transaction.uncategorize(transactions, repository);
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

  private class CreateMiscProjectItem implements PostProcessor.Functor {
    private final Glob project;
    private final GlobList transactions;

    public CreateMiscProjectItem(Glob project, GlobList transactions) {
      this.project = project;
      this.transactions = transactions;
    }

    public void apply(GlobRepository repository) {
      SortedSet<Integer> monthIds = transactions.getSortedSet(Transaction.BUDGET_MONTH);
      Integer projectId = project.get(Project.ID);
      Glob item = repository.create(ProjectItem.TYPE,
                                    value(ProjectItem.ITEM_TYPE, ProjectItemType.EXPENSE.getId()),
                                    value(ProjectItem.LABEL, Lang.get("project.upgrade.misc")),
                                    value(ProjectItem.FIRST_MONTH, monthIds.first()),
                                    value(ProjectItem.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID),
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

      for (Glob transaction : transactions) {
        repository.update(transaction.getKey(), Transaction.SERIES, newSeries.get(Series.ID));
      }
    }
  }

  private class UpdateSequenceNumbers implements PostProcessor.Functor {
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

  private class BindTransferTransactions implements PostProcessor.Functor {
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
      if (seriesId == null) {
        return;
      }

      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      Glob mirror = repository.findLinkTarget(series, Series.MIRROR_SERIES);

      GlobList all = new GlobList();
      all.addAll(seriesTransactions);
      all.addAll(mirrorTransactions);
      all.keepExistingGlobsOnly(repository);
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
