package org.designup.picsou.triggers.projects;

import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.model.SeriesType;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class SeriesStatToProjectStatTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(SeriesStat.TYPE)) {
      updateAll(changeSet.getChanged(SeriesStat.TYPE), repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Project.TYPE) || changedTypes.contains(SeriesStat.TYPE)) {
      for (Glob project : repository.getAll(Project.TYPE)) {
        update(project, repository);
      }
    }
  }

  private void updateAll(Set<Key> modifiedStats, GlobRepository repository) {
    Set<Integer> seriesIds = new HashSet<Integer>();
    for (Key modifiedStat : modifiedStats) {
      if (SeriesType.SERIES.equals(SeriesType.get(modifiedStat.get(SeriesStat.TARGET_TYPE)))) {
        seriesIds.add(modifiedStat.get(SeriesStat.TARGET));
      }
    }
    for (Integer seriesId : seriesIds) {
      Glob series = repository.find(Key.create(Series.TYPE, seriesId));
      if (series != null) {
        Glob project = Project.findProject(series, repository);
        if (project != null) {
          update(project, repository);
        }
      }
    }
  }

  private void update(Glob project, GlobRepository repository) {
    double actual = 0;
    for (Integer seriesId : Project.getSeriesIds(project, repository)) {
      for (Glob seriesStat : SeriesStat.getAllMonths(seriesId, SeriesType.SERIES, repository)) {
        Double statActual = seriesStat.get(SeriesStat.ACTUAL_AMOUNT);
        if (statActual != null) {
          actual += statActual;
        }
      }

      Glob projectStat = repository.findOrCreate(Key.create(ProjectStat.TYPE, project.get(Project.ID)));
      repository.update(projectStat.getKey(),
                        value(ProjectStat.ACTUAL_AMOUNT, actual));

    }
  }
}
