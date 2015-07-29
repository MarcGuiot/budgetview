package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.model.SignpostStatus;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class BudgetSectionPanel extends SignpostSectionPanel {
  public BudgetSectionPanel(GlobRepository repository, Directory directory) {
    super(SignpostSection.BUDGET, repository, directory);
  }

  protected boolean isCompleted(GlobRepository repository) {
    return SignpostStatus.isCompleted(SignpostStatus.GOTO_BUDGET_DONE, repository)
           && SignpostStatus.isCompleted(SignpostStatus.SERIES_AMOUNT_DONE, repository)
           && repository.contains(PeriodSeriesStat.TYPE)
           && !repository.contains(PeriodSeriesStat.TYPE, fieldEquals(PeriodSeriesStat.TO_SET, true));
  }

  protected AbstractAction getAction(final Directory directory) {
    return new AbstractAction(SignpostSection.BUDGET.getLabel()) {
      public void actionPerformed(ActionEvent actionEvent) {
        directory.get(NavigationService.class).gotoCard(Card.BUDGET);
      }
    };
  }

}
