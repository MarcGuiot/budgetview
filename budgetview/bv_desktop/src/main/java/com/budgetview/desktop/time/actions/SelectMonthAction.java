package com.budgetview.desktop.time.actions;

import com.budgetview.desktop.components.dialogs.MonthChooserDialog;
import com.budgetview.model.CurrentMonth;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Ref;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;

public class SelectMonthAction extends AbstractMonthSelectionAction {
  public SelectMonthAction(GlobRepository repository, Directory directory) {
    super(Lang.get("selectMonth.custom"), repository, directory);
  }

  protected Iterable<Integer> getSelection(int currentMonthId) {
    MonthChooserDialog chooser = new MonthChooserDialog(directory.get(JFrame.class), directory);
    chooser.setNoneOptionShown(true);
    final Ref<Integer> selected = new Ref<Integer>();
    chooser.show(CurrentMonth.getCurrentMonth(repository),
                 CurrentMonth.getFirstMonth(repository),
                 CurrentMonth.getLastMonth(repository),
                 Collections.<Integer>emptyList(),
                 new MonthChooserDialog.Callback() {
                   public void processSelection(int monthId) {
                     selected.set(monthId);
                   }

                   public void processNoneSelected() {
                     selected.set(null);
                   }

                 });

    Integer result = selected.get();
    return result == null ? null : Collections.singleton(result);
  }
}
