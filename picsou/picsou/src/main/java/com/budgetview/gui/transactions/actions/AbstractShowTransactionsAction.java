package com.budgetview.gui.transactions.actions;

import com.budgetview.gui.card.NavigationService;
import com.budgetview.model.Month;
import com.budgetview.model.Transaction;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public abstract class AbstractShowTransactionsAction extends AbstractAction {

  private GlobRepository repository;
  private Directory directory;
  private Set<Integer> monthIds;
  private GlobMatcher matcher;

  public AbstractShowTransactionsAction(String label,
                                        Set<Integer> monthIds, GlobMatcher matcher,
                                        GlobRepository repository, Directory directory) {
    super(label);
    this.monthIds = monthIds;
    this.matcher = matcher;
    this.repository = repository;
    this.directory = directory;
  }

  public final void actionPerformed(ActionEvent actionEvent) {

    GlobList months = repository.getAll(Month.TYPE, fieldIn(Month.ID, monthIds));
    directory.get(SelectionService.class).select(months, Month.TYPE);

    GlobList transactions = repository.getAll(Transaction.TYPE,
                                              and(fieldIn(Transaction.MONTH, monthIds), matcher));
    doShow(transactions, directory.get(NavigationService.class));
  }

  protected abstract void doShow(GlobList transactions, NavigationService navigationService);
}
