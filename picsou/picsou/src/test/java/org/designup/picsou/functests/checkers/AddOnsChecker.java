package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.model.AddOns;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.GlobRepository;
import org.uispec4j.Window;

public class AddOnsChecker extends ViewChecker {
  private GlobRepository repository;

  public AddOnsChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void checkRegistered() {
    ViewSelectionChecker views = new ViewSelectionChecker(mainWindow);
    views.checkProjectsEnabled();
    views.checkAnalysisEnabled();
  }

  public void checkNotRegistered() {
    ViewSelectionChecker views = new ViewSelectionChecker(mainWindow);
    views.checkProjectsDisabled();
    views.checkAnalysisDisabled();
  }

  public void activateAll() {
    AddOns.setAllEnabled(getRepository(), true);
  }

  public void activateProjects() {
    setEnabled(AddOns.PROJECTS, true);
  }

  public void disableProjects() {
    setEnabled(AddOns.PROJECTS, false);
  }

  public void activateAnalysis() {
    setEnabled(AddOns.ANALYSIS, true);
  }

  public void disableAnalysis() {
    setEnabled(AddOns.ANALYSIS, false);
  }

  public void activateGroups() {
    setEnabled(AddOns.GROUPS, true);
  }

  private void setEnabled(BooleanField field, boolean enabled) {
    AddOns.setEnabled(field, getRepository(), enabled);
  }

  private GlobRepository getRepository() {
    if (repository == null) {
      repository = ((PicsouFrame) mainWindow.getAwtComponent()).getRepository();
    }
    return repository;
  }
}
