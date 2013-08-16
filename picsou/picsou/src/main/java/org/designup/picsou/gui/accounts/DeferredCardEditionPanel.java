package org.designup.picsou.gui.accounts;

import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobComboEditor;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.GlobRepository;
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

  public DeferredCardEditionPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
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
    return builder.load();
  }

  public void setVisible(boolean visible) {
    getPanel().setVisible(visible);
    deferredDay.getComponent().setVisible(visible);
    deferredDebitDay.getComponent().setVisible(visible);
    deferredMonthShift.getComponent().setVisible(visible);
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
