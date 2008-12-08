package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.components.AbstractRolloverEditor;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.Account;
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
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.metamodel.fields.DoubleField;

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
    if (!render) {
      currentSeries = repository.get(Key.create(Series.TYPE, itemId));
    }

    switch (SeriesWrapperType.get(seriesWrapper)) {
      case BUDGET_AREA:
        updateBudgetAreaLabel(BudgetArea.get(itemId));
        colors.setColors(seriesWrapper, row, offset, isSelected, label, labelPanel);
        return labelPanel;

      case SERIES:
        JButton button = render ? rendererButton : editorButton;
        PaintablePanel panel = render ? rendererPanel : editorPanel;
        updateSeriesButton(itemId, button, panel);
        colors.setColors(seriesWrapper, row, offset, isSelected, button, panel);
        return panel;

      case SUMMARY:
        updateSummaryLabel(seriesWrapper);
        colors.setColors(seriesWrapper, row, offset, isSelected, label, labelPanel);
        return labelPanel;

      default:
        throw new InvalidParameter("Unexpected type: " + SeriesWrapperType.get(seriesWrapper));
    }
  }

  private void updateBudgetAreaLabel(BudgetArea budgetArea) {
    Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, referenceMonthId));
    if (budgetArea.equals(BudgetArea.UNCATEGORIZED)) {
      label.setText(format(balanceStat, BalanceStat.UNCATEGORIZED));
    }
    else {
      if (balanceStat != null) {
        label.setText(format(balanceStat, BalanceStat.getPlanned(budgetArea)));
      }
      else {
        label.setText("nothing");
      }
    }
  }

  private String format(Glob glob, DoubleField field) {
    Double value = glob.get(field);
    if ((value == null) || Math.abs(value) < 0.001) {
      return "";
    }
    return Formatting.toString(value);
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
      button.setText(format(seriesStat, SeriesStat.PLANNED_AMOUNT));
    }
  }

  private void updateSummaryLabel(Glob seriesWrapper) {
    Integer id = seriesWrapper.get(SeriesWrapper.ID);
    if (id.equals(SeriesWrapper.BALANCE_SUMMARY_ID)) {
      Glob balanceStat = repository.get(Key.create(BalanceStat.TYPE, referenceMonthId));
      label.setText(format(balanceStat, BalanceStat.MONTH_BALANCE));
    }
    else if (id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
      Glob balanceStat = repository.get(Key.create(BalanceStat.TYPE, referenceMonthId));
      label.setText(format(balanceStat, BalanceStat.END_OF_MONTH_ACCOUNT_POSITION));
    }
    else if (id.equals(SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
      Glob balanceStat = repository.find(Key.create(SavingsBalanceStat.MONTH, referenceMonthId,
                                                    SavingsBalanceStat.ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID));
      if (balanceStat != null) {
        label.setText(format(balanceStat, SavingsBalanceStat.END_OF_MONTH_POSITION));
      }
      else {
        label.setText(null);
      }
    }
    else {
      throw new InvalidParameter("Unexpected ID: " + id);
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
