package org.globsframework.gui.components;

import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GlobUnselectPanel  {
  private JPanel panel;
  private SelectionService selectionService;

  public static GlobUnselectPanel init(GlobType type, Directory directory) {
    return new GlobUnselectPanel(type, directory);
  }

  private GlobUnselectPanel(final GlobType type, Directory directory) {
    this.selectionService = directory.get(SelectionService.class);
    this.panel = new JPanel();
    this.panel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent mouseEvent) {
        selectionService.clear(type);
      }
    });
  }

  public JPanel getComponent() {
    return panel;
  }
}
