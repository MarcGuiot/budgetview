package org.designup.picsou.triggers.projects;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.Series;
import org.globsframework.model.*;

import static org.globsframework.model.FieldValue.value;

public class ProjectToSeriesTrigger extends AbstractChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Project.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob series = repository.create(Series.TYPE,
                                        value(Series.BUDGET_AREA, BudgetArea.EXTRAS.getId()),
                                        value(Series.NAME, values.get(Project.NAME)),
                                        value(Series.IS_AUTOMATIC, false));
        repository.update(key, value(Project.SERIES, series.get(Series.ID)));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob project = repository.get(key);
        if (values.contains(Project.NAME)) {
          repository.update(project.getTargetKey(Project.SERIES),
                            value(Series.NAME, values.get(Project.NAME)));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Key seriesKey = Key.create(Series.TYPE, previousValues.get(Project.SERIES));
        repository.delete(seriesKey);
      }
    });
  }

}
