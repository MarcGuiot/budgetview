package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.*;
import org.globsframework.model.*;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class ProjectItemToSubSeriesTrigger extends AbstractChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectItem.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (values.isTrue(ProjectItem.ACTIVE) && ProjectItem.usesSubSeries(values)) {
          createSubSeries(key, values, repository);
        }
      }

      public void visitUpdate(Key itemKey, FieldValuesWithPrevious values) throws Exception {
        Glob item = repository.get(itemKey);
        if (!ProjectItem.usesSubSeries(item)) {
          return;
        }
        if (values.contains(ProjectItem.ACTIVE)) {
          if (values.isTrue(ProjectItem.ACTIVE) && item.get(ProjectItem.SUB_SERIES) == null) {
            createSubSeries(itemKey, item, repository);
          }
          else if (!values.isTrue(ProjectItem.ACTIVE)) {
            Integer subSeriesId = item.get(ProjectItem.SUB_SERIES);
            Key subSeriesKey = Key.create(SubSeries.TYPE, subSeriesId);
            if (repository.contains(subSeriesKey) &&
                !repository.contains(Transaction.TYPE, fieldEquals(Transaction.SUB_SERIES, subSeriesId))) {
              repository.update(itemKey, ProjectItem.SUB_SERIES, null);
              repository.delete(subSeriesKey);
              return;
            }
          }
        }
        if (values.contains(ProjectItem.LABEL)) {
          Glob subSeries = repository.findLinkTarget(repository.get(itemKey), ProjectItem.SUB_SERIES);
          if (subSeries != null) {
            repository.update(subSeries.getKey(),
                              value(SubSeries.NAME, values.get(ProjectItem.LABEL)));
          }
        }
      }

      public void visitDeletion(Key itemKey, FieldValues previousValues) throws Exception {
        Integer subSeriesId = previousValues.get(ProjectItem.SUB_SERIES);
        if (subSeriesId == null) {
          return;
        }
        Key subSeriesKey = Key.create(SubSeries.TYPE, subSeriesId);
        if (repository.contains(subSeriesKey)) {
          repository.delete(subSeriesKey);
        }
      }
    });
  }

  public static void createSubSeries(Key projectItemKey, FieldValues projectItemValues, GlobRepository repository) {
    Integer projectId = projectItemValues.get(ProjectItem.PROJECT);
    Glob project = repository.get(Key.create(Project.TYPE, projectId));
    Glob subSeries =
      repository.create(SubSeries.TYPE,
                        value(SubSeries.SERIES, project.get(Project.SERIES)),
                        value(SubSeries.NAME, projectItemValues.get(ProjectItem.LABEL)));
    repository.update(projectItemKey, ProjectItem.SUB_SERIES, subSeries.get(SubSeries.ID));
  }
}
