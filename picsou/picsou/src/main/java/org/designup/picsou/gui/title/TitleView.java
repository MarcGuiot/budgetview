package org.designup.picsou.gui.title;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.description.MonthListStringifier;
import org.designup.picsou.gui.description.MonthRangeFormatter;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;
import java.util.Collections;

public class TitleView extends View implements GlobSelectionListener {

  private JLabel sectionTitle = new JLabel();
  private JLabel periodTitle = new JLabel();
  private Set<Integer> months = Collections.emptySet();
  private Card card = Card.HOME;

  public TitleView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    directory.get(SelectionService.class).addListener(this, Card.TYPE, Month.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("sectionTitle", sectionTitle);
    builder.add("periodTitle", periodTitle);
    updateLabel();
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(Card.TYPE)) {
      GlobList cards = selection.getAll(Card.TYPE);
      if (cards.size() == 1) {
        card = Card.get(cards.get(0).get(Card.ID));
      }
    }
    if (selection.isRelevantForType(Month.TYPE)) {
      months = selection.getAll(Month.TYPE).getValueSet(Month.ID);
    }
    updateLabel();
  }

  private void updateLabel() {
    if (!repository.contains(Transaction.TYPE)) {
      updateText(Lang.get("title.nodata"), "");
      return;
    }
    if (card == null) {
      updateText(Lang.get("title.nocard"), "");
      return;
    }
    if (months.isEmpty()) {
      updateText(Lang.get("title.noperiod"), "");
      return;
    }

    updateText(Lang.get("title.card", card.getLabel()),
               MonthListStringifier.toString(months, MonthRangeFormatter.STANDARD));
  }

  private void updateText(String sectionText, String periodText) {
    sectionTitle.setText(sectionText);
    periodTitle.setText(periodText);
  }

}
