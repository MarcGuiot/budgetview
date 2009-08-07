package org.designup.picsou.gui.series.view;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SeriesWrapperUpdater implements ChangeSetListener {

  private GlobRepository localRepository;
  private boolean excludeBudgetAreaAll = false;
  private boolean createSummaries = false;

  public SeriesWrapperUpdater(GlobRepository localRepository) {
    this.localRepository = localRepository;
  }

  public void setExcludeBudgetAreaAll(boolean excludeBudgetAreaAll) {
    this.excludeBudgetAreaAll = excludeBudgetAreaAll;
  }

  public void setCreateSummaries(boolean createSummaries) {
    this.createSummaries = createSummaries;
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer seriesId = key.get(Series.ID);

        Glob budgetAreaWrapper =
          localRepository.findUnique(SeriesWrapper.TYPE,
                                     value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId()),
                                     value(SeriesWrapper.ITEM_ID, values.get(Series.BUDGET_AREA)));

        localRepository.create(SeriesWrapper.TYPE,
                               value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId()),
                               value(SeriesWrapper.ITEM_ID, seriesId),
                               value(SeriesWrapper.MASTER, budgetAreaWrapper.get(SeriesWrapper.ID)));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer seriesId = key.get(Series.ID);
        Glob wrapper = SeriesWrapper.find(localRepository, SeriesWrapperType.SERIES, seriesId);
        if (wrapper != null) {
          localRepository.delete(wrapper.getKey());
        }
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    localRepository.startChangeSet();
    try {
      localRepository.deleteAll(SeriesWrapper.TYPE);

      if (!excludeBudgetAreaAll) {
        localRepository.create(SeriesWrapper.TYPE,
                               value(SeriesWrapper.ID, SeriesWrapper.ALL_ID),
                               value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId()),
                               value(SeriesWrapper.ITEM_ID, BudgetArea.ALL.getId()));
      }
      localRepository.create(SeriesWrapper.TYPE,
                             value(SeriesWrapper.ID, SeriesWrapper.UNCATEGORIZED_ID),
                             value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId()),
                             value(SeriesWrapper.ITEM_ID, BudgetArea.UNCATEGORIZED.getId()));

      if (createSummaries) {
        for (Integer id : Arrays.asList(SeriesWrapper.BALANCE_SUMMARY_ID,
                                        SeriesWrapper.MAIN_POSITION_SUMMARY_ID,
                                        SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
          localRepository.create(SeriesWrapper.TYPE,
                                 value(SeriesWrapper.ID, id),
                                 value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SUMMARY.getId()),
                                 value(SeriesWrapper.ITEM_ID, null));
        }
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
          Glob wrapper = localRepository.create(SeriesWrapper.TYPE,
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
        localRepository.create(SeriesWrapper.TYPE,
                               value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId()),
                               value(SeriesWrapper.ITEM_ID, series.get(Series.ID)),
                               value(SeriesWrapper.MASTER, budgetAreaWrapperId));
      }
    }
    finally {
      localRepository.completeChangeSet();
    }
  }
}
