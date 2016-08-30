package com.budgetview.desktop.series.view;

import com.budgetview.shared.model.BudgetArea;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesGroup;
import com.budgetview.model.SubSeries;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.utils.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesWrapperUpdateTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(SeriesGroup.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob budgetAreaWrapper =
          SeriesWrapper.getWrapperForBudgetArea(values.get(SeriesGroup.BUDGET_AREA), repository);
        if (SeriesWrapper.find(repository, SeriesWrapperType.SERIES_GROUP, key.get(SeriesGroup.ID)) == null) {
          repository.create(SeriesWrapper.TYPE,
                            value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES_GROUP.getId()),
                            value(SeriesWrapper.ITEM_ID, key.get(SeriesGroup.ID)),
                            value(SeriesWrapper.PARENT, budgetAreaWrapper.get(SeriesWrapper.ID)));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Glob groupWrapper =
          SeriesWrapper.getWrapperForSeriesGroup(key.get(SeriesGroup.ID), repository);
        for (Glob wrapper : repository.getAll(SeriesWrapper.TYPE, linkedTo(groupWrapper, SeriesWrapper.PARENT))) {
          Glob series = SeriesWrapper.getSeries(wrapper, repository);
          if (series != null) {
            BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
            repository.update(wrapper.getKey(),
                              value(SeriesWrapper.PARENT,
                                    SeriesWrapper.getWrapperForBudgetArea(budgetArea, repository).get(SeriesWrapper.ID))
            );
          }
        }
      }
    });

    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (!SeriesWrapper.shouldCreateWrapperForSeries(repository.find(key))) {
          return;
        }

        Glob parentWrapper;
        if (values.get(Series.GROUP) != null) {
          parentWrapper = SeriesWrapper.getWrapperForSeriesGroup(values.get(Series.GROUP), repository);
        }
        else {
          parentWrapper = SeriesWrapper.getWrapperForBudgetArea(values.get(Series.BUDGET_AREA), repository);
        }
        if (parentWrapper == null) {
          Log.write("Bug Empty parentWrapper for " + GlobPrinter.toString(values));
        }
        else {
          Integer seriesId = key.get(Series.ID);
          if (repository.findByIndex(SeriesWrapper.INDEX, SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId())
            .findByIndex(SeriesWrapper.ITEM_ID, seriesId)
            .findByIndex(SeriesWrapper.PARENT, parentWrapper.get(SeriesWrapper.ID)).getGlobs().isEmpty()) {
            repository.create(SeriesWrapper.TYPE,
                              value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId()),
                              value(SeriesWrapper.ITEM_ID, seriesId),
                              value(SeriesWrapper.PARENT, parentWrapper.get(SeriesWrapper.ID)));
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.BUDGET_AREA) || values.contains(Series.GROUP)) {
          Glob wrapper = SeriesWrapper.find(repository, SeriesWrapperType.SERIES, key.get(Series.ID));
          if (wrapper == null) {
            return;
          }

          Glob series = repository.get(key);
          Integer budgetAreaId = SeriesWrapper.getBudgetAreaForTarget(wrapper, repository).getId();
          repository.delete(wrapper);
          GlobList subSeries = repository.findLinkedTo(repository.get(key), SubSeries.SERIES);
          for (Glob sub : subSeries) {
            repository.delete(SeriesWrapper.findAll(repository, SeriesWrapperType.SUB_SERIES, sub.get(SubSeries.ID)));
          }

          if (BudgetArea.OTHER.getId().equals(budgetAreaId)) {
            return;
          }

          Glob parentWrapper;
          if (values.contains(Series.GROUP) && values.get(Series.GROUP) != null) {
            parentWrapper = SeriesWrapper.getWrapperForSeriesGroup(values.get(Series.GROUP), repository);
          }
          else {
            parentWrapper = SeriesWrapper.getWrapperForBudgetArea(series.get(Series.BUDGET_AREA), repository);
          }

          if (parentWrapper == null) {
            Log.write("Bug Empty parentWrapper for " + GlobPrinter.toString(values));
          }
          else {
            Glob seriesWrapper = repository.create(SeriesWrapper.TYPE,
                                                   value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId()),
                                                   value(SeriesWrapper.ITEM_ID, key.get(Series.ID)),
                                                   value(SeriesWrapper.PARENT, parentWrapper.get(SeriesWrapper.ID)));
            for (Glob sub : subSeries) {
              repository.create(SeriesWrapper.TYPE,
                                value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SUB_SERIES.getId()),
                                value(SeriesWrapper.ITEM_ID, sub.get(SubSeries.ID)),
                                value(SeriesWrapper.PARENT, seriesWrapper.get(SeriesWrapper.ID)));
            }
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        delete(key, SeriesWrapperType.SERIES, Series.ID, repository);
      }
    });

    changeSet.safeVisit(SubSeries.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob parentSeriesWrapper =
          SeriesWrapper.getWrapperForSeries(values.get(SubSeries.SERIES), repository);
        if (parentSeriesWrapper == null) {
          Log.write("Parent wrapper missing " + values.get(SubSeries.SERIES));
        }
        else {
          if (repository.findByIndex(SeriesWrapper.INDEX, SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SUB_SERIES.getId())
            .findByIndex(SeriesWrapper.ITEM_ID, key.get(SubSeries.ID))
            .findByIndex(SeriesWrapper.PARENT, parentSeriesWrapper.get(SeriesWrapper.ID)).getGlobs().isEmpty()) {
            repository.create(SeriesWrapper.TYPE,
                              value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SUB_SERIES.getId()),
                              value(SeriesWrapper.ITEM_ID, key.get(SubSeries.ID)),
                              value(SeriesWrapper.PARENT, parentSeriesWrapper.get(SeriesWrapper.ID)));
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        delete(key, SeriesWrapperType.SUB_SERIES, SubSeries.ID, repository);
      }
    });

    changeSet.safeVisit(SeriesGroup.TYPE, new DefaultChangeSetVisitor() {
      public void visitDeletion(Key key, FieldValues values) throws Exception {
        Integer groupId = key.get(SeriesGroup.ID);
        repository.delete(SeriesWrapper.TYPE,
                          and(fieldEquals(SeriesWrapper.ITEM_ID, groupId),
                              fieldEquals(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES_GROUP.getId()))
        );
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    repository.startChangeSet();
    try {
      repository.deleteAll(SeriesWrapper.TYPE);

      repository.create(SeriesWrapper.TYPE,
                        value(SeriesWrapper.ID, SeriesWrapper.ALL_ID),
                        value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId()),
                        value(SeriesWrapper.ITEM_ID, BudgetArea.ALL.getId()));

      repository.create(SeriesWrapper.TYPE,
                        value(SeriesWrapper.ID, SeriesWrapper.UNCATEGORIZED_ID),
                        value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId()),
                        value(SeriesWrapper.ITEM_ID, BudgetArea.UNCATEGORIZED.getId()));

      for (Integer id : SeriesWrapper.SUMMARY_IDS) {
        repository.create(SeriesWrapper.TYPE,
                          value(SeriesWrapper.ID, id),
                          value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SUMMARY.getId()),
                          value(SeriesWrapper.ITEM_ID, id));
      }

      Map<Integer, Integer> budgetAreaIds = new HashMap<Integer, Integer>();

      for (Glob budgetArea : repository.getAll(BudgetArea.TYPE)) {
        Integer budgetAreaId = budgetArea.get(BudgetArea.ID);
        Integer wrapperId = null;
        if (budgetAreaId.equals(BudgetArea.ALL.getId())) {
          wrapperId = SeriesWrapper.ALL_ID;
        }
        else if (budgetAreaId.equals(BudgetArea.UNCATEGORIZED.getId())) {
          wrapperId = SeriesWrapper.UNCATEGORIZED_ID;
        }
        else if (!BudgetArea.OTHER.getId().equals(budgetAreaId)) {
          Glob wrapper = repository.create(SeriesWrapper.TYPE,
                                           value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId()),
                                           value(SeriesWrapper.ITEM_ID, budgetAreaId),
                                           value(SeriesWrapper.PARENT, null));
          wrapperId = wrapper.get(SeriesWrapper.ID);
        }
        budgetAreaIds.put(budgetAreaId, wrapperId);
      }

      for (Glob group : repository.getAll(SeriesGroup.TYPE)) {
        Glob budgetAreaWrapper =
          SeriesWrapper.getWrapperForBudgetArea(group.get(SeriesGroup.BUDGET_AREA), repository);
        repository.create(SeriesWrapper.TYPE,
                          value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES_GROUP.getId()),
                          value(SeriesWrapper.ITEM_ID, group.get(SeriesGroup.ID)),
                          value(SeriesWrapper.PARENT, budgetAreaWrapper.get(SeriesWrapper.ID)));
      }

      for (Glob series : repository.getAll(Series.TYPE)) {
        if (!SeriesWrapper.shouldCreateWrapperForSeries(series)) {
          continue;
        }

        Integer parentWrapperId;
        if (series.get(Series.GROUP) != null) {
          parentWrapperId = SeriesWrapper.getWrapperForSeriesGroup(series.get(Series.GROUP), repository).get(SeriesWrapper.ID);
        }
        else {
          parentWrapperId = budgetAreaIds.get(series.get(Series.BUDGET_AREA));
        }

        if (parentWrapperId != null) {
          repository.create(SeriesWrapper.TYPE,
                            value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId()),
                            value(SeriesWrapper.ITEM_ID, series.get(Series.ID)),
                            value(SeriesWrapper.PARENT, parentWrapperId));
        }
      }

      for (Glob subSeries : repository.getAll(SubSeries.TYPE)) {
        Glob seriesWrapper =
          SeriesWrapper.getWrapperForSeries(subSeries.get(SubSeries.SERIES), repository);
        if (seriesWrapper == null) {
          Log.write("Missing parent " + GlobPrinter.toString(subSeries));
        }
        else {
          repository.create(SeriesWrapper.TYPE,
                            value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SUB_SERIES.getId()),
                            value(SeriesWrapper.ITEM_ID, subSeries.get(SubSeries.ID)),
                            value(SeriesWrapper.PARENT, seriesWrapper.get(SeriesWrapper.ID)));
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private void delete(Key key, SeriesWrapperType type, IntegerField idField, GlobRepository repository) {
    GlobList wrappers = SeriesWrapper.findAll(repository, type, key.get(idField));
    repository.delete(wrappers);
  }
}
