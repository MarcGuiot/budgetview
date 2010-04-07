package org.designup.picsou.gui.series.view;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SeriesWrapperUpdateTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer seriesId = key.get(Series.ID);
        if (seriesId.equals(Series.UNCATEGORIZED_SERIES_ID)) {
          return;
        }
        Glob budgetAreaWrapper =
          repository.findUnique(SeriesWrapper.TYPE,
                                value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId()),
                                value(SeriesWrapper.ITEM_ID, values.get(Series.BUDGET_AREA)));

        repository.create(SeriesWrapper.TYPE,
                          value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId()),
                          value(SeriesWrapper.ITEM_ID, seriesId),
                          value(SeriesWrapper.MASTER, budgetAreaWrapper.get(SeriesWrapper.ID)));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.BUDGET_AREA)){
          Glob wrapper = SeriesWrapper.find(repository, SeriesWrapperType.SERIES, key.get(Series.ID));
          if (wrapper != null){
            repository.delete(wrapper.getKey());
            Glob budgetAreaWrapper =
              repository.findUnique(SeriesWrapper.TYPE,
                                    value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId()),
                                    value(SeriesWrapper.ITEM_ID, values.get(Series.BUDGET_AREA)));
            repository.create(SeriesWrapper.TYPE,
                              value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId()),
                              value(SeriesWrapper.ITEM_ID, key.get(Series.ID)),
                              value(SeriesWrapper.MASTER, budgetAreaWrapper.get(SeriesWrapper.ID)));
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer seriesId = key.get(Series.ID);
        Glob wrapper = SeriesWrapper.find(repository, SeriesWrapperType.SERIES, seriesId);
        if (wrapper != null) {
          repository.delete(wrapper.getKey());
        }
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
                          value(SeriesWrapper.ITEM_ID, null));
      }

      Map<Integer, Integer> budgetAreaIds = new HashMap<Integer, Integer>();

      for (Glob budgetArea : repository.getAll(BudgetArea.TYPE)) {
        Integer budgetAreaId = budgetArea.get(BudgetArea.ID);
        Integer wrapperId;
        if (budgetAreaId.equals(BudgetArea.ALL.getId())) {
          wrapperId = SeriesWrapper.ALL_ID;
        }
        else if (budgetAreaId.equals(BudgetArea.UNCATEGORIZED.getId())) {
          wrapperId = SeriesWrapper.UNCATEGORIZED_ID;
        }
        else {
          Glob wrapper = repository.create(SeriesWrapper.TYPE,
                                           value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId()),
                                           value(SeriesWrapper.ITEM_ID, budgetAreaId),
                                           value(SeriesWrapper.MASTER, null));
          wrapperId = wrapper.get(SeriesWrapper.ID);
        }
        budgetAreaIds.put(budgetAreaId, wrapperId);
      }

      for (Glob series : repository.getAll(Series.TYPE)) {
        Integer budgetAreaId = series.get(Series.BUDGET_AREA);
        if (BudgetArea.UNCATEGORIZED.getId().equals(budgetAreaId)) {
          continue;
        }
        Integer budgetAreaWrapperId = budgetAreaIds.get(budgetAreaId);
        repository.create(SeriesWrapper.TYPE,
                          value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId()),
                          value(SeriesWrapper.ITEM_ID, series.get(Series.ID)),
                          value(SeriesWrapper.MASTER, budgetAreaWrapperId));
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

}
