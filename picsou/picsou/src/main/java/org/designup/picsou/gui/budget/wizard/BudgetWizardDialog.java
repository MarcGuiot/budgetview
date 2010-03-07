package org.designup.picsou.gui.budget.wizard;

import org.designup.picsou.gui.components.wizard.HelpWizardPage;
import org.designup.picsou.gui.components.wizard.WizardDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class BudgetWizardDialog {
  private WizardDialog wizard;
  private GlobRepository repository;
  private Directory directory;
  private HyperlinkHandler handler;

  public BudgetWizardDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.handler = new HyperlinkHandler(directory);

    wizard = new WizardDialog(repository, directory);
    wizard.add(newHelpPage("intro", "00_budgetWizardIntro.html"));
    wizard.add(newHelpPage("budgetView", "01_budgetView.html"));
    wizard.add(newHelpPage("setPlannedAmounts", "02_setPlannedAmounts.html"));
    wizard.add(newHelpPage("setPeriodicities", "03_setPeriodicities.html"));
    wizard.add(newHelpPage("prepareSavings", "04_prepareSavings.html"));
    wizard.add(newHelpPage("prepareProjects", "05_prepareProjects.html"));
    wizard.add(new BudgetPositionPage(repository, directory));
    wizard.add(new BudgetThresholdPage(repository, directory));
    wizard.add(newHelpPage("endOfCurrentMonth", "09_endOfCurrentMonth.html"));
    wizard.add(newHelpPage("future", "10_future.html"));
    wizard.add(new BudgetBalancePage(repository, directory));
  }

  private HelpWizardPage newHelpPage(String id, String filePath) {
    return new HelpWizardPage(id,
                              "budgetWizard." + id,
                              "budgetWizard/" + filePath,
                              handler, repository, directory);
  }

  public void show() {
    wizard.show();
  }
}