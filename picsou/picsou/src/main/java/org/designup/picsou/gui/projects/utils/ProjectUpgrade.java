package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.model.SeriesType;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.designup.picsou.triggers.projects.ProjectItemToSeriesTrigger;
import org.designup.picsou.triggers.projects.ProjectToSeriesGroupTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobFieldsComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import static org.designup.picsou.model.ProjectItemType.*;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class ProjectUpgrade {

  private List<Functor> functors = new ArrayList<Functor>();

  public void updateProjectSeriesAndGroups(GlobRepository repository) {

    ProjectToSeriesGroupTrigger.createGroupsForProjects(repository);

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
      Glob series = ProjectItemToSeriesTrigger.createSeries(item, repository);
      if (!transactions.isEmpty()) {
        functors.add(new BindTransactionsToSeries(series, transactions));
      }
      clearSubSeries(repository, transactions);
    }
    for (Glob item : repository.getAll(ProjectItem.TYPE, isTransfer())) {
      Glob series = repository.findLinkTarget(item, ProjectItem.SERIES);
      storeSeriesBinding(series, repository);
      clearActualStats(series);
      Glob mirror = repository.findLinkTarget(series, Series.MIRROR_SERIES);
      if (mirror != null) {
        storeSeriesBinding(mirror, repository);
        clearActualStats(mirror);
      }
      repository.update(item.getKey(), ProjectItem.SUB_SERIES, null);
    }
    for (Glob project : repository.getAll(Project.TYPE)) {
      Integer seriesId = project.get(Project.SERIES);
      if (seriesId != null) {
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
          clearSubSeries(repository, transactions);
          functors.add(new CreateMiscProjectItem(project, transactions));
        }
      }
    }
    functors.add(new UpdateSequenceNumbers());
  }

  private void clearActualStats(final Glob series) {
    final Integer seriesId = series.get(Series.ID);
    functors.add(new Functor() {
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
    GlobList transactions = repository.getAll(Transaction.TYPE,
                                              and(fieldEquals(Transaction.SERIES, series.get(Series.ID)),
                                                  isFalse(Transaction.PLANNED)));
    functors.add(new BindTransactionsToSeries(series, transactions));
    clearSubSeries(repository, transactions);
  }

  private void clearSubSeries(GlobRepository repository, GlobList transactions) {
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
      SortedSet<Integer> monthIds = transactions.getSortedSet(Transaction.BUDGET_MONTH);
      Integer projectId = project.get(Project.ID);
      Glob item = repository.create(ProjectItem.TYPE,
                                    value(ProjectItem.ITEM_TYPE, ProjectItemType.EXPENSE.getId()),
                                    value(ProjectItem.LABEL, Lang.get("project.upgrade.misc")),
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

      for (Glob transaction : transactions) {
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
}
