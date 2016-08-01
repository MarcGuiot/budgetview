package com.budgetview.gui.title;

import com.budgetview.gui.model.Card;
import com.budgetview.gui.View;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class TitleView extends View implements GlobSelectionListener {

  private JLabel sectionTitle = new JLabel();
  private Card card = Card.HOME;

  public TitleView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    directory.get(SelectionService.class).addListener(this, Card.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("sectionTitle", sectionTitle);
    updateLabel();
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Card.TYPE)) {
      GlobList cards = selection.getAll(Card.TYPE);
      if (cards.size() == 1) {
        card = Card.get(cards.get(0).get(Card.ID));
      }
    }
    updateLabel();
  }

  private void updateLabel() {
    if (card == null) {
      updateText(Lang.get("title.nocard"));
      return;
    }
    updateText(Lang.get("title.card", card.getLabel()));
  }

  private void updateText(String sectionText) {
    sectionTitle.setText(sectionText);
  }
}
