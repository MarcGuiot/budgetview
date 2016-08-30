package com.budgetview.functests.checkers;

import com.budgetview.shared.model.BudgetArea;

public class ExtrasBudgetAreaCategorizationChecker extends BudgetAreaCategorizationChecker {
  public ExtrasBudgetAreaCategorizationChecker(CategorizationChecker categorizationChecker) {
    super(categorizationChecker, BudgetArea.EXTRAS);
  }

  public void checkProjectCreationHidden() {
    categorizationChecker.checkProjectCreationHidden();
  }

  public void checkProjectCreationShown() {
    categorizationChecker.checkProjectCreationShown();
  }

  public void createProject() {
    categorizationChecker.createProject();
  }

  public void editProjectSeries(String seriesName) {
    categorizationChecker.editProject(seriesName);
  }
}
