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

  public void activateProjects() {
    enable(AddOns.PROJECTS);
  }

  public void activateAnalysis() {
    enable(AddOns.ANALYSIS);
  }

  private void enable(BooleanField field) {
    if (repository == null) {
      repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();
    }
    repository.update(AddOns.KEY, field, true);
  }
}
