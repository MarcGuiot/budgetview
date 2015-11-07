package com.budgetview.analytics.gui;

import com.budgetview.analytics.model.User;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import static org.globsframework.model.utils.GlobMatchers.*;

public class UserTableFilter {

  private enum Filter {
    ALL("All", GlobMatchers.ALL),
    ACTIVE_WITH_MAIL("Active with email", and(isTrue(User.ACTIVATED),
                                              not(isTrue(User.LOST)),
                                              isNotNull(User.EMAIL))),
    RETAINED_AND_LOST("Retained and lost", and(isTrue(User.RETAINED),
                                               isTrue(User.LOST)));

    private final String label;
    private final GlobMatcher matcher;

    Filter(String label, GlobMatcher matcher) {
      this.label = label;
      this.matcher = matcher;
    }

    public String toString() {
      return label;
    }
  }

  public static JComboBox createCombo(final GlobTableView usersTable) {
    JComboBox combo = new JComboBox(Filter.values());
    combo.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent itemEvent) {
        usersTable.setFilter(((Filter) itemEvent.getItem()).matcher);
      }
    });
    return combo;
  }
}
