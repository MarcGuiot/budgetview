package com.budgetview.desktop.utils;

import com.budgetview.desktop.model.Card;
import com.budgetview.model.SignpostStatus;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.ReplicationGlobRepository;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class MainPanelContainer extends JPanel implements GlobSelectionListener {

  private MainPanelLayout layout;
  private GlobRepository repository;
  private SelectionService selectionService;

  public MainPanelContainer(ReplicationGlobRepository repository, Directory directory) {
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
    directory.get(SelectionService.class).addListener(this, Card.TYPE);
    repository.addChangeListener(new KeyChangeListener(SignpostStatus.KEY) {
      public void update() {
        updateLayout();
      }
    });
  }

  public void reset(boolean initCompleted) {
    if (layout != null) {
      layout.setCard(Card.HOME, initCompleted);
    }
  }

  public void setLayout(LayoutManager mgr) {
    if (layout == null) {
      layout = new MainPanelLayout();
      super.setLayout(layout);
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    updateLayout();
  }

  private void updateLayout() {
    if (layout != null) {
      Glob currentCard = selectionService.getSelection(Card.TYPE).getFirst();
      if (currentCard != null) {
        layout.setCard(Card.get(currentCard), SignpostStatus.isOnboardingCompleted(repository));
        invalidate();
      }
    }
  }
}
