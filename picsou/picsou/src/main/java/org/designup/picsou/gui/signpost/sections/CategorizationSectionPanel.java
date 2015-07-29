package org.designup.picsou.gui.signpost.sections;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.model.SignpostStatus;
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
