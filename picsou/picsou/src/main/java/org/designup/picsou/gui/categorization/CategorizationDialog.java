package org.designup.picsou.gui.categorization;

import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatFactory;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.gui.components.PicsouDialog;

import javax.swing.*;
import java.awt.*;

public class CategorizationDialog {
  protected JPanel panel;

  public CategorizationDialog(final GlobRepository repository, Directory directory) {

    DescriptionService descriptionService = directory.get(DescriptionService.class);
    final GlobStringifier budgetAreaStringifier = descriptionService.getStringifier(BudgetArea.TYPE);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/categorizationDialog.splits",
                                                      repository, directory);
    builder.addRepeat("budgetAreas", new RepeatFactory<Glob>() {
      public void register(RepeatCellBuilder cellBuilder, Glob budgetArea) {
        String name = budgetAreaStringifier.toString(budgetArea,  repository);
        cellBuilder.add("budgetAreaToggle", new JToggleButton(name));
      }
    }, BudgetArea.TYPE.getConstants());

    panel = builder.load();
  }

  public void show(GlobList transactions, Window parent) {
    PicsouDialog dialog = PicsouDialog.create(parent);
    dialog.setContentPane(panel);
    GuiUtils.showCentered(dialog);
  }
}
