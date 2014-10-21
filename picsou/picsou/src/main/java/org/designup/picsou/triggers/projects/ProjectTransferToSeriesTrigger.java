package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Utils;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;

public class ProjectTransferToSeriesTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(ProjectTransfer.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key projectTransferKey, FieldValues values) throws Exception {
        Glob item = ProjectTransfer.getItemFromTransfer(projectTransferKey, repository);
        if (Utils.equal(ProjectItemType.get(item), ProjectItemType.TRANSFER) &&
            ProjectTransfer.usesSavingsAccounts(values, repository)) {
          createSavingsSeries(projectTransferKey, repository);
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
        boolean isSavings = ProjectTransfer.usesSavingsAccounts(newTransferValues, repository);
        boolean wasSavings = ProjectTransfer.usesSavingsAccounts(previousTransferValues, repository);
        if (isSavings && !wasSavings) {
          createSavingsSeries(projectTransferKey, repository);
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

  public static Glob createSavingsSeries(Key projectTransferKey, GlobRepository repository) {
    Glob projectTransfer = repository.get(projectTransferKey);
    Glob item = ProjectTransfer.getItemFromTransfer(projectTransferKey, repository);
    Integer firstMonth = item.get(ProjectItem.FIRST_MONTH);
    Integer lastMonth = ProjectItem.getLastMonth(item);
    Glob series = repository.create(Series.TYPE,
                                    value(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId()),
                                    value(Series.NAME, item.get(ProjectItem.LABEL)),
                                    value(Series.ACTIVE, item.get(ProjectItem.ACTIVE)),
                                    value(Series.IS_AUTOMATIC, false),
                                    value(Series.IS_INITIAL, false),
                                    value(Series.INITIAL_AMOUNT, null),
                                    value(Series.FROM_ACCOUNT, projectTransfer.get(ProjectTransfer.FROM_ACCOUNT)),
                                    value(Series.TARGET_ACCOUNT, projectTransfer.get(ProjectTransfer.TO_ACCOUNT)),
                                    value(Series.TO_ACCOUNT, projectTransfer.get(ProjectTransfer.TO_ACCOUNT)),
                                    value(Series.FIRST_MONTH, firstMonth),
                                    value(Series.LAST_MONTH, lastMonth));
    repository.update(item.getKey(), ProjectItem.SERIES, series.get(Series.ID));
    return series;
  }

  private void updateSavingsSeries(Key projectTransferKey, FieldValues values, GlobRepository repository) {
    Glob item = ProjectTransfer.getItemFromTransfer(projectTransferKey, repository);
    Key seriesKey = Key.create(Series.TYPE, item.get(ProjectItem.SERIES));
    int targetAccount;
    Glob series = repository.get(seriesKey);
    if (Utils.equal(series.get(Series.FROM_ACCOUNT), series.get(Series.TARGET_ACCOUNT))){
      targetAccount = values.get(ProjectTransfer.FROM_ACCOUNT);
    }
    else /*if (Utils.equal(series.get(Series.TO_ACCOUNT), series.get(Series.TARGET_ACCOUNT)))*/ {
      targetAccount = values.get(ProjectTransfer.TO_ACCOUNT);
    }
    repository.update(seriesKey,
                      value(Series.FROM_ACCOUNT, values.get(ProjectTransfer.FROM_ACCOUNT)),
                      value(Series.TARGET_ACCOUNT, targetAccount),
                      value(Series.TO_ACCOUNT, values.get(ProjectTransfer.TO_ACCOUNT)));
  }

  private void deleteSavingsSeries(Key key, FieldValues previousValues, GlobRepository repository) {
    Key seriesKey = Key.create(Series.TYPE, previousValues.get(ProjectItem.SERIES));
    Glob series = repository.find(seriesKey);
    if (series != null) {
      Integer mirrorId = series.get(Series.MIRROR_SERIES);
      if (mirrorId != null) {
        Glob mirror = repository.find(Key.create(Series.TYPE, mirrorId));
        if (mirror != null) {
          Transaction.uncategorize(repository.getAll(Transaction.TYPE, linkedTo(series, Transaction.SERIES)), repository);
          repository.delete(mirror);
        }
      }
      repository.delete(series);
    }

    Glob item = repository.find(Key.create(ProjectItem.TYPE, key.get(ProjectTransfer.PROJECT_ITEM)));
    ProjectItemToSeriesTrigger.createSeries(item.getKey(), item, repository);
  }
}
