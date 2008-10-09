package org.designup.picsou.gui.series;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.ConfirmationDialog;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.description.MonthListStringifier;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import org.globsframework.model.utils.ReverseGlobFieldComparator;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.util.Set;

public class SeriesBudgetEditionPanel {
  private AmountEditor amountEditor;
  private JPanel panel;
  private Window container;
  private GlobRepository localRepository;
  private Directory directory;
  private TimeService timeService;
  private Glob series;
  private BudgetArea budgetArea;
  private SelectionService selectionService;
  private CardHandler modeCard;

  public SeriesBudgetEditionPanel(Window container, final GlobRepository repository,
                                  final GlobRepository localRepository, Directory directory) {
    this.container = container;
    this.localRepository = localRepository;
    this.directory = directory;
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    timeService = directory.get(TimeService.class);
    GlobsPanelBuilder builder = new GlobsPanelBuilder(SeriesEditionDialog.class,
                                                      "/layout/seriesBudgetEditionPanel.splits",
                                                      localRepository, directory);

    modeCard = builder.addCardHandler("modeCard");
    builder.add("manual", new GotoManualAction());
    builder.add("automatic", new GotoAutomaticAction());

    amountEditor = new AmountEditor(SeriesBudget.AMOUNT, localRepository, directory);
    builder.add("amountEditor", amountEditor.getNumericEditor());
    builder.add("positiveAmounts", amountEditor.getPositiveRadio());
    builder.add("negativeAmounts", amountEditor.getNegativeRadio());

    builder.addLabel("seriesBudgetEditionAmountLabel", SeriesBudget.TYPE, new AmountLabelStringifier());

    final GlobTableView budgetTable = builder.addTable("seriesBudget", SeriesBudget.TYPE,
                                                       new ReverseGlobFieldComparator(SeriesBudget.MONTH))
      .setFilter(fieldEquals(SeriesBudget.ACTIVE, true))
      .setDefaultBackgroundPainter(new TableBackgroundPainter())
      .setDefaultLabelCustomizer(new SeriesBudgetLabelCustomizer())
      .addColumn(Lang.get("seriesBudgetEdition.year"), new YearStringifier())
      .addColumn(Lang.get("seriesBudgetEdition.month"), new MonthStringifier())
      .addColumn(Lang.get("seriesBudgetEdition.observed.amount"),
                 new ObservedAmountStringifier(repository, descriptionService),
                 new ObservedLabelCustomizer(repository))
      .addColumn(Lang.get("seriesBudgetEdition.amount"), new AmountStringifier(), LabelCustomizers.ALIGN_RIGHT);
    PicsouTableHeaderPainter.install(budgetTable, directory);

    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList series = selection.getAll(Series.TYPE);
        SeriesBudgetEditionPanel.this.series = series.getFirst();
        if (SeriesBudgetEditionPanel.this.series == null) {
          budgetTable.setFilter(GlobMatchers.NONE);
        }
        else {
          budgetTable.setFilter(
            GlobMatchers.and(fieldEquals(SeriesBudget.ACTIVE, true),
                             fieldEquals(SeriesBudget.SERIES, SeriesBudgetEditionPanel.this.series.get(Series.ID))));

          budgetArea = BudgetArea.get(SeriesBudgetEditionPanel.this.series.get(Series.BUDGET_AREA));
          amountEditor.setBudgetArea(budgetArea);
          if (SeriesBudgetEditionPanel.this.series.get(Series.IS_AUTOMATIC)) {
            modeCard.show("automatic");
          }
          else {
            modeCard.show("manual");
          }
        }
      }
    }, Series.TYPE);

    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void update(Glob series) {
    this.series = series;
  }

  public void selectAmountEditor() {
    amountEditor.selectAll();
  }

  public void selectBudgets(Set<Integer> monthIds) {
    if (series != null) {
      GlobList budgets =
        localRepository.getAll(SeriesBudget.TYPE,
                               GlobMatchers.and(fieldEquals(SeriesBudget.SERIES, series.get(Series.ID)),
                                                GlobMatchers.fieldIn(SeriesBudget.MONTH, monthIds)));
      selectionService.select(budgets, SeriesBudget.TYPE);
    }
  }

  private class YearStringifier extends AbstractGlobStringifier {
    public String toString(Glob seriesBudget, GlobRepository repository) {
      return Integer.toString(Month.toYear(seriesBudget.get(SeriesBudget.MONTH)));
    }
  }

  private class MonthStringifier extends AbstractGlobStringifier {
    public String toString(Glob seriesBudget, GlobRepository repository) {
      return Month.getFullMonthLabel(seriesBudget.get(SeriesBudget.MONTH));
    }
  }

  private class AmountStringifier extends AbstractGlobStringifier {
    public String toString(Glob glob, GlobRepository repository) {
      Double value = glob.get(SeriesBudget.AMOUNT);
      if ((value == null) || (value == 0.0)) {
        return "0";
      }
      StringBuilder builder = new StringBuilder();
      if ((value < 0) && budgetArea.isIncome()) {
        builder.append("-");
      }
      if ((value > 0) && !budgetArea.isIncome()) {
        builder.append("+");
      }
      builder.append(PicsouDescriptionService.DECIMAL_FORMAT.format(Math.abs(value)));
      return builder.toString();
    }
  }

  private class TableBackgroundPainter implements CellPainter {
    public void paint(Graphics g, Glob seriesBudget, int row, int column,
                      boolean isSelected, boolean hasFocus, int width, int height) {
      Color color;
      if (isSelected) {
        color = Gui.getDefaultTableSelectionBackground();
      }
      else {
        Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
        if (monthId == timeService.getCurrentMonthId()) {
          color = Colors.toColor("EEEEFF");
        }
        else {
          boolean even = Month.toYear(monthId) % 2 == 0;
          color = even ? Color.WHITE : Colors.toColor("EEEEEE");
        }
      }
      g.setColor(color);
      g.fillRect(0, 0, width, height);
    }
  }

  private class SeriesBudgetLabelCustomizer implements LabelCustomizer, ColorChangeListener {
    private Color pastTextColor;
    private Color normalTextColor;

    private SeriesBudgetLabelCustomizer() {
      directory.get(ColorService.class).addListener(this);
    }

    protected void finalize() throws Throwable {
      super.finalize();
      directory.get(ColorService.class).removeListener(this);
    }

    public void colorsChanged(ColorLocator colorLocator) {
      pastTextColor = colorLocator.get("seriesBudgetEdition.table.text.past");
      normalTextColor = colorLocator.get("seriesBudgetEdition.table.text.normal");
    }

    public void process(JLabel label, Glob seriesBudget, boolean isSelected, boolean hasFocus, int row, int column) {
      int budgetMonthId = seriesBudget.get(SeriesBudget.MONTH);
      int currentMonthId = timeService.getCurrentMonthId();
      if (budgetMonthId == currentMonthId) {
        label.setForeground(normalTextColor);
        LabelCustomizers.BOLD.process(label, seriesBudget, isSelected, hasFocus, row, column);
      }
      else if (budgetMonthId < currentMonthId) {
        label.setForeground(pastTextColor);
        LabelCustomizers.PLAIN.process(label, seriesBudget, isSelected, hasFocus, row, column);
      }
      else if (budgetMonthId > currentMonthId) {
        label.setForeground(normalTextColor);
        LabelCustomizers.PLAIN.process(label, seriesBudget, isSelected, hasFocus, row, column);
      }
      if (isSelected) {
        label.setForeground(Color.WHITE);
      }
    }
  }

  private class ObservedLabelCustomizer implements LabelCustomizer, ColorChangeListener {
    private GlobRepository repository;
    private Color color;

    private ObservedLabelCustomizer(GlobRepository repository) {
      this.repository = repository;
      directory.get(ColorService.class).addListener(this);
    }

    public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
      Integer monthId = glob.get(SeriesBudget.MONTH);
      Integer seriesId = glob.get(SeriesBudget.SERIES);
      Glob seriesStat = repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
      if (seriesStat != null) {
        if (!BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome()) {
          if (seriesStat.get(SeriesStat.AMOUNT) < glob.get(SeriesBudget.AMOUNT)) {
            label.setForeground(color);
          }
        }
      }
    }

    public void colorsChanged(ColorLocator colorLocator) {
      color = colorLocator.get("seriesBudgetEdition.table.amount.overrun");
    }

    protected void finalize() throws Throwable {
      super.finalize();
      directory.get(ColorService.class).removeListener(this);
    }
  }

  private class ObservedAmountStringifier extends AbstractGlobStringifier {
    private DecimalFormat format;
    private GlobRepository repository;

    private ObservedAmountStringifier(GlobRepository repository, DescriptionService descriptionService) {
      this.repository = repository;
      format = descriptionService.getFormats().getDecimalFormat();
    }

    public String toString(Glob glob, GlobRepository repository) {
      Integer monthId = glob.get(SeriesBudget.MONTH);
      Integer seriesId = glob.get(SeriesBudget.SERIES);
      Glob seriesStat = this.repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
      if (seriesStat != null) {
        Double amount = seriesStat.get(SeriesStat.AMOUNT);
        if (!BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome() && amount != 0.0) {
          amount = -amount;
        }
        String str = format.format(amount);
        if (!BudgetArea.get(series.get(Series.BUDGET_AREA)).isIncome()) {
          if (amount < 0) {
            return str.replace("-", "+");
          }
        }
        return str;
      }
      return "";
    }
  }

  private class AmountLabelStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      Set<Integer> monthIds = list.getValueSet(SeriesBudget.MONTH);
      String monthDescription = MonthListStringifier.toString(monthIds);
      if (Strings.isNullOrEmpty(monthDescription)) {
        return Lang.get("seriesBudgetEdition.amount.label.short");
      }
      else {
        return Lang.get("seriesBudgetEdition.amount.label.full", monthDescription.toLowerCase());
      }
    }
  }

  private class GotoAutomaticAction extends AbstractAction {
    public GotoAutomaticAction() {
      super(Lang.get("seriesEdition.goto.automatic"));
    }

    public void actionPerformed(ActionEvent e) {
      ConfirmationDialog confirm = new ConfirmationDialog("seriesEdition.goto.automatic.title",
                                                          "seriesEdition.goto.automatic.warning",
                                                          container, directory) {
        protected void postValidate() {
          localRepository.update(series.getKey(), Series.IS_AUTOMATIC, true);
          modeCard.show("automatic");
        }
      };
      confirm.show();
    }
  }

  private class GotoManualAction extends AbstractAction {
    private GotoManualAction() {
      super(Lang.get("seriesEdition.goto.manual"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.update(series.getKey(), Series.IS_AUTOMATIC, false);
      modeCard.show("manual");
    }
  }

}
