package org.designup.picsou.gui.series;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.ConfirmationDialog;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.description.MonthListStringifier;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.triggers.SameAccountChecker;
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
import static org.globsframework.gui.views.utils.LabelCustomizers.chain;
import org.globsframework.model.*;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
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
  private Glob currentSeries;
  private boolean isAutomatic;
  private BudgetArea budgetArea;
  private SelectionService selectionService;
  private CardHandler modeCard;
  private GlobTableView budgetTable;
  private Boolean isNormalyPositive;
  private SwitchToAutomaticAction switchToAutomaticAction = new SwitchToAutomaticAction();
  private SwitchToManualAction switchToManualAction = new SwitchToManualAction();

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

    builder.add("manual", switchToManualAction);
    builder.add("automatic", switchToAutomaticAction);

    amountEditor = new AmountEditor(SeriesBudget.AMOUNT, localRepository, directory);
    builder.add("amountEditor", amountEditor.getNumericEditor());
    builder.add("positiveAmounts", amountEditor.getPositiveRadio());
    builder.add("negativeAmounts", amountEditor.getNegativeRadio());

    builder.addLabel("seriesBudgetEditionAmountLabel", SeriesBudget.TYPE, new AmountLabelStringifier());

    budgetTable = builder.addTable("seriesBudget", SeriesBudget.TYPE,
                                   new ReverseGlobFieldComparator(SeriesBudget.MONTH))
      .setFilter(fieldEquals(SeriesBudget.ACTIVE, true))
      .setDefaultBackgroundPainter(new TableBackgroundPainter())
      .setDefaultLabelCustomizer(new SeriesBudgetLabelCustomizer())
      .addColumn(Lang.get("seriesBudgetEdition.year"), new YearStringifier())
      .addColumn(Lang.get("seriesBudgetEdition.month"), new MonthStringifier())
      .addColumn(Lang.get("seriesBudgetEdition.observed.amount"),
                 new ObservedAmountStringifier(repository, descriptionService),
                 chain(new ObservedLabelCustomizer(repository), LabelCustomizers.ALIGN_RIGHT))
      .addColumn(Lang.get("seriesBudgetEdition.amount"),
                 new AmountStringifier(), LabelCustomizers.ALIGN_RIGHT);
    PicsouTableHeaderPainter.install(budgetTable, directory);

    selectionService = directory.get(SelectionService.class);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        SeriesBudgetEditionPanel.this.currentSeries = selection.getAll(Series.TYPE).getFirst();
        processSeriesSelection();
      }
    }, Series.TYPE);

    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (currentSeries == null) {
          return;
        }
        if (changeSet.containsChanges(currentSeries.getKey())) {
          FieldValues previousValue = changeSet.getPreviousValue(currentSeries.getKey());
          if (previousValue.contains(Series.IS_AUTOMATIC)) {
            isAutomatic = currentSeries.get(Series.IS_AUTOMATIC);
            if (isAutomatic) {
              modeCard.show("automatic");
            }
            else {
              modeCard.show("manual");
            }
          }
          if (previousValue.contains(Series.PROFILE_TYPE)) {
            if (previousValue.get(Series.PROFILE_TYPE).equals(ProfileType.IRREGULAR.getId())) {
              repository.update(currentSeries.getKey(), Series.IS_AUTOMATIC, isAutomatic);
              if (isAutomatic) {
                modeCard.show("automatic");
              }
              else {
                modeCard.show("manual");
              }
            }
            else if (currentSeries.get(Series.PROFILE_TYPE).equals(ProfileType.IRREGULAR.getId())) {
              repository.update(currentSeries.getKey(), Series.IS_AUTOMATIC, false);
              modeCard.show("manual");
            }
          }
          if (previousValue.contains(Series.FROM_ACCOUNT) || previousValue.contains(Series.TO_ACCOUNT)) {
            updatePositiveOrNegativeRadio();
          }
        }
      }
    });

    panel = builder.load();
  }

  private void processSeriesSelection() {
    if (currentSeries == null) {
      budgetTable.setFilter(GlobMatchers.NONE);
      switchToManualAction.setEnabled(false);
      return;
    }

    budgetTable.setFilter(
      and(fieldEquals(SeriesBudget.ACTIVE, true),
          fieldEquals(SeriesBudget.SERIES, currentSeries.get(Series.ID))));

    budgetArea = BudgetArea.get(currentSeries.get(Series.BUDGET_AREA));
    updatePositiveOrNegativeRadio();

    switchToManualAction.setEnabled(true);
    if (currentSeries.get(Series.IS_AUTOMATIC)) {
      modeCard.show("automatic");
      isAutomatic = true;
      if (currentSeries.get(Series.PROFILE_TYPE).equals(ProfileType.IRREGULAR.getId())) {
        switchToAutomaticAction.setEnabled(false);
      }
      else {
        switchToAutomaticAction.setEnabled(true);
      }
    }
    else {
      modeCard.show("manual");
      isAutomatic = false;
    }
  }

  private void updatePositiveOrNegativeRadio() {
    Glob fromAccount = localRepository.findLinkTarget(currentSeries, Series.FROM_ACCOUNT);
    Glob toAccount = localRepository.findLinkTarget(currentSeries, Series.TO_ACCOUNT);
//    boolean isFromToSeries = Account.shoudCreateMirror(fromAccount, toAccount) ||
//                             Account.areNoneImported(fromAccount, toAccount);

    SameAccountChecker mainAccountChecker = SameAccountChecker.getSameAsMain(localRepository);

    Integer forAccountId = currentSeries.get(Series.TO_ACCOUNT) == null ?
                           currentSeries.get(Series.FROM_ACCOUNT) : currentSeries.get(Series.TO_ACCOUNT);
    if (mainAccountChecker.isSame(currentSeries.get(Series.FROM_ACCOUNT))) {
      forAccountId = currentSeries.get(Series.FROM_ACCOUNT);
    }
    if (mainAccountChecker.isSame(currentSeries.get(Series.TO_ACCOUNT))) {
      forAccountId = currentSeries.get(Series.TO_ACCOUNT);
    }

    double multiplier = 1;
    if (forAccountId != null) {
      multiplier = Account.getMultiplierForInOrOutputOfTheAccount(fromAccount, toAccount,
                                                                  localRepository.get(Key.create(Account.TYPE,
                                                                                                 forAccountId)));
    }

    isNormalyPositive = budgetArea.isIncome() ||
                        (budgetArea == BudgetArea.SAVINGS && multiplier > 0);
    amountEditor.update(isNormalyPositive, budgetArea == BudgetArea.SAVINGS);
  }

  public JPanel getPanel() {
    return panel;
  }

  public void selectAmountEditor() {
    amountEditor.selectAll();
  }

  public void selectMonths(Set<Integer> monthIds) {
    if (currentSeries == null) {
      return;
    }

    GlobList budgets =
      localRepository.getAll(SeriesBudget.TYPE,
                             and(fieldEquals(SeriesBudget.SERIES, currentSeries.get(Series.ID)),
                                 fieldIn(SeriesBudget.MONTH, monthIds),
                                 fieldEquals(SeriesBudget.ACTIVE, true)));
    if (budgets.isEmpty()) {
      budgets = localRepository.getAll(SeriesBudget.TYPE,
                                       and(fieldEquals(SeriesBudget.SERIES, currentSeries.get(Series.ID)),
                                           fieldEquals(SeriesBudget.ACTIVE, true)));
    }

    selectionService.select(budgets, SeriesBudget.TYPE);
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
      if (Amounts.isNullOrZero(value)) {
        return "0";
      }
      StringBuilder builder = new StringBuilder();
      if ((value < 0) && isNormalyPositive) {
        builder.append("-");
      }
      if ((value > 0) && !isNormalyPositive) {
        builder.append("+");
      }
      builder.append(Formatting.DECIMAL_FORMAT.format(Math.abs(value)));
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
        if (!BudgetArea.get(currentSeries.get(Series.BUDGET_AREA)).isIncome()) {
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
        if (!BudgetArea.get(currentSeries.get(Series.BUDGET_AREA)).isIncome() && !Amounts.isNullOrZero(amount)) {
          amount = -amount;
        }
        String str = format.format(amount);
        if (!BudgetArea.get(currentSeries.get(Series.BUDGET_AREA)).isIncome()) {
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

  private class SwitchToAutomaticAction extends AbstractAction {
    public SwitchToAutomaticAction() {
      super(Lang.get("seriesEdition.goto.automatic"));
    }

    public void actionPerformed(ActionEvent e) {
      ConfirmationDialog confirm = new ConfirmationDialog("seriesEdition.goto.automatic.title",
                                                          "seriesEdition.goto.automatic.warning",
                                                          container, directory) {
        protected void postValidate() {
          localRepository.update(currentSeries.getKey(), Series.IS_AUTOMATIC, true);
          modeCard.show("automatic");
          isAutomatic = true;
        }
      };
      confirm.show();
    }
  }

  private class SwitchToManualAction extends AbstractAction {
    private SwitchToManualAction() {
      super(Lang.get("seriesEdition.goto.manual"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.update(currentSeries.getKey(), Series.IS_AUTOMATIC, false);
      modeCard.show("manual");
      isAutomatic = false;
    }
  }

}
