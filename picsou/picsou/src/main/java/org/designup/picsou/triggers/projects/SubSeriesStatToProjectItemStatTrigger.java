package org.designup.picsou.triggers.projects;

import org.designup.picsou.gui.model.ProjectItemStat;
import org.designup.picsou.gui.model.SubSeriesStat;
import org.designup.picsou.model.ProjectItem;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

public class SubSeriesStatToProjectItemStatTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(SubSeriesStat.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob subSeriesStat = repository.get(key);
        Double delta = subSeriesStat.get(SubSeriesStat.AMOUNT, 0.00);
        updateTargetStat(key.get(SubSeriesStat.SUB_SERIES), delta, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Double delta = values.get(SubSeriesStat.AMOUNT, 0.00) - values.getPrevious(SubSeriesStat.AMOUNT, 0.00);
        updateTargetStat(key.get(SubSeriesStat.SUB_SERIES), delta, repository);
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Double delta = -previousValues.get(SubSeriesStat.AMOUNT, 0.00);
        updateTargetStat(key.get(SubSeriesStat.SUB_SERIES), delta, repository);
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(SubSeriesStat.TYPE)) {
      for (Glob stat : repository.getAll(SubSeriesStat.TYPE)) {
        updateTargetStat(stat.get(SubSeriesStat.SUB_SERIES), stat.get(SubSeriesStat.AMOUNT, 0.00), repository);
      }
    }
  }

  private void updateTargetStat(Integer subSeriesId, Double delta, GlobRepository repository) {
    GlobList items = repository.findByIndex(ProjectItem.SUB_SERIES_INDEX, subSeriesId);
    if (items.size() == 1) {
      Glob projectItem = items.getFirst();
      Glob projectItemStat = repository.find(Key.create(ProjectItemStat.TYPE, projectItem.get(ProjectItem.ID)));
      if (projectItemStat != null) {
        Double actual = projectItemStat.get(ProjectItemStat.ACTUAL_AMOUNT);
        repository.update(projectItemStat.getKey(),
                          ProjectItemStat.ACTUAL_AMOUNT,
                          actual + delta);
      }
    }
  }
}
