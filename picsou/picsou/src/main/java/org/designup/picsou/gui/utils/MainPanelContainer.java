package org.designup.picsou.gui.utils;

import org.designup.picsou.gui.model.Card;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class MainPanelContainer extends JPanel implements GlobSelectionListener {

  private MainPanelLayout layout;

  public MainPanelContainer(Directory directory) {
    directory.get(SelectionService.class).addListener(this, Card.TYPE);
  }

  public void setLayout(LayoutManager mgr) {
    if (layout == null) {
      layout = new MainPanelLayout();
      super.setLayout(layout);
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    Glob currentCard = selection.getAll(Card.TYPE).getFirst();
    if (currentCard != null) {
      layout.setCard(Card.get(currentCard));
      invalidate();
    }
  }
}
