package org.designup.picsou.gui.series.wizard;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.ConfirmationDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class SeriesWizardDialog {
  private LocalGlobRepository localRepository;
  private Directory directory;
  private SeriesWizardModel model;
  private PicsouDialog dialog;

  public SeriesWizardDialog(GlobRepository parentRepository, Directory directory) {
    this.localRepository =
      LocalGlobRepositoryBuilder.init(parentRepository)
        .copy(Category.TYPE, Series.TYPE, SeriesToCategory.TYPE, Month.TYPE,
              CurrentMonth.TYPE,
              SeriesStat.TYPE, SeriesBudget.TYPE)
        .get();

    SeriesEditionDialog.addSeriesCreationTriggers(localRepository);

    this.directory = directory;
    this.model = new SeriesWizardModel();
    createDialog();
  }

  private void createDialog() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/seriesWizardDialog.splits",
                                                      localRepository, directory);

    builder.addRepeat("budgetAreasRepeat",
                      Arrays.asList(model.getBudgetAreas()),
                      new BudgetAreaRepeatFactory());

    JPanel panel = builder.load();

    dialog = PicsouDialog.createWithButtons(directory.get(JFrame.class), panel,
                                            createOkAction(), createCancelAction(),
                                            directory);
  }

  public void show() {
    GuiUtils.showCentered(dialog);
  }

  private class BudgetAreaRepeatFactory implements RepeatComponentFactory<BudgetArea> {
    public void registerComponents(RepeatCellBuilder cellBuilder, BudgetArea budgetArea) {

      cellBuilder.add("budgetAreaTitle", new JLabel(budgetArea.getLabel()));

      String key = "seriesWizard.description." + budgetArea.getName();
      cellBuilder.add("budgetAreaDescription", new JEditorPane("text/html", Lang.get(key)));

      cellBuilder.addRepeat("entryRepeat",
                            model.getEntries(budgetArea),
                            new EntryRepeatFactory());
    }
  }

  private class EntryRepeatFactory implements RepeatComponentFactory<SeriesWizardEntry> {
    public void registerComponents(RepeatCellBuilder cellBuilder, final SeriesWizardEntry entry) {
      final JCheckBox checkBox = new JCheckBox();
      checkBox.addActionListener(new AbstractAction(entry.getName()) {
        public void actionPerformed(ActionEvent e) {
          entry.setSelected(checkBox.isSelected());
        }
      });
      checkBox.setText(entry.getName());
      cellBuilder.add("checkBox", checkBox);
    }
  }

  private AbstractAction createOkAction() {
    return new AbstractAction(Lang.get("ok")) {
      public void actionPerformed(ActionEvent e) {
        ConfirmationDialog confirm = new ConfirmationDialog("seriesWizard.validation.title",
                                                            "seriesWizard.validation.message",
                                                            dialog, directory) {
          protected void postValidate() {
            model.createSeries(localRepository);
            localRepository.commitChanges(true);
            dialog.setVisible(false);
            directory.get(NavigationService.class).gotoCategorization();
          }
        };
        confirm.show();
      }
    };
  }

  private AbstractAction createCancelAction() {
    return new AbstractAction(Lang.get("cancel")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    };
  }
}
