package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.components.AbstractRolloverEditor;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.gui.splits.painters.PaintablePanel;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.KeyBuilder;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

public class SeriesEvolutionMonthEditor extends AbstractRolloverEditor {

  private int offset;
  private Integer referenceMonthId;
  private SeriesEvolutionColors colors;

  private Glob currentSeries;

  private JLabel label = new JLabel();
  private PaintablePanel labelPanel;
  private HyperlinkButton rendererButton;
  private PaintablePanel rendererPanel;
  private HyperlinkButton editorButton;
  private PaintablePanel editorPanel;

  protected SeriesEvolutionMonthEditor(int offset, GlobTableView view,
                                       GlobRepository repository, Directory directory,
                                       SeriesEvolutionColors colors,
                                       SeriesEditionDialog seriesEditionDialog) {
    super(view, directory.get(DescriptionService.class), repository, directory);
    this.offset = offset;
    this.colors = colors;

    OpenSeriesEditionDialogAction action = new OpenSeriesEditionDialogAction(seriesEditionDialog);

    LabelCustomizers.BOLD.process(label);
    labelPanel = createCellPanel(label, false);

    rendererButton = createHyperlinkButton(action);
    rendererPanel = createCellPanel(rendererButton, false);

    editorButton = createHyperlinkButton(action);
    editorPanel = createCellPanel(editorButton, false);
  }

  public void setReferenceMonth(Integer monthId) {
    this.referenceMonthId = monthId;
  }

  protected Component getComponent(Glob seriesWrapper, boolean render) {
    if (referenceMonthId == null) {
      label.setText("no month");
      return labelPanel;
    }

    Integer itemId = seriesWrapper.get(SeriesWrapper.ITEM_ID);
    if (seriesWrapper.get(SeriesWrapper.IS_BUDGET_AREA)) {
      updateBudgetAreaLabel(BudgetArea.get(itemId));
      colors.setColors(seriesWrapper, row, offset, isSelected, label, labelPanel);
      return labelPanel;
    }

    if (!render) {
      currentSeries = repository.get(Key.create(Series.TYPE, itemId));
    }

    JButton button = render ? rendererButton : editorButton;
    PaintablePanel panel = render ? rendererPanel : editorPanel;
    updateSeriesButton(itemId, button, panel);
    colors.setColors(seriesWrapper, row, offset, isSelected, button, panel);
    return panel;
  }

  private void updateSeriesButton(Integer itemId, JButton button, PaintablePanel panel) {
    Glob seriesStat = repository.find(KeyBuilder.init(SeriesStat.TYPE)
      .set(SeriesStat.MONTH, referenceMonthId)
      .set(SeriesStat.SERIES, itemId)
      .get());
    if (seriesStat == null || Math.abs(seriesStat.get(SeriesStat.PLANNED_AMOUNT)) < 0.01) {
      button.setText("");
    }
    else {
      button.setText(Formatting.toString(seriesStat.get(SeriesStat.PLANNED_AMOUNT)));
    }
  }

  private void updateBudgetAreaLabel(BudgetArea budgetArea) {
    if (budgetArea.equals(BudgetArea.ALL)) {
      label.setText("x");
    }
    else if (budgetArea.equals(BudgetArea.UNCATEGORIZED)) {
      label.setText("?");
    }
    else {
      Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, referenceMonthId));
      if (balanceStat != null) {
        label.setText(Formatting.toString(balanceStat.get(BalanceStat.getPlanned(budgetArea))));
      }
      else {
        label.setText("nothing");
      }
    }
  }

  protected HyperlinkButton createHyperlinkButton(Action action) {
    HyperlinkButton button = super.createHyperlinkButton(action);
    final Font font = button.getFont().deriveFont(Font.PLAIN, 10);
    button.setFont(font);
    return button;
  }

  private class OpenSeriesEditionDialogAction extends AbstractAction {
    private SeriesEditionDialog seriesEditionDialog;

    public OpenSeriesEditionDialogAction(SeriesEditionDialog seriesEditionDialog) {
      this.seriesEditionDialog = seriesEditionDialog;
    }

    public void actionPerformed(ActionEvent e) {
      seriesEditionDialog.show(currentSeries, Collections.singleton(referenceMonthId));
    }
  }
}
