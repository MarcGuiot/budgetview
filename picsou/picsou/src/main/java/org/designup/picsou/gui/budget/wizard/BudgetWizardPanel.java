package org.designup.picsou.gui.budget.wizard;

import org.designup.picsou.gui.components.wizard.HelpWizardPage;
import org.designup.picsou.gui.components.wizard.WizardPanel;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.UserPreferences;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Functor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public class BudgetWizardPanel implements ChangeSetListener {
  private WizardPanel wizard;
  private GlobRepository repository;
  private Directory directory;
  private HyperlinkHandler handler;

  public BudgetWizardPanel(final GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.handler = new HyperlinkHandler(directory);
    this.repository.addChangeListener(this);

    wizard = new WizardPanel(repository, directory, new Functor() {
      public void run() throws Exception {
        repository.update(UserPreferences.KEY, UserPreferences.SHOW_BUDGET_VIEW_WIZARD, false);
      }
    });
    wizard.add(newHelpPage("intro", false));
    wizard.add(newHelpPage("budgetView", true));
    wizard.add(newHelpPage("setPlannedAmounts", true));
    wizard.add(newHelpPage("setPeriodicities", true));
    wizard.add(newHelpPage("prepareSavings", true));
    wizard.add(newHelpPage("prepareProjects", true));
    wizard.add(newHelpPage("endOfCurrentMonth", true));
    wizard.add(newHelpPage("future", true));

  }

  public JPanel getPanel() {
    return wizard.getPanel();
  }

  private HelpWizardPage newHelpPage(String id, boolean showHelp) {
    return new HelpWizardPage(id,
                              "budgetWizard." + id,
                              showHelp ? "budgetWizard." + id : null,
                              handler, repository, directory);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(UserPreferences.TYPE) ||
        changeSet.containsChanges(Series.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(UserPreferences.TYPE) ||
        changedTypes.contains(Series.TYPE)) {
      update();
    }
  }

  private void update() {
    Glob prefs = repository.find(UserPreferences.KEY);
    if (prefs == null){
      return;
    }
    boolean showWizard =
      prefs.isTrue(UserPreferences.SHOW_BUDGET_VIEW_WIZARD)
      && repository.contains(Series.TYPE, not(fieldEquals(Series.ID, Series.UNCATEGORIZED_SERIES_ID)));
    int page = prefs.get(UserPreferences.CURRENT_WIZARD_PAGE);
    wizard.show(true || showWizard, page);
  }
}