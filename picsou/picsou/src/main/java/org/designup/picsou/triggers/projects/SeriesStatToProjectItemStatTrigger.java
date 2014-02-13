package org.designup.picsou.triggers.projects;

import org.designup.picsou.gui.model.ProjectItemStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.ProjectItem;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

public class SeriesStatToProjectItemStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (!changeSet.containsChanges(SeriesStat.TYPE) && !changeSet.containsChanges(ProjectItem.TYPE)) {
      return;
    }

    changeSet.safeVisit(SeriesStat.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob seriesStat = repository.get(key);
        Double delta = seriesStat.get(SeriesStat.ACTUAL_AMOUNT, 0.00);
        updateTargetStat(key.get(SeriesStat.TARGET), delta, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesStat.ACTUAL_AMOUNT)) {
          Double delta = values.get(SeriesStat.ACTUAL_AMOUNT, 0.00) - values.getPrevious(SeriesStat.ACTUAL_AMOUNT, 0.00);
          updateTargetStat(key.get(SeriesStat.TARGET), delta, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Double delta = -previousValues.get(SeriesStat.ACTUAL_AMOUNT, 0.00);
        updateTargetStat(key.get(SeriesStat.TARGET), delta, repository);
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  private void updateTargetStat(Integer seriesId, Double delta, GlobRepository repository) {
    GlobList items = repository.findByIndex(ProjectItem.SERIES_INDEX, seriesId);
    for (Glob projectItem : items) {
      if (!ProjectItem.usesSeries(projectItem)) {
        continue;
      }
      Glob projectItemStat = repository.findOrCreate(Key.create(ProjectItemStat.TYPE, projectItem.get(ProjectItem.ID)));
      Double actual = projectItemStat.get(ProjectItemStat.ACTUAL_AMOUNT);
      double newValue = actual + delta;
      repository.update(projectItemStat.getKey(),
                        ProjectItemStat.ACTUAL_AMOUNT,
                        newValue);
    }
  }
}
