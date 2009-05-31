package org.designup.picsou.gui.series.wizard;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.ConfirmationDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.ProfileTypeSeriesTrigger;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Set;

public class SeriesWizardDialog {
  private LocalGlobRepository localRepository;
  private Directory directory;
  private SeriesWizardModel model;
  private PicsouDialog dialog;
  private JScrollPane scrollPane;
  private GlobRepository parentRepository;

  public SeriesWizardDialog(GlobRepository parentRepository, Directory directory) {
    this.parentRepository = parentRepository;

    this.localRepository =
      LocalGlobRepositoryBuilder.init(parentRepository)
        .copy(Series.TYPE, SubSeries.TYPE, Month.TYPE,
              CurrentMonth.TYPE,
              SeriesStat.TYPE, SeriesBudget.TYPE)
        .get();

    SeriesEditionDialog.addSeriesCreationTriggers(localRepository, ProfileTypeSeriesTrigger.NULL);

    this.directory = directory;
    this.model = new SeriesWizardModel(localRepository);
    createDialog();
  }

  private void createDialog() {
    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/seriesWizardDialog.splits",
                                                      localRepository, directory);

    builder.addRepeat("budgetAreasRepeat",
                      Arrays.asList(model.getBudgetAreas()),
                      new BudgetAreaRepeatFactory());

    scrollPane = builder.add("scrollPane", new JScrollPane());

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory, dialog));

    JPanel panel = builder.load();

    dialog.addPanelWithButtons(panel, createOkAction(), createCancelAction());
  }

  public void show() {
    dialog.pack();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        scrollToTop();
      }
    });
    GuiUtils.showCentered(dialog);
  }

  private void scrollToTop() {
    scrollPane.getViewport().setViewPosition(new Point(0, 0));
    scrollPane.getVerticalScrollBar().setValue(0);
    scrollPane.getViewport().scrollRectToVisible(new Rectangle(1, 1, 1, 1));
    scrollPane.scrollRectToVisible(new Rectangle(1, 1, 1, 1));
  }

  public Window getDialog() {
    return dialog;
  }

  private class BudgetAreaRepeatFactory implements RepeatComponentFactory<BudgetArea> {
    public void registerComponents(RepeatCellBuilder cellBuilder, BudgetArea budgetArea) {

      cellBuilder.add("budgetAreaTitle", new JLabel(budgetArea.getLabel()));

      String key = "seriesWizard.description." + budgetArea.getName();
      cellBuilder.add("budgetAreaDescription", new JTextArea(Lang.get(key)));

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
      checkBox.setSelected(entry.isSelected());
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

            GlobList list = getAllMonths();
            directory.get(NavigationService.class).gotoCategorization();

            dialog.setVisible(false);
          }
        };
        confirm.show();
      }
    };
  }

  private GlobList getAllMonths() {
    Set<Integer> valueSet = parentRepository.getAll(Transaction.TYPE).getValueSet(Transaction.MONTH);
    return parentRepository.getAll(Month.TYPE, GlobMatchers.fieldIn(Month.ID, valueSet));
  }

  private AbstractAction createCancelAction() {
    return new AbstractAction(Lang.get("cancel")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    };
  }
}
