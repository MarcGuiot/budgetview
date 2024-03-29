package com.budgetview.desktop.series.edition;

import com.budgetview.desktop.components.dialogs.CloseDialogAction;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.model.SubSeries;
import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class DeleteSubSeriesDialog {

  private GlobRepository repository;
  private Directory directory;
  private GlobList subSeriesList;

  private PicsouDialog dialog;
  private JComboBox combo;
  private GlobsPanelBuilder builder;

  public DeleteSubSeriesDialog(GlobList subSeriesList,
                               GlobRepository repository,
                               Directory directory,
                               JDialog owner) {
    this.repository = repository;
    this.directory = directory;
    this.subSeriesList = subSeriesList;
    createDialog(owner);
  }

  private void createDialog(Window owner) {
    builder = new GlobsPanelBuilder(getClass(), "/layout/series/deleteSubSeriesDialog.splits",
                                                      repository, directory);

    combo = builder.add("combo", new JComboBox(getFunctors())).getComponent();

    JPanel panel = builder.load();

    dialog = PicsouDialog.create(this, owner, true, directory);
    dialog.addPanelWithButtons(panel, new ValidateAction(), new CloseDialogAction(dialog));
  }

  public void show() {
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
  }

  public GlobFunctor[] getFunctors() {
    java.util.List<GlobFunctor> functors = new ArrayList<GlobFunctor>();

    Glob series = repository.findLinkTarget(subSeriesList.getFirst(), SubSeries.SERIES);
    functors.add(DeleteSubSeriesFunctors.moveToSeries(series));

    Set<Integer> subSeriesIds = subSeriesList.getValueSet(SubSeries.ID);
    for (Glob subSeries : repository.findLinkedTo(series, SubSeries.SERIES).sort(SubSeries.NAME)) {
      if (!subSeriesIds.contains(subSeries.get(SubSeries.ID))) {
        functors.add(DeleteSubSeriesFunctors.moveToSubSeries(subSeries));
      }
    }

    functors.add(DeleteSubSeriesFunctors.uncategorize());

    return functors.toArray(new GlobFunctor[functors.size()]);
  }

  private class ValidateAction extends AbstractAction {

    private ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      GlobFunctor functor = (GlobFunctor)combo.getSelectedItem();
      repository.startChangeSet();
      try {
        repository.getAll(Transaction.TYPE,
                          fieldIn(Transaction.SUB_SERIES, subSeriesList.getValueSet(SubSeries.ID)))
          .safeApply(functor, repository);
        repository.delete(subSeriesList);
      }
      finally {
        repository.completeChangeSet();
      }
      dialog.setVisible(false);
    }
  }
}
