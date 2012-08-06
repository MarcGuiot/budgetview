package org.designup.picsou.gui.projects.utils;

import org.designup.picsou.gui.model.ProjectItemStat;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.util.AmountMap;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class ProjectItemStatUpdater implements ChangeSetListener {
  private GlobRepository localRepository;
  private GlobRepository parentRepository;

  public ProjectItemStatUpdater(GlobRepository localRepository, GlobRepository parentRepository) {
    this.localRepository = localRepository;
    this.parentRepository = parentRepository;
    localRepository.addChangeListener(this);
  }

  public void reset(Key projectKey) {

    Glob project = localRepository.get(projectKey);

    AmountMap amounts = new AmountMap();
    for (Glob transaction : parentRepository.findByIndex(Transaction.SERIES_INDEX,
                                                         Transaction.SERIES,
                                                         project.get(Project.SERIES)).getGlobs()) {
      Integer subSeriesId = transaction.get(Transaction.SUB_SERIES);
      if (subSeriesId != null) {
        amounts.add(subSeriesId, transaction.get(Transaction.AMOUNT));
      }
    }

    localRepository.startChangeSet();
    try {
      localRepository.deleteAll(ProjectItemStat.TYPE);
      for (Glob projectItem : localRepository.findLinkedTo(project, ProjectItem.PROJECT)) {
        Integer subSeriesId = projectItem.get(ProjectItem.SUB_SERIES);
        if (subSeriesId != null) {
          localRepository.create(ProjectItemStat.TYPE,
                                 value(ProjectItemStat.PROJECT_ITEM, projectItem.get(ProjectItem.ID)),
                                 value(ProjectItemStat.ACTUAL_AMOUNT, amounts.get(subSeriesId)),
                                 value(ProjectItemStat.PLANNED_AMOUNT, projectItem.get(ProjectItem.AMOUNT, 0.00)));
        }
      }
    }
    finally {
      localRepository.completeChangeSet();
    }
  }

  public void clear() {
    localRepository.deleteAll(ProjectItemStat.TYPE);
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectItem.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        repository.create(ProjectItemStat.TYPE,
                          value(ProjectItemStat.PROJECT_ITEM, key.get(ProjectItem.ID)),
                          value(ProjectItemStat.ACTUAL_AMOUNT, 0.00),
                          value(ProjectItemStat.PLANNED_AMOUNT, values.get(ProjectItem.AMOUNT, 0.00)));

      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(ProjectItem.AMOUNT)) {
          repository.update(Key.create(ProjectItemStat.TYPE, key.get(ProjectItem.ID)),
                            value(ProjectItemStat.PLANNED_AMOUNT, values.get(ProjectItem.AMOUNT, 0.00)));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        repository.delete(Key.create(ProjectItemStat.TYPE, key.get(ProjectItem.ID)));
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
