package org.designup.picsou.gui.categorization.utils;

import org.designup.picsou.gui.categorization.components.SeriesChooserComponentFactory;
import org.designup.picsou.gui.description.stringifiers.SeriesNameComparator;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.components.GlobRepeatListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Functor;
import org.globsframework.utils.collections.Pair;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class FilteredRepeats {

  private GlobRepository repository;
  private Directory directory;

  java.util.List<Pair<Matchers.CategorizationFilter, GlobRepeat>> list =
    new ArrayList<Pair<Matchers.CategorizationFilter, GlobRepeat>>();

  public interface Handler {
    boolean isEmpty();

    void addListener(Functor functor);
  }

  public FilteredRepeats(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public Handler addRepeat(BudgetArea budgetArea, GlobsPanelBuilder builder, Matchers.CategorizationFilter filter) {

    JRadioButton invisibleRadio = new JRadioButton("invisibleRadio");
    builder.add("invisibleRadio", invisibleRadio);

    final GlobRepeat repeat = builder.addRepeat("seriesRepeat",
                                                Series.TYPE,
                                                filter,
                                                SeriesNameComparator.INSTANCE,
                                                new SeriesChooserComponentFactory(budgetArea, invisibleRadio, repository, directory));
    add(filter, repeat);
    return new Handler() {
      public boolean isEmpty() {
        return repeat.isEmpty();
      }

      public void addListener(final Functor functor) {
        repeat.addListener(new GlobRepeatListener() {
          public void listChanged(GlobList currentList) {
            try {
              functor.run();
            }
            catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        });
      }
    };
  }

  public void update(Set<Integer> months, GlobList currentTransactions) {
    for (Pair<Matchers.CategorizationFilter, GlobRepeat> filter : list) {
      filter.getFirst().filterDates(months, currentTransactions);
      filter.getSecond().setFilter(filter.getFirst());
    }
  }

  public boolean updateAndCheckMatch(Glob series, Glob transaction) {
    boolean noneMatch = false;
    for (Pair<Matchers.CategorizationFilter, GlobRepeat> filter : list) {
      filter.getFirst().filterDates(Collections.singleton(transaction.get(Transaction.BUDGET_MONTH)),
                                    Collections.singletonList(transaction));
      filter.getSecond().setFilter(filter.getFirst());
      noneMatch |= filter.getFirst().matches(series, repository);
    }
    return !noneMatch;
  }

  private void add(Matchers.CategorizationFilter filter, GlobRepeat repeat) {
    list.add(new Pair<Matchers.CategorizationFilter, GlobRepeat>(filter, repeat));
  }
}
