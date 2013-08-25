package org.designup.picsou.triggers.projects;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.model.ProjectItemStat;
import org.designup.picsou.gui.model.SubSeriesStat;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.SubSeries;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;

import java.util.HashSet;
import java.util.Set;

public class SubSeriesStatToProjectItemStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (!changeSet.containsChanges(SubSeriesStat.TYPE) && !changeSet.containsChanges(ProjectItem.TYPE)) {
      return;
    }

    final Set<Integer> modifiedProjectItemIds = new HashSet<Integer>();
    changeSet.safeVisit(SubSeriesStat.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob subSeriesStat = repository.get(key);
        Double delta = subSeriesStat.get(SubSeriesStat.ACTUAL_AMOUNT, 0.00);
        updateTargetStat(key.get(SubSeriesStat.SUB_SERIES), delta, modifiedProjectItemIds, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Double delta = values.get(SubSeriesStat.ACTUAL_AMOUNT, 0.00) - values.getPrevious(SubSeriesStat.ACTUAL_AMOUNT, 0.00);
        updateTargetStat(key.get(SubSeriesStat.SUB_SERIES), delta, modifiedProjectItemIds, repository);
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Double delta = -previousValues.get(SubSeriesStat.ACTUAL_AMOUNT, 0.00);
        updateTargetStat(key.get(SubSeriesStat.SUB_SERIES), delta, modifiedProjectItemIds, repository);
      }
    });
    for (Key key : changeSet.getUpdated(ProjectItem.MONTH)) {
      modifiedProjectItemIds.add(key.get(ProjectItem.ID));
    }
    for (Integer projectItemId : modifiedProjectItemIds) {
      checkCategorizationWarning(projectItemId, repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  private void updateTargetStat(Integer subSeriesId, Double delta, Set<Integer> modifiedProjectItemIds, GlobRepository repository) {
    GlobList items = repository.findByIndex(ProjectItem.SUB_SERIES_INDEX, subSeriesId);
    if (items.size() != 1) {
      return;
    }
    Glob projectItem = items.getFirst();
    Glob projectItemStat = repository.findOrCreate(Key.create(ProjectItemStat.TYPE, projectItem.get(ProjectItem.ID)));
    Double actual = projectItemStat.get(ProjectItemStat.ACTUAL_AMOUNT);
    double newValue = actual + delta;
    repository.update(projectItemStat.getKey(),
                      ProjectItemStat.ACTUAL_AMOUNT,
                      newValue);
    modifiedProjectItemIds.add(projectItem.get(ProjectItem.ID));
  }

  private void checkCategorizationWarning(Integer projectItemId, GlobRepository repository) {
    Glob projectItem = repository.get(Key.create(ProjectItem.TYPE, projectItemId));
    Glob subSeries = repository.get(Key.create(SubSeries.TYPE, projectItem.get(ProjectItem.SUB_SERIES)));
    boolean warning = false;
    for (Glob subSeriesStat : repository.findLinkedTo(subSeries, SubSeriesStat.SUB_SERIES)) {
      if (!Utils.equal(subSeriesStat.get(SubSeriesStat.MONTH), projectItem.get(ProjectItem.MONTH))
          && Amounts.isNotZero(subSeriesStat.get(SubSeriesStat.ACTUAL_AMOUNT))) {
        warning = true;
      }
    }
    repository.update(Key.create(ProjectItemStat.TYPE, projectItemId),
                      ProjectItemStat.CATEGORIZATION_WARNING,
                      warning);
  }
}
