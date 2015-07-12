package org.designup.picsou.gui.categorization.utils;

import org.designup.picsou.gui.categorization.components.SeriesChooserComponentFactory;
import org.designup.picsou.gui.description.stringifiers.SeriesNameComparator;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.components.GlobRepeatListener;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Functor;
import org.globsframework.utils.collections.Pair;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class FilteredRepeats {

  private GlobRepository repository;
  private Directory directory;
  private Map<BudgetArea, JRadioButton> unselectRadios = new HashMap<BudgetArea, JRadioButton>();
  private Map<BudgetArea, ButtonGroup> buttonGroups = new HashMap<BudgetArea, ButtonGroup>();
  private int currentId = 0;

  java.util.Map<Integer, Pair<CategorizationFilter, GlobRepeat>> list =
    new HashMap<Integer, Pair<CategorizationFilter, GlobRepeat>>();

  public interface Handler extends Disposable {
    boolean isEmpty();

    void addListener(Functor functor);

    void refresh();
  }

  public FilteredRepeats(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public Handler addRepeat(BudgetArea budgetArea, PanelBuilder builder, CategorizationFilter filter) {

    final GlobRepeat repeat =
      GlobsPanelBuilder.addRepeat("seriesRepeat", Series.TYPE, filter,
                                  new SeriesNameComparator(), repository, builder,
                                  new SeriesChooserComponentFactory(budgetArea,
                                                                    getUnselectRadio(budgetArea), getButtonGroup(budgetArea),
                                                                    repository, directory));
    final int filterId = add(filter, repeat);
    return new Handler() {
      private GlobRepeatListener listener;

      public boolean isEmpty() {
        return repeat.isEmpty();
      }

      public void addListener(final Functor functor) {
        listener = new GlobRepeatListener() {
          public void listChanged(GlobList currentList) {
            try {
              functor.run();
            }
            catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        };
        repeat.addListener(listener);
      }

      public void refresh() {
        repeat.refresh();
      }

      public void dispose() {
        list.remove(filterId);
        if (listener != null) {
          repeat.removeListener(listener);
          listener = null;
        }
      }
    };
  }

  public ButtonGroup getButtonGroup(BudgetArea budgetArea) {
    ButtonGroup group = buttonGroups.get(budgetArea);
    if (group == null) {
      group = new ButtonGroup();
      buttonGroups.put(budgetArea, group);
    }
    return group;
  }

  private JRadioButton getUnselectRadio(BudgetArea budgetArea) {
    JRadioButton radio = unselectRadios.get(budgetArea);
    if (radio == null) {
      radio = new JRadioButton("invisibleRadio");
      unselectRadios.put(budgetArea, radio);
    }
    return radio;
  }

  public void update(GlobList currentTransactions) {
    for (Pair<CategorizationFilter, GlobRepeat> filter : list.values()) {
      filter.getFirst().filterDates(currentTransactions);
      filter.getSecond().setFilter(filter.getFirst());
    }
  }

  public boolean updateAndCheckMatch(Glob series, Glob transaction) {
    boolean noneMatch = false;
    for (Pair<CategorizationFilter, GlobRepeat> filter : list.values()) {
      CategorizationFilter categorizationFilter = filter.getFirst();
      GlobRepeat repeat = filter.getSecond();

      categorizationFilter.filterDates(new GlobList(transaction));
      repeat.setFilter(categorizationFilter);
      noneMatch |= categorizationFilter.matches(series, repository);
    }
    return !noneMatch;
  }

  private int add(CategorizationFilter filter, GlobRepeat repeat) {
    int id = currentId++;
    list.put(id, new Pair<CategorizationFilter, GlobRepeat>(filter, repeat));
    return id;
  }
}
