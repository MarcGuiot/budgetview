package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;

import java.util.Set;

public class ProjectItemToSeriesTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectItem.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob project = repository.get(Key.create(Project.TYPE, values.get(ProjectItem.PROJECT)));
        repository.update(key, ProjectItem.SERIES, project.get(Project.SERIES));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(ProjectItem.ITEM_TYPE)) {
          throw new UnsupportedOperationException("Not managed");
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Glob project = repository.find(Key.create(Project.TYPE, previousValues.get(ProjectItem.PROJECT)));
        Integer projectItemSeriesId = previousValues.get(ProjectItem.SERIES);
        if (projectItemSeriesId == null) {
          return;
        }
        Integer projectSeriesId = project != null ? project.get(Project.SERIES) : null;
        Key seriesKey = Key.create(Series.TYPE, projectItemSeriesId);
        if (repository.contains(seriesKey) &&
            (project == null || !Utils.equal(projectSeriesId, projectItemSeriesId))) {
          repository.delete(seriesKey);
        }
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(ProjectItem.TYPE)) {
      for (Glob item : repository.getAll(ProjectItem.TYPE)) {
        if (item.get(ProjectItem.SERIES) == null) {
          Glob project = repository.findLinkTarget(item, ProjectItem.PROJECT);
          repository.update(item.getKey(), ProjectItem.SERIES, project.get(Project.SERIES));
        }
      }
    }
  }
}
