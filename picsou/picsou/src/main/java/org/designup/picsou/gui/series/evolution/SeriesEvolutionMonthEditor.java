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
import org.designup.picsou.model.util.Amounts;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.gui.splits.painters.PaintablePanel;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.KeyBuilder;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

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
        label.setText(getBudgetAreaLabelText(BudgetArea.get(itemId)));
        colors.setColors(seriesWrapper, row, offset, referenceMonthId, isSelected, label, labelPanel);
        return labelPanel;

      case SERIES:
        JButton button = render ? rendererButton : editorButton;
        button.setText(getSeriesButtonText(itemId));
        PaintablePanel panel = render ? rendererPanel : editorPanel;
        colors.setColors(seriesWrapper, row, offset, referenceMonthId, isSelected, button, panel);
        return panel;

      case SUMMARY:
        label.setText(getSummaryLabelText(seriesWrapper));
        colors.setColors(seriesWrapper, row, offset, referenceMonthId, isSelected, label, labelPanel);
        return labelPanel;

      default:
        throw new InvalidParameter("Unexpected type: " + SeriesWrapperType.get(seriesWrapper));
    }
  }

  private String getBudgetAreaLabelText(BudgetArea budgetArea) {
    Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, referenceMonthId));
    if (budgetArea.equals(BudgetArea.UNCATEGORIZED)) {
      return format(balanceStat, BalanceStat.UNCATEGORIZED, budgetArea);
    }
    else if (balanceStat != null) {
      return format(balanceStat, BalanceStat.getPlanned(budgetArea), budgetArea);
    }
    return "";
  }

  private String getSeriesButtonText(Integer itemId) {
    Glob seriesStat = repository.find(KeyBuilder.init(SeriesStat.TYPE)
      .set(SeriesStat.MONTH, referenceMonthId)
      .set(SeriesStat.SERIES, itemId)
      .get());
    if (seriesStat == null) {
      return "";
    }

    Double observed = seriesStat.get(SeriesStat.AMOUNT);
    Double planned = seriesStat.get(SeriesStat.PLANNED_AMOUNT);
    Double value;
    if (!Amounts.isNullOrZero(observed) && !Amounts.isNullOrZero(planned)) {
      if (observed < 0 && observed < planned) {
        value = observed;
      }
      else {
        value = planned;
      }
    }
    else if (!Amounts.isNullOrZero(planned)) {
      value = planned;
    }
    else {
      value = observed;
    }

    Glob series = repository.find(Key.create(Series.TYPE, itemId));
    BudgetArea budgeArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    return format(value, budgeArea);
  }

  private String getSummaryLabelText(Glob seriesWrapper) {
    Integer id = seriesWrapper.get(SeriesWrapper.ID);
    if (id.equals(SeriesWrapper.BALANCE_SUMMARY_ID)) {
      Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, referenceMonthId));
      return format(balanceStat, BalanceStat.MONTH_BALANCE, null);
    }

    if (id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
      Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, referenceMonthId));
      return format(balanceStat, BalanceStat.END_OF_MONTH_ACCOUNT_POSITION, null);
    }

    if (id.equals(SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
      Glob balanceStat = repository.find(Key.create(SavingsBalanceStat.MONTH, referenceMonthId,
                                                    SavingsBalanceStat.ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID));
      return format(balanceStat, SavingsBalanceStat.END_OF_MONTH_POSITION, null);
    }

    throw new InvalidParameter("Unexpected ID: " + id);
  }

  private String format(Glob glob, DoubleField field, BudgetArea budgetArea) {
    if (glob == null) {
      return "";
    }
    return format(glob.get(field), budgetArea);
  }

  private String format(Double value, BudgetArea budgetArea) {
    if (Amounts.isNullOrZero(value)) {
      return "";
    }
    if (budgetArea != null) {
      return Formatting.toString(value, budgetArea, false);
    }
    return Formatting.toString(value);
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
