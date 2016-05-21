package com.budgetview.gui.categorization.actions;

import com.budgetview.gui.card.NavigationService;
import com.budgetview.model.Month;
import com.budgetview.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class ShowTransactionsToCategorizeAction extends AbstractAction {

  private Set<Integer> monthIds;
  private GlobRepository repository;
  private Directory directory;

  public ShowTransactionsToCategorizeAction(Set<Integer> monthIds, GlobRepository repository, Directory directory) {
    super(Lang.get("showTransactionsInCategorizationViewAction.text"));
    this.monthIds = monthIds;
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    directory.get(SelectionService.class)
      .select(repository.getAll(Month.TYPE, fieldIn(Month.ID, monthIds)), Month.TYPE);
    directory.get(NavigationService.class)
      .gotoUncategorizedForSelectedMonths();
  }
}
