package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.BudgetArea;

public class ExtrasBudgetAreaCategorizationChecker extends BudgetAreaCategorizationChecker {
  public ExtrasBudgetAreaCategorizationChecker(CategorizationChecker categorizationChecker) {
    super(categorizationChecker, BudgetArea.EXTRAS);
  }

  public ProjectEditionChecker createProject() {
    return ProjectEditionChecker.open(categorizationChecker.getCreateProjectButton());
  }

  public ProjectEditionChecker editProjectSeries(String seriesName) {
    return categorizationChecker.editProject(seriesName);
  }
}
