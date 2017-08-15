package com.budgetview.desktop.projects.components;

import com.budgetview.desktop.components.AmountEditor;
import com.budgetview.desktop.components.MonthSlider;
import com.budgetview.desktop.components.SingleMonthAdapter;
import com.budgetview.desktop.components.tips.ErrorTip;
import com.budgetview.desktop.description.AmountStringifier;
import com.budgetview.desktop.description.stringifiers.MonthYearStringifier;
import com.budgetview.model.Month;
import com.budgetview.model.ProjectItem;
import com.budgetview.model.ProjectItemAmount;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobComparators;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedValue;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class ProjectItemAmountEditor implements Disposable {
  private Key itemKey;
  private final boolean forcePositiveAmounts;
  private final Action validate;
  private final GlobRepository repository;
  private final Directory localDirectory;

  private Mode currentMode = null;

  private DisposableGroup disposables = new DisposableGroup();
  private JTextField singleAmountEditor;
  private JTextField singleMonthAmountEditor;
  protected JTextField monthAmountEditorField;
  protected GlobNumericEditor monthCountEditor;
  private JPanel panel;
  private JPanel singleMonthPanel = new JPanel();
  private JPanel singleAmountPanel = new JPanel();
  private JPanel monthEditorPanel = new JPanel();
  private JPanel currentPanel;
  private GlobTableView monthTableView;

  private enum Mode {
    SINGLE_MONTH,
    SAME_AMOUNT,
    MONTH_EDITOR;

    public static Mode get(Glob item) {
      boolean useSameAmounts = item.isTrue(ProjectItem.USE_SAME_AMOUNTS);
      if (!useSameAmounts) {
        return MONTH_EDITOR;
      }
      Integer monthCount = item.get(ProjectItem.MONTH_COUNT);
      boolean severalMonths = (monthCount != null) && (monthCount != 1);
      return severalMonths ? SAME_AMOUNT : SINGLE_MONTH;
    }
  }

  public ProjectItemAmountEditor(Key itemKey, boolean forcePositiveAmounts, Action validate, GlobRepository repository, Directory directory) {
    this.itemKey = itemKey;
    this.forcePositiveAmounts = forcePositiveAmounts;
    this.validate = validate;
    this.repository = repository;
    this.localDirectory = new DefaultDirectory(directory);
    this.localDirectory.add(new SelectionService());
  }

  private void init() {

    final GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/components/projectItemAmountEditor.splits",
                                                            repository, localDirectory);

    builder.add("singleMonthPanel", singleMonthPanel);
    builder.add("singleAmountPanel", singleAmountPanel);
    builder.add("monthEditorPanel", monthEditorPanel);

    addMonthSlider(builder, "monthEditor1");
    addMonthSlider(builder, "monthEditor2");
    addMonthSlider(builder, "monthEditor3");

    singleMonthAmountEditor = addAmountEditor(builder, "amountEditor1");
    singleAmountEditor = addAmountEditor(builder, "amountEditor2");

    monthCountEditor =
      createMonthEditor()
        .setValidationAction(validate);
    builder.add("monthCountEditor", monthCountEditor);

    GlobNumericEditor tableMonthCountEditor = createMonthEditor();
    builder.add("tableMonthCountEditor", tableMonthCountEditor);
    disposables.add(tableMonthCountEditor);

    monthTableView = GlobTableView.init(ProjectItemAmount.TYPE, repository,
                                        GlobComparators.ascending(ProjectItemAmount.MONTH), localDirectory)
      .setFilter(GlobMatchers.fieldEquals(ProjectItemAmount.PROJECT_ITEM, itemKey.get(ProjectItem.ID)))
      .addColumn(Lang.get("month"), new MonthYearStringifier(ProjectItemAmount.MONTH));
    if (forcePositiveAmounts) {
      monthTableView
        .addColumn(Lang.get("planned"), ProjectItemAmount.PLANNED_AMOUNT);
    }
    else {
      monthTableView
        .addColumn(Lang.get("planned"), AmountStringifier.getForSingle(ProjectItemAmount.PLANNED_AMOUNT, BudgetArea.EXTRAS));
    }
    builder.add("monthAmountsTable", monthTableView.getComponent());

    monthAmountEditorField = createMonthAmountEditor(builder);

    ToggleBooleanAction toggleMonthAmounts =
      new ToggleBooleanAction(itemKey,
                              ProjectItem.USE_SAME_AMOUNTS,
                              Lang.get("projectView.item.edition.switchToMonthEditor"),
                              Lang.get("projectView.item.edition.revertToSingleAmount"),
                              repository);
    builder.add("toggleMonthAmounts", toggleMonthAmounts);

    builder.add("switchToSeveralMonths", new SwitchToSeveralMonthsAction());
    builder.add("switchToMonthEditor", new SwitchToMonthEditorAction());
    builder.add("revertToSingleAmount", new RevertToSingleAmountAction());

    panel = builder.load();

    FullSingleComponentLayout layout = new FullSingleComponentLayout(panel);
    panel.setLayout(layout);

    ModeSelector modeSelector = new ModeSelector();
    disposables.add(modeSelector);
    modeSelector.update();
  }

  private JTextField addAmountEditor(GlobsPanelBuilder builder, String componentName) {
    JTextField editorField;
    if (forcePositiveAmounts) {
      GlobNumericEditor amountEditor = GlobNumericEditor.init(ProjectItem.PLANNED_AMOUNT, repository, localDirectory)
        .setValidationAction(validate)
        .setPositiveNumbersOnly(true)
        .forceSelection(itemKey);
      editorField = amountEditor.getComponent();
      builder.add(componentName, editorField);
      disposables.add(amountEditor);
    }
    else {
      AmountEditor amountEditor = new AmountEditor(ProjectItem.PLANNED_AMOUNT, repository, localDirectory, false, null)
        .forceSelection(itemKey)
        .addAction(validate)
        .update(false, false);
      builder.add(componentName, amountEditor.getPanel());
      disposables.add(amountEditor);
      editorField = amountEditor.getNumericEditor().getComponent();
    }
    return editorField;
  }

  private JTextField createMonthAmountEditor(GlobsPanelBuilder builder) {
    JTextField editorField;
    if (forcePositiveAmounts) {
      GlobNumericEditor amountEditor = GlobNumericEditor.init(ProjectItemAmount.PLANNED_AMOUNT, repository, localDirectory)
        .setPositiveNumbersOnly(true)
        .setNotifyOnKeyPressed(true);
      editorField = amountEditor.getComponent();
      builder.add("monthAmountEditor", editorField);
      disposables.add(amountEditor);
    }
    else {
      AmountEditor amountEditor = new AmountEditor(ProjectItemAmount.PLANNED_AMOUNT, repository, localDirectory, true, null)
        .update(false, false);
      editorField = amountEditor.getNumericEditor().getComponent();
      builder.add("monthAmountEditor", amountEditor.getPanel());
      disposables.add(amountEditor);
    }
    return editorField;
  }

  private void addMonthSlider(GlobsPanelBuilder builder, String ref) {
    MonthSlider monthSlider = new MonthSlider(new SingleMonthAdapter(ProjectItem.FIRST_MONTH) {
      public String convertToString(Integer monthId) {
        return Month.getShortMonthLabelWithYear(monthId);
      }
    }, repository, localDirectory);
    monthSlider.setKey(itemKey);
    builder.add(ref, monthSlider);
    disposables.add(monthSlider);
  }

  private GlobNumericEditor createMonthEditor() {
    return GlobNumericEditor.init(ProjectItem.MONTH_COUNT, repository, localDirectory)
      .setPositiveNumbersOnly(true)
      .setNotifyOnKeyPressed(false)
      .forceSelection(itemKey);
  }

  public JPanel getPanel() {
    if (panel == null) {
      init();
    }
    return panel;
  }

  private void setMode(Mode newMode) {
    JPanel previous = currentPanel;
    switch (newMode) {
      case SINGLE_MONTH:
        currentPanel = singleMonthPanel;
        break;
      case SAME_AMOUNT:
        currentPanel = singleAmountPanel;
        break;
      case MONTH_EDITOR:
        currentPanel = monthEditorPanel;
        break;
      default:
        throw new UnexpectedValue(newMode);
    }
    if (previous != currentPanel) {
      currentMode = newMode;
      panel.removeAll();
      panel.add(currentPanel);
      GuiUtils.revalidate(panel);
    }
  }

  public void requestFocus() {
    if (currentMode != null) {
      switch (currentMode) {
        case SINGLE_MONTH:
          GuiUtils.selectAndRequestFocus(singleMonthAmountEditor);
          break;
        case SAME_AMOUNT:
          GuiUtils.selectAndRequestFocus(singleAmountEditor);
          break;
        case MONTH_EDITOR:
          GuiUtils.selectAndRequestFocus(monthAmountEditorField);
          break;
      }
    }
  }

  public void dispose() {
    disposables.dispose();
  }

  public boolean check() {
    Glob projectItem = repository.get(itemKey);
    Integer count = projectItem.get(ProjectItem.MONTH_COUNT);
    if (count == null || count < 1) {
      JTextField monthCountField = monthCountEditor.getComponent();
      ErrorTip.showLeft(monthCountField,
                        Lang.get("projectEdition.error.invalidMonthCount"),
                        localDirectory);
      GuiUtils.selectAndRequestFocus(monthCountField);
      return false;
    }
    return true;
  }

  private class ModeSelector extends KeyChangeListener implements Disposable {

    protected ModeSelector() {
      super(itemKey);
      repository.addChangeListener(this);
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      if (itemKey != null && changedTypes.contains(itemKey.getGlobType())) {
        ProjectItemAmountEditor.this.currentMode = null;
      }
      super.globsReset(repository, changedTypes);
    }

    public void update() {
      Glob item = repository.find(itemKey);
      if (item == null) {
        return;
      }

      Mode newMode = Mode.get(item);
      setMode(newMode);
    }

    public void dispose() {
      repository.removeChangeListener(this);
    }
  }

  private class SwitchToSeveralMonthsAction extends AbstractAction {
    private SwitchToSeveralMonthsAction() {
      super(Lang.get("projectView.item.edition.switchToSeveralMonths"));
    }

    public void actionPerformed(ActionEvent e) {
      setMode(Mode.SAME_AMOUNT);
      requestFocus();
    }
  }

  private class SwitchToMonthEditorAction extends AbstractAction {

    private SwitchToMonthEditorAction() {
      super(Lang.get("projectView.item.edition.switchToMonthEditor"));
    }

    public void actionPerformed(ActionEvent e) {
      repository.startChangeSet();
      try {
        repository.update(itemKey, ProjectItem.USE_SAME_AMOUNTS, false);
        setMode(Mode.MONTH_EDITOR);
      }
      finally {
        repository.completeChangeSet();
      }
      monthTableView.selectFirst();
      requestFocus();
    }
  }

  private class RevertToSingleAmountAction extends AbstractAction {

    private RevertToSingleAmountAction() {
      super(Lang.get("projectView.item.edition.revertToSingleAmount"));
    }

    public void actionPerformed(ActionEvent e) {
      setMode(Mode.SAME_AMOUNT);
      repository.update(itemKey, ProjectItem.USE_SAME_AMOUNTS, true);
    }
  }

}
