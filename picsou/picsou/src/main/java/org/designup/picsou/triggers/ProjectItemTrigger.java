package org.designup.picsou.triggers;

import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.SubSeries;
import org.globsframework.model.*;

import static org.globsframework.model.FieldValue.value;

public class ProjectItemTrigger extends AbstractChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectItem.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        createSubSeries(key, values, repository);
      }

      public void visitUpdate(Key itemKey, FieldValuesWithPrevious values) throws Exception {
        if (!values.contains(ProjectItem.LABEL)) {
          return;
        }
        Glob subSeries = repository.findLinkTarget(repository.get(itemKey), ProjectItem.SUB_SERIES);
        if (subSeries != null) {
          repository.update(subSeries.getKey(),
                            value(SubSeries.NAME, values.get(ProjectItem.LABEL)));
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
    Glob project = repository.get(Key.create(Project.TYPE, projectItemValues.get(ProjectItem.PROJECT)));
    Glob subSeries =
      repository.create(SubSeries.TYPE,
                        value(SubSeries.SERIES, project.get(Project.SERIES)),
                        value(SubSeries.NAME, projectItemValues.get(ProjectItem.LABEL)));
    repository.update(projectItemKey,
                      value(ProjectItem.SUB_SERIES, subSeries.get(SubSeries.ID)));
    System.out.println("- ProjectItemTrigger.createSubSeries ==> " + subSeries);
  }
}
