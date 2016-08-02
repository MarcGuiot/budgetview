package com.budgetview.triggers.projects;

import com.budgetview.desktop.model.ProjectItemStat;
import com.budgetview.model.Project;
import com.budgetview.model.ProjectItem;
import com.budgetview.model.ProjectItemAmount;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class ProjectItemToStatTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (!changeSet.containsChanges(ProjectItem.TYPE) && !changeSet.containsChanges(ProjectItemAmount.TYPE)) {
      return;
    }

    final Set<Integer> changedItemIds = new HashSet<Integer>();
    changeSet.safeVisit(ProjectItem.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        repository.findOrCreate(Key.create(ProjectItemStat.TYPE, key.get(ProjectItem.ID)),
                                value(ProjectItemStat.PLANNED_AMOUNT, ProjectItem.getTotalPlannedAmount(values, repository)));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(ProjectItem.PLANNED_AMOUNT) ||
            values.contains(ProjectItem.MONTH_COUNT) ||
            values.contains(ProjectItem.USE_SAME_AMOUNTS)) {
          changedItemIds.add(key.get(ProjectItem.ID));
          Glob item = repository.get(key);
          repository.update(Key.create(ProjectItemStat.TYPE, key.get(ProjectItem.ID)),
                            value(ProjectItemStat.PLANNED_AMOUNT, ProjectItem.getTotalPlannedAmount(item, repository)));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Key statKey = Key.create(ProjectItemStat.TYPE, key.get(ProjectItem.ID));
        if (repository.contains(statKey)) {
          repository.delete(statKey);
        }
      }
    });

    for (Key amountKey : changeSet.getCreatedOrUpdated(ProjectItemAmount.TYPE)) {
      Glob itemAmount = repository.get(amountKey);
      changedItemIds.add(itemAmount.get(ProjectItemAmount.PROJECT_ITEM));
    }
    for (Integer itemId : changedItemIds) {
      Glob item = repository.find(Key.create(ProjectItem.TYPE, itemId));
      if (item != null) {
        updateStatForItem(item, repository);
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Project.TYPE) || changedTypes.contains(ProjectItem.TYPE) || changedTypes.contains(ProjectItemAmount.TYPE)) {
      repository.deleteAll(ProjectItemStat.TYPE);
      for (Glob projectItem : repository.getAll(ProjectItem.TYPE)) {
        updateStatForItem(projectItem, repository);
      }
    }
  }

  private void updateStatForItem(Glob projectItem, GlobRepository repository) {
    Key statKey = Key.create(ProjectItemStat.TYPE, projectItem.get(ProjectItem.ID));
    repository.findOrCreate(statKey);
    repository.update(statKey,
                      value(ProjectItemStat.PLANNED_AMOUNT, ProjectItem.getTotalPlannedAmount(projectItem, repository)));
  }
}
