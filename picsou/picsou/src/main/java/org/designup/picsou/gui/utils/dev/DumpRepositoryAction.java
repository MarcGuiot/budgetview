package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.model.*;
import org.designup.picsou.triggers.SeriesBudgetTrigger;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobMatchers;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DumpRepositoryAction extends AbstractAction {

  private GlobRepository repository;

  public DumpRepositoryAction(GlobRepository repository) {
    super("[Dump repository]");
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    GlobPrinter.print(repository, Series.TYPE, SeriesGroup.TYPE, Project.TYPE, ProjectItem.TYPE);
  }
}
