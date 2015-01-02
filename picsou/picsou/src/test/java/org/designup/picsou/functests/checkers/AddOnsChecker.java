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
    enable(AddOns.PROJECTS);
  }

  public void activateAnalysis() {
    enable(AddOns.ANALYSIS);
  }

  public void activateGroups() {
    enable(AddOns.GROUPS);
  }

  private void enable(BooleanField field) {
    AddOns.enable(field, getRepository());
  }

  private GlobRepository getRepository() {
    if (repository == null) {
      repository = ((PicsouFrame) mainWindow.getAwtComponent()).getRepository();
    }
    return repository;
  }
}
