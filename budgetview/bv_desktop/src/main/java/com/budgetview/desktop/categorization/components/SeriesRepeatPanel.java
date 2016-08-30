package com.budgetview.desktop.categorization.components;

import com.budgetview.desktop.categorization.utils.CategorizationFilter;
import com.budgetview.desktop.categorization.utils.CategorizationMatchers;
import com.budgetview.desktop.categorization.utils.FilteredRepeats;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesGroup;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.utils.Functor;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SeriesRepeatPanel implements Disposable {
  private final BudgetArea budgetArea;
  private Glob group;
  private FilteredRepeats seriesRepeat;
  private final GlobRepository repository;
  private final Directory directory;
  private GlobsPanelBuilder builder;
  private FilteredRepeats.Handler handler;
  private JPanel panel;
  private ChangeSetListener listener;

  public SeriesRepeatPanel(BudgetArea budgetArea, Glob group, FilteredRepeats seriesRepeat,
                           GlobList transactions, GlobRepository repository, Directory directory) {
    this.budgetArea = budgetArea;
    this.group = group;
    this.seriesRepeat = seriesRepeat;
    this.repository = repository;
    this.directory = directory;
    createPanel(transactions);
  }

  private void createPanel(GlobList transactions) {

    builder = new GlobsPanelBuilder(getClass(), "/layout/categorization/seriesRepeatPanel.splits",
                                    this.repository, directory);

    CategorizationFilter innerFilter = CategorizationMatchers.seriesCategorizationFilter(budgetArea);
    innerFilter.filterForTransactions(transactions);
    GroupMatcher groupMatcher = new GroupMatcher(group, innerFilter);

    handler = seriesRepeat.addRepeat(budgetArea, builder, groupMatcher);
    panel = builder.load();

    listener = new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsUpdates(Series.GROUP) && (handler != null)) {
          handler.refresh();
        }
      }
    };
    repository.addChangeListener(listener);
  }

  public JPanel getPanel() {
    return panel;
  }

  public void addAutoHide(final JComponent component) {
    handler.addListener(new Functor() {
      public void run() throws Exception {
        component.setVisible(!handler.isEmpty());
      }
    });
  }

  public void dispose() {
    if (listener != null) {
      repository.removeChangeListener(listener);
      listener = null;
    }
    if (builder != null) {
      builder.dispose();
      builder = null;
    }
    if (handler != null) {
      handler.dispose();
      handler = null;
    }
  }

  private class GroupMatcher implements CategorizationFilter {
    private CategorizationFilter innerFilter;
    private final Integer groupId;

    public GroupMatcher(Glob group, CategorizationFilter innerFilter) {
      this.groupId = group == null ? null : group.get(SeriesGroup.ID);
      this.innerFilter = innerFilter;
    }

    public void filterForTransactions(GlobList transactions) {
      innerFilter.filterForTransactions(transactions);
    }

    public boolean matches(Glob series, GlobRepository repository) {
      return Utils.equal(groupId, series.get(Series.GROUP)) && innerFilter.matches(series, repository);
    }

    public String toString() {
      return "GroupMatcher(" + groupId + ", " + innerFilter + ")";
    }
  }
}
