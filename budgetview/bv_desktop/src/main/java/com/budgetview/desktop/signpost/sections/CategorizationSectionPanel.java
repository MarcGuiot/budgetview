package com.budgetview.desktop.signpost.sections;

import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.model.Card;
import com.budgetview.model.SignpostStatus;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CategorizationSectionPanel extends SignpostSectionPanel {
  public CategorizationSectionPanel(GlobRepository repository, Directory directory) {
    super(SignpostSection.CATEGORIZATION, repository, directory);
  }

  protected boolean isCompleted(GlobRepository repository) {
    return SignpostStatus.isCompleted(SignpostStatus.GOTO_BUDGET_SHOWN, repository);
  }

  protected AbstractAction getAction(final Directory directory) {
    return new AbstractAction(SignpostSection.CATEGORIZATION.getLabel()) {
      public void actionPerformed(ActionEvent actionEvent) {
        directory.get(NavigationService.class).gotoCard(Card.CATEGORIZATION);
      }
    };
  }
}
