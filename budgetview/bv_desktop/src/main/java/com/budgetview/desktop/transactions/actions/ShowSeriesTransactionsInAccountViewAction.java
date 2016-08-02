package com.budgetview.desktop.transactions.actions;

import com.budgetview.desktop.card.NavigationService;
import com.budgetview.utils.Lang;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class ShowSeriesTransactionsInAccountViewAction extends AbstractAction {

  private Set<Integer> seriesIds;
  private Directory directory;

  public ShowSeriesTransactionsInAccountViewAction(Set<Integer> seriesIds, Directory directory) {
    super(Lang.get("showTransactionsInAccountViewAction.text"));
    this.seriesIds = seriesIds;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    directory.get(NavigationService.class).gotoDataForSeries(seriesIds);
  }
}
