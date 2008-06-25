package org.globsframework.gui.views;

import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.ComponentHolder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class GlobToggleView extends AbstractGlobComponentHolder<GlobToggleView> {
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
