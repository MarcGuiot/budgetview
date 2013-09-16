package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.ProjectItemAmount;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class ProjectItemToAmountGlobalTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectItem.TYPE, new DefaultChangeSetVisitor() {
      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob item = repository.get(key);
        Integer projectItemId = item.get(ProjectItem.ID);

        if (values.contains(ProjectItem.USE_SAME_AMOUNTS) || values.contains(ProjectItem.MONTH_COUNT)) {
          if (item.get(ProjectItem.MONTH_COUNT) == 1) {
            repository.update(key, ProjectItem.USE_SAME_AMOUNTS, true);
          }
        }

        if (!item.isTrue(ProjectItem.USE_SAME_AMOUNTS) &&
            (values.contains(ProjectItem.FIRST_MONTH) || values.contains(ProjectItem.MONTH_COUNT))) {
          ProjectItemToAmountLocalTrigger.updateAmountsForPeriod(item, projectItemId, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        repository.delete(ProjectItemAmount.TYPE, fieldIn(ProjectItemAmount.PROJECT_ITEM, key.get(ProjectItem.ID)));
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
