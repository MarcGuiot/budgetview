package org.designup.picsou.gui.budget.wizard;

import org.designup.picsou.gui.components.wizard.HelpWizardPage;
import org.designup.picsou.gui.components.wizard.WizardDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class BudgetWizardDialog {
  private WizardDialog wizard;

  public BudgetWizardDialog(GlobRepository repository, Directory directory) {

    HyperlinkHandler handler = new HyperlinkHandler(directory);

    wizard = new WizardDialog(repository, directory);
    wizard.add(new HelpWizardPage("intro",
                                  "budgetWizard.intro",
                                  "budgetWizard/0_budgetWizardIntro.html",
                                  handler, repository, directory));
    wizard.add(new HelpWizardPage("budgetView",
                                  "budgetWizard.budgetView",
                                  "budgetWizard/1_budgetView.html",
                                  handler, repository, directory));
    wizard.add(new HelpWizardPage("setPlannedAmounts",
                                  "budgetWizard.setPlannedAmounts",
                                  "budgetWizard/2_setPlannedAmounts.html",
                                  handler, repository, directory));
    wizard.add(new HelpWizardPage("setPeriodicities",
                                  "budgetWizard.setPeriodicities",
                                  "budgetWizard/3_setPeriodicities.html",
                                  handler, repository, directory));
    wizard.add(new HelpWizardPage("prepareSavings",
                                  "budgetWizard.prepareSavings",
                                  "budgetWizard/4_prepareSavings.html",
                                  handler, repository, directory));
    wizard.add(new HelpWizardPage("prepareProjects",
                                  "budgetWizard.prepareProjects",
                                  "budgetWizard/5_prepareProjects.html",
                                  handler, repository, directory));
    wizard.add(new HelpWizardPage("adjustBudget",
                                  "budgetWizard.adjustBudget",
                                  "budgetWizard/6_adjustBudget.html",
                                  handler, repository, directory));
    wizard.add(new BudgetPositionPage(repository, directory));
    wizard.add(new BudgetThresholdPage(repository, directory));
    wizard.add(new BudgetBalancePage(repository, directory));
  }

  public void show() {
    wizard.show();
  }
}