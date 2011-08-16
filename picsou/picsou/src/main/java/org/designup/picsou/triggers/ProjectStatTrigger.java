package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class ProjectStatTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(SeriesStat.TYPE)) {
      updateAll(changeSet.getCreatedOrUpdated(SeriesStat.TYPE), repository);
    }

    if (changeSet.containsCreationsOrDeletions(Project.TYPE)) {
      changeSet.safeVisit(Project.TYPE, new DefaultChangeSetVisitor() {
        public void visitDeletion(Key key, FieldValues values) throws Exception {
          Glob projectStat = repository.find(Key.create(ProjectStat.TYPE, key.get(Project.ID)));
          if (projectStat != null) {
            repository.delete(projectStat.getKey());
          }
        }
      });
    }
  }

  private void updateAll(Set<Key> modifiedStats, GlobRepository repository) {
    Set<Integer> seriesIds = new HashSet<Integer>();
    for (Key modifiedStat : modifiedStats) {
      seriesIds.add(modifiedStat.get(SeriesStat.SERIES));
    }
    for (Integer seriesId : seriesIds) {
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      Glob project = Project.findProject(series, repository);
      if (project != null) {
        update(project, repository);
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(SeriesStat.TYPE)) {
      updateAll(repository);
    }
  }

  private void update(Glob project, GlobRepository repository) {
    double actual = 0;
    double planned = 0;
    for (Glob seriesStat : repository.getAll(SeriesStat.TYPE,
                                             fieldEquals(SeriesStat.SERIES, project.get(Project.SERIES)))) {
      Double statActual = seriesStat.get(SeriesStat.AMOUNT);
      if (statActual != null) {
        actual += statActual;
      }
      Double statPlanned = seriesStat.get(SeriesStat.PLANNED_AMOUNT);
      if (statPlanned != null) {
        planned += statPlanned;
      }
    }

    Glob projectStat = repository.findOrCreate(Key.create(ProjectStat.TYPE, project.get(Project.ID)));
    repository.update(projectStat.getKey(),
                      value(ProjectStat.ACTUAL_AMOUNT, actual),
                      value(ProjectStat.PLANNED_AMOUNT, planned));
  }

  private void updateAll(GlobRepository repository) {
    repository.deleteAll(ProjectStat.TYPE);
    for (Glob project : repository.getAll(Project.TYPE)) {
      update(project, repository);
    }
  }
}
