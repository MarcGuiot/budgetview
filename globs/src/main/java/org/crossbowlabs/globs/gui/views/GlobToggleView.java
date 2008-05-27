package org.crossbowlabs.globs.gui.views;

import org.crossbowlabs.globs.gui.utils.AbstractGlobComponentHolder;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.format.GlobStringifier;
import org.crossbowlabs.globs.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class GlobToggleView extends AbstractGlobComponentHolder {
  private GlobStringifier stringifier;
  private List<JToggleButton> buttons = new ArrayList<JToggleButton>();

  public static GlobToggleView init(GlobType type, GlobList globs, GlobRepository globRepository, Directory directory) {
    return new GlobToggleView(type, globs, globRepository, directory);
  }

  private GlobToggleView(GlobType type, GlobList globs, GlobRepository globRepository, Directory directory) {
    super(type, globRepository, directory);
    this.stringifier = directory.get(DescriptionService.class).getStringifier(type);
    ButtonGroup group = new ButtonGroup();
    for (Glob glob : globs) {
      JToggleButton toggle = new JToggleButton(new SelectionAction(glob));
      group.add(toggle);
      buttons.add(toggle);
    }
  }

  public List<JToggleButton> getButtons() {
    return buttons;
  }

  public JPanel getComponent() {
    JPanel panel = new JPanel(new FlowLayout());
    for (JToggleButton button : buttons) {
      panel.add(button);
    }
    return panel;
  }

  public void dispose() {
  }

  private class SelectionAction extends AbstractAction {
    private Glob glob;

    public SelectionAction(Glob glob) {
      super(stringifier.toString(glob, repository));
      this.glob = glob;
    }

    public void actionPerformed(ActionEvent event) {
      selectionService.select(glob);
    }
  }
}
