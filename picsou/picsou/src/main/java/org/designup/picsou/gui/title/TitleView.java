package org.designup.picsou.gui.title;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class TitleView extends View implements GlobSelectionListener {

  private JLabel label = new JLabel();
  private Integer[] months = {};
  private Card card = Card.HOME;

  public TitleView(GlobRepository globRepository, Directory directory) {
    super(globRepository, directory);
    directory.get(SelectionService.class).addListener(this, Card.TYPE, Month.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("title", label);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Card.TYPE)) {
      GlobList cards = selection.getAll(Card.TYPE);
      if (cards.size() == 1) {
        card = Card.get(cards.get(0).get(Card.ID));
      }
    }
    if (selection.isRelevantForType(Month.TYPE)) {
      months = selection.getAll(Month.TYPE).getSortedArray(Month.ID);
    }
    updateLabel();
  }

  private void updateLabel() {
    if (card == null) {
      label.setText(Lang.get("title.nocard"));
      return;
    }
    if (months.length == 0) {
      label.setText(Lang.get("title.noperiod"));
      return;
    }
    String monthDesc = getMonthDesc();

    label.setText(card.getLabel() + (monthDesc.length() > 0 ? (" - " + monthDesc) : ""));
  }

  private String getMonthDesc() {
    if (months.length == 1) {
      return Month.getLabel(months[0]).toLowerCase();
    }
    return "";
  }
}
