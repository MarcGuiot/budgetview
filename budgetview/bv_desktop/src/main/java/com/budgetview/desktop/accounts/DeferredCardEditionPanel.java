package com.budgetview.desktop.accounts;

import com.budgetview.desktop.accounts.utils.AccountMatchers;
import com.budgetview.model.Account;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobComboEditor;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class DeferredCardEditionPanel implements Disposable {

  private GlobRepository repository;
  private Directory directory;

  private GlobLinkComboEditor deferredDebitDay;
  private GlobLinkComboEditor deferredDay;
  private GlobComboEditor deferredMonthShift;

  private JPanel panel;
  private GlobsPanelBuilder builder;
  private GlobLinkComboEditor deferredTargetAccount;
  private GlobMatcher accountFilter;

  public DeferredCardEditionPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public static GlobMatcher notInSelection(final GlobType type, final SelectionService selectionService){
    return new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        GlobList currentSelection = selectionService.getSelection(type);
        return !currentSelection.getKeySet().contains(item.getKey());
      }
    };
  }

  public JPanel getPanel() {
    if (panel == null) {
      panel = createPanel();
    }
    return panel;
  }

  private JPanel createPanel() {
    builder = new GlobsPanelBuilder(getClass(),
                                    "/layout/accounts/deferredCardEditionPanel.splits",
                                    repository, directory);
    deferredDay = builder.addComboEditor("deferredDay", Account.DEFERRED_DAY);
    deferredDebitDay = builder.addComboEditor("deferredDebitDay", Account.DEFERRED_DEBIT_DAY);
    deferredMonthShift =
      builder
        .addComboEditor("deferredMonthShift", Account.DEFERRED_MONTH_SHIFT, new int[]{0, 1, 2, 3})
        .setRenderer(new MonthShiftRenderer());
    accountFilter = GlobMatchers.and(AccountMatchers.userCreatedMainAccounts(),
                                     notInSelection(Account.TYPE, directory.get(SelectionService.class)));
    deferredTargetAccount = builder.addComboEditor("deferredTargetAccount", Account.DEFERRED_TARGET_ACCOUNT)
      .setFilter(accountFilter);
    return builder.load();
  }

  public void setVisible(boolean visible) {
    if (visible) {
      deferredTargetAccount.setFilter(accountFilter);
    }
    getPanel().setVisible(visible);
    deferredDay.setVisible(visible);
    deferredDebitDay.setVisible(visible);
    deferredMonthShift.setVisible(visible);
    deferredTargetAccount.setVisible(visible);
  }

  public void dispose() {
    builder.dispose();
  }

  private class MonthShiftRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList jList, Object value, int i, boolean b, boolean b1) {
      Integer shift = (Integer)value;
      if (shift == null) {
        setText("");
        return this;
      }

      setText(Lang.get("account.deferred.monthShift." + shift));
      return this;
    }
  }
}
