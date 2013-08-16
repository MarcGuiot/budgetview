package org.designup.picsou.triggers.projects;

import org.designup.picsou.gui.model.ProjectItemStat;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class ProjectItemToStatTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectItem.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        repository.create(ProjectItemStat.TYPE,
                          value(ProjectItemStat.PROJECT_ITEM, key.get(ProjectItem.ID)),
                          value(ProjectItemStat.ACTUAL_AMOUNT, 0.00),
                          value(ProjectItemStat.PLANNED_AMOUNT, values.get(ProjectItem.PLANNED_AMOUNT, 0.00)));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(ProjectItem.PLANNED_AMOUNT)) {
          repository.update(Key.create(ProjectItemStat.TYPE, key.get(ProjectItem.ID)),
                            value(ProjectItemStat.PLANNED_AMOUNT, values.get(ProjectItem.PLANNED_AMOUNT, 0.00)));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        repository.delete(Key.create(ProjectItemStat.TYPE, key.get(ProjectItem.ID)));
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Project.TYPE) || changedTypes.contains(ProjectItem.TYPE)) {
      repository.deleteAll(ProjectItemStat.TYPE);
      for (Glob projectItem : repository.getAll(ProjectItem.TYPE)) {
        Key statKey = Key.create(ProjectItemStat.TYPE, projectItem.get(ProjectItem.ID));
        repository.findOrCreate(statKey);
        repository.update(statKey,
                          value(ProjectItemStat.PLANNED_AMOUNT, projectItem.get(ProjectItem.PLANNED_AMOUNT, 0.00)));
      }
    }
  }
}
