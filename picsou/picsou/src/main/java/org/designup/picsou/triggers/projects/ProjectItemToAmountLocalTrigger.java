package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.ProjectItemAmount;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetVisitor;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class ProjectItemToAmountLocalTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectItem.TYPE, new DefaultChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (!values.isTrue(ProjectItem.USE_SAME_AMOUNTS)) {
          createAmounts(key, values, repository);
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob item = repository.get(key);
        Integer projectItemId = item.get(ProjectItem.ID);

        if (values.contains(ProjectItem.USE_SAME_AMOUNTS)) {
          if (item.isTrue(ProjectItem.USE_SAME_AMOUNTS)) {
            GlobList itemAmounts = repository.findByIndex(ProjectItemAmount.PROJECT_ITEM_INDEX, ProjectItemAmount.PROJECT_ITEM, projectItemId)
              .getGlobs()
              .sort(ProjectItemAmount.MONTH);
            repository.update(key, ProjectItem.PLANNED_AMOUNT,
                              itemAmounts.isEmpty() ? 0.00 : itemAmounts.getFirst().get(ProjectItemAmount.PLANNED_AMOUNT));
            repository.delete(itemAmounts);
          }
          else {
            createAmounts(key, item, repository);
          }
          return;
        }

        if (!item.isTrue(ProjectItem.USE_SAME_AMOUNTS) &&
            (values.contains(ProjectItem.FIRST_MONTH) || values.contains(ProjectItem.MONTH_COUNT))) {
          updateAmountsForPeriod(item, projectItemId, repository);
          return;
        }
      }
    });
  }

  public static void updateAmountsForPeriod(Glob item, Integer projectItemId, GlobRepository repository) {
    GlobList itemAmounts = repository.findByIndex(ProjectItemAmount.PROJECT_ITEM_INDEX, ProjectItemAmount.PROJECT_ITEM, projectItemId)
      .getGlobs()
      .sort(ProjectItemAmount.MONTH);
    Double[] amounts  = itemAmounts.getValues(ProjectItemAmount.PLANNED_AMOUNT);
    repository.delete(itemAmounts);

    ClosedMonthRange monthRange = ProjectItem.getMonthRange(item);
    int index = 0;
    for (Integer monthId : monthRange) {
      Double amount = index >= amounts.length ? 0.00 : amounts[index++];
      repository.create(ProjectItemAmount.TYPE,
                        value(ProjectItemAmount.PROJECT_ITEM, projectItemId),
                        value(ProjectItemAmount.MONTH, monthId),
                        value(ProjectItemAmount.PLANNED_AMOUNT, amount));
    }
  }

  private static void createAmounts(Key key, FieldValues values, GlobRepository repository) {
    ClosedMonthRange monthRange = ProjectItem.getMonthRange(values);
    if (monthRange == null) {
      return;
    }
    Double amount = values.get(ProjectItem.PLANNED_AMOUNT);
    for (Integer monthId : monthRange) {
      Glob itemAmount = repository.findOrCreate(Key.create(ProjectItemAmount.PROJECT_ITEM, key.get(ProjectItem.ID),
                                                           ProjectItemAmount.MONTH, monthId));
      repository.update(itemAmount.getKey(), ProjectItemAmount.PLANNED_AMOUNT, amount);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
