package org.designup.picsou.gui.budget.summary;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.PeriodAccountStat;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.SortedSet;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class UncategorizedButton implements ChangeSetListener, Disposable {

  private final Integer accountId;
  private final GlobRepository repository;
  private final Directory directory;
  private JButton button = new JButton();

  public static void create(Key key, PanelBuilder builder, String componentName, GlobRepository repository, Directory directory) {
    UncategorizedButton button = new UncategorizedButton(key, repository, directory);
    builder.add(componentName, button.getComponent());
    builder.addDisposable(button);
  }

  private UncategorizedButton(Key accountKey, GlobRepository repository, Directory directory) {
    this.accountId = accountKey.get(Account.ID);
    this.repository = repository;
    this.directory = directory;
    repository.addChangeListener(this);
    repository.addChangeListener(this);
    button.addActionListener(new GotoUncategorizedAction());
    update();
  }

  public JButton getComponent() {
    return button;
  }

  public void update() {

    Glob stat = repository.find(Key.create(PeriodAccountStat.TYPE, accountId));
    int uncategorized = stat != null ? stat.get(PeriodAccountStat.UNCATEGORIZED_COUNT) : 0;
    button.setText(Integer.toString(uncategorized));
    button.setVisible(uncategorized > 0);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(PeriodAccountStat.TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(PeriodAccountStat.TYPE)) {
      update();
    }
  }

  public void dispose() {
    repository.removeChangeListener(this);
  }

  private class GotoUncategorizedAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      directory.get(NavigationService.class).gotoUncategorizedForSelectedMonths();
    }
  }
}

