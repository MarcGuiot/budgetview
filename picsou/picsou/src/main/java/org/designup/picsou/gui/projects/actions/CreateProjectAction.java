package org.designup.picsou.gui.projects.actions;

import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CreateProjectAction extends AbstractAction {

  private SeriesEditor seriesEditor;

  public CreateProjectAction(Directory directory) {
    super(Lang.get("projectView.create"));
    this.seriesEditor = directory.get(SeriesEditor.class);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    seriesEditor.showNewProject();
  }
}
