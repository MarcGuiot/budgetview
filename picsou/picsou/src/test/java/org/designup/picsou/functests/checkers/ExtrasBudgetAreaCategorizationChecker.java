package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.BudgetArea;

public class ExtrasBudgetAreaCategorizationChecker extends BudgetAreaCategorizationChecker {
  public ExtrasBudgetAreaCategorizationChecker(CategorizationChecker categorizationChecker) {
    super(categorizationChecker, BudgetArea.EXTRAS);
  }

  public void createProject() {
    categorizationChecker.createProject();
  }

  public void editProjectSeries(String seriesName) {
    categorizationChecker.editProject(seriesName);
  }
}
