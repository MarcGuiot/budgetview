package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class ProjectTransferToSeriesTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectTransfer.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key projectTransferKey, FieldValues values) throws Exception {
        if (shouldCreateSavingsSeries(projectTransferKey, values, repository)) {
          createSavingsSeries(projectTransferKey, values, repository);
        }
      }

      public void visitUpdate(Key projectTransferKey, FieldValuesWithPrevious values) throws Exception {
        Glob transfer = repository.get(projectTransferKey);
        FieldValues newTransferValues = FieldValuesBuilder.init(transfer.toArray())
          .set(values)
          .get();
        FieldValues previousTransferValues = FieldValuesBuilder.init(transfer.toArray())
          .set(values.getPreviousValues())
          .get();
        boolean isSavings = ProjectTransfer.isSavings(newTransferValues, repository);
        boolean wasSavings = ProjectTransfer.isSavings(previousTransferValues, repository);
        if (isSavings && !wasSavings) {
          createSavingsSeries(projectTransferKey, newTransferValues, repository);
        }
        else if (wasSavings && !isSavings) {
          deleteSavingsSeries(projectTransferKey, previousTransferValues, repository);
        }
        else if (isSavings) {
          updateSavingsSeries(projectTransferKey, newTransferValues, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  private void createSavingsSeries(Key projectTransferKey, FieldValues values, GlobRepository repository) {
    Glob projectTransfer = repository.get(projectTransferKey);
    Glob item = ProjectTransfer.getItemFromTransfer(projectTransferKey, repository);
    Integer firstMonth = item.get(ProjectItem.MONTH);
    Integer lastMonth = ProjectItem.getLastMonth(item);
    Glob series = repository.create(Series.TYPE,
                                    value(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()),
                                    value(Series.NAME, item.get(ProjectItem.LABEL)),
                                    value(Series.IS_AUTOMATIC, false),
                                    value(Series.IS_INITIAL, false),
                                    value(Series.INITIAL_AMOUNT, null),
                                    value(Series.FROM_ACCOUNT, projectTransfer.get(ProjectTransfer.FROM_ACCOUNT)),
                                    value(Series.TARGET_ACCOUNT, projectTransfer.get(ProjectTransfer.FROM_ACCOUNT)),
                                    value(Series.TO_ACCOUNT, projectTransfer.get(ProjectTransfer.TO_ACCOUNT)),
                                    value(Series.FIRST_MONTH, firstMonth),
                                    value(Series.LAST_MONTH, lastMonth));
    repository.update(item.getKey(), ProjectItem.SERIES, series.get(Series.ID));
    repository.update(item.getKey(), ProjectItem.SUB_SERIES, null);
  }

  private void updateSavingsSeries(Key projectTransferKey, FieldValues values, GlobRepository repository) {
    Glob item = ProjectTransfer.getItemFromTransfer(projectTransferKey, repository);
    Key seriesKey = Key.create(Series.TYPE, item.get(ProjectItem.SERIES));
    repository.update(seriesKey,
                      value(Series.FROM_ACCOUNT, values.get(ProjectTransfer.FROM_ACCOUNT)),
                      value(Series.TARGET_ACCOUNT, values.get(ProjectTransfer.FROM_ACCOUNT)),
                      value(Series.TO_ACCOUNT, values.get(ProjectTransfer.TO_ACCOUNT)));
  }

  private void deleteSavingsSeries(Key key, FieldValues previousValues, GlobRepository repository) {
    Key seriesKey = Key.create(Series.TYPE, previousValues.get(ProjectItem.SERIES));
    repository.delete(seriesKey);

    Glob item = repository.find(Key.create(ProjectItem.TYPE, key.get(ProjectTransfer.PROJECT_ITEM)));
    Glob project = repository.findLinkTarget(item, ProjectItem.PROJECT);
    repository.update(item.getKey(), ProjectItem.SERIES, project.get(Project.SERIES));
  }

  private boolean shouldCreateSavingsSeries(Key projectTransferKey, FieldValues transferValues, GlobRepository repository) {
    Glob item = ProjectTransfer.getItemFromTransfer(projectTransferKey, repository);
    return Utils.equal(ProjectItemType.get(item), ProjectItemType.TRANSFER) &&
           ProjectTransfer.isSavings(transferValues, repository);
  }
}
