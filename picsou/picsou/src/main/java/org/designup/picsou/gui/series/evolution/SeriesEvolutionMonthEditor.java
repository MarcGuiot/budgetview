package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.components.AbstractRolloverEditor;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.SeriesAmountEditionDialog;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
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
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
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
                                       SeriesAmountEditionDialog seriesAmountEditionDialog) {
    super(view, directory.get(DescriptionService.class), repository, directory);
    this.offset = offset;
    this.colors = colors;

    OpenSeriesAmountEditionDialogAction action =
      new OpenSeriesAmountEditionDialogAction(seriesAmountEditionDialog);

    LabelCustomizers.BOLD.process(label);
    labelPanel = initCellPanel(label, false, new PaintablePanel());

    rendererButton = createHyperlinkButton(action);
    rendererPanel = initCellPanel(rendererButton, false, new PaintablePanel());

    editorButton = createHyperlinkButton(action);
    editorPanel = initCellPanel(editorButton, false, new PaintablePanel());
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
        label.setText(stringify(seriesWrapper));
        colors.setColors(seriesWrapper, row, offset, referenceMonthId, isSelected, label, labelPanel);
        return labelPanel;

      case SERIES:
        JButton button = render ? rendererButton : editorButton;
        button.setText(stringify(seriesWrapper));
        PaintablePanel panel = render ? rendererPanel : editorPanel;
        colors.setColors(seriesWrapper, row, offset, referenceMonthId, isSelected, button, panel);
        return panel;

      case SUMMARY:
        label.setText(stringify(seriesWrapper));
        colors.setColors(seriesWrapper, row, offset, referenceMonthId, isSelected, label, labelPanel);
        return labelPanel;

      default:
        throw new InvalidParameter("Unexpected type: " + SeriesWrapperType.get(seriesWrapper));
    }
  }

  private String stringify(Glob seriesWrapper) {

    Integer itemId = seriesWrapper.get(SeriesWrapper.ITEM_ID);

    switch (SeriesWrapperType.get(seriesWrapper)) {
      case BUDGET_AREA:
        return getBudgetAreaLabelText(BudgetArea.get(itemId));

      case SERIES:
        return getSeriesButtonText(itemId);

      case SUMMARY:
        return getSummaryLabelText(seriesWrapper);

      default:
        throw new InvalidParameter("Unexpected type: " + SeriesWrapperType.get(seriesWrapper));
    }
  }

  private String getBudgetAreaLabelText(BudgetArea budgetArea) {
    Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, referenceMonthId));
    if (budgetArea.equals(BudgetArea.UNCATEGORIZED)) {
      return format(budgetStat, BudgetStat.UNCATEGORIZED_ABS, budgetArea);
    }
    else if (budgetStat != null) {
      return format(budgetStat, BudgetStat.getSummary(budgetArea), budgetArea);
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

    Glob series = repository.find(Key.create(Series.TYPE, itemId));
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    Double value = seriesStat.get(SeriesStat.SUMMARY_AMOUNT);

    return format(value, budgetArea);
  }

  private String getSummaryLabelText(Glob seriesWrapper) {
    Integer id = seriesWrapper.get(SeriesWrapper.ID);
    if (id.equals(SeriesWrapper.BALANCE_SUMMARY_ID)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, referenceMonthId));
      return format(budgetStat, BudgetStat.MONTH_BALANCE, null);
    }

    if (id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, referenceMonthId));
      return format(budgetStat, BudgetStat.END_OF_MONTH_ACCOUNT_POSITION, null);
    }

    if (id.equals(SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
      Glob budgetStat = SavingsBudgetStat.findSummary(referenceMonthId, repository);
      return format(budgetStat, SavingsBudgetStat.END_OF_MONTH_POSITION, null);
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
      return Formatting.toString(value, budgetArea);
    }
    return Formatting.toString(value);
  }

  protected HyperlinkButton createHyperlinkButton(Action action) {
    HyperlinkButton button = super.createHyperlinkButton(action);
    final Font font = button.getFont().deriveFont(Font.PLAIN, 10);
    button.setFont(font);
    return button;
  }

  public GlobStringifier getStringifier() {
    return new AbstractGlobStringifier() {
      public String toString(Glob seriesWrapper, GlobRepository repository) {
        return stringify(seriesWrapper);
      }
    };
  }

  private class OpenSeriesAmountEditionDialogAction extends AbstractAction {
    private SeriesAmountEditionDialog seriesAmountEditionDialog;

    public OpenSeriesAmountEditionDialogAction(SeriesAmountEditionDialog seriesAmountEditionDialog) {
      this.seriesAmountEditionDialog = seriesAmountEditionDialog;
    }

    public void actionPerformed(ActionEvent e) {
      seriesAmountEditionDialog.show(currentSeries, Collections.singleton(referenceMonthId));
    }
  }
}
