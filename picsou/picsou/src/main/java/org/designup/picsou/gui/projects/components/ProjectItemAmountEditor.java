package org.designup.picsou.gui.projects.components;

import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.description.stringifiers.MonthYearStringifier;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.ProjectItemAmount;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobComparators;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ProjectItemAmountEditor implements Disposable {
  private Key itemKey;
  private final boolean forcePositiveAmounts;
  private final Action validate;
  private final GlobRepository repository;
  private final Directory localDirectory;

  private DisposableGroup disposables = new DisposableGroup();
  protected JTextField monthAmountEditorField;
  protected GlobNumericEditor monthCountEditor;
  private JPanel panel;
  private JPanel singleAmountPanel = new JPanel();
  private JPanel monthEditorPanel = new JPanel();

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

    builder.add("singleAmountPanel", singleAmountPanel);
    builder.add("monthEditorPanel", monthEditorPanel);

    if (forcePositiveAmounts) {
      GlobNumericEditor amountEditor = GlobNumericEditor.init(ProjectItem.PLANNED_AMOUNT, repository, localDirectory)
        .setValidationAction(validate)
        .setPositiveNumbersOnly(true)
        .forceSelection(itemKey);
      monthAmountEditorField = amountEditor.getComponent();
      builder.add("amountEditor", monthAmountEditorField);
      disposables.add(amountEditor);
    }
    else {
      AmountEditor amountEditor = new AmountEditor(ProjectItem.PLANNED_AMOUNT, repository, localDirectory, false, null)
        .forceSelection(itemKey)
        .addAction(validate)
        .update(false, false);
      builder.add("amountEditor", amountEditor.getPanel());
      disposables.add(amountEditor);
      monthAmountEditorField = amountEditor.getNumericEditor().getComponent();
    }

    monthCountEditor =
      createMonthEditor()
        .setValidationAction(validate);
    builder.add("monthCountEditor", monthCountEditor);
    disposables.add(monthCountEditor);

    GlobNumericEditor tableMonthCountEditor = createMonthEditor();
    builder.add("tableMonthCountEditor", tableMonthCountEditor);
    disposables.add(tableMonthCountEditor);

    GlobTableView tableView =
      GlobTableView.init(ProjectItemAmount.TYPE, repository,
                         GlobComparators.ascending(ProjectItemAmount.MONTH), localDirectory)
        .setFilter(GlobMatchers.fieldEquals(ProjectItemAmount.PROJECT_ITEM, itemKey.get(ProjectItem.ID)))
        .addColumn(Lang.get("month"), new MonthYearStringifier(ProjectItemAmount.MONTH))
        .addColumn(ProjectItemAmount.PLANNED_AMOUNT);
    builder.add("monthAmountsTable", tableView.getComponent());

    if (forcePositiveAmounts) {
      GlobNumericEditor amountEditor = GlobNumericEditor.init(ProjectItemAmount.PLANNED_AMOUNT, repository, localDirectory)
        .setPositiveNumbersOnly(true)
        .setNotifyOnKeyPressed(true);
      monthAmountEditorField = amountEditor.getComponent();
      builder.add("monthAmountEditor", monthAmountEditorField);
      disposables.add(amountEditor);
    }
    else {
      AmountEditor amountEditor = new AmountEditor(ProjectItemAmount.PLANNED_AMOUNT, repository, localDirectory, false, null)
        .update(false, false);
      monthAmountEditorField = amountEditor.getNumericEditor().getComponent();
      builder.add("monthAmountEditor", amountEditor.getPanel());
      disposables.add(amountEditor);
    }

    ToggleBooleanAction toggleMonthAmounts =
      new ToggleBooleanAction(itemKey,
                              ProjectItem.USE_SAME_AMOUNTS,
                              Lang.get("projectView.item.edition.switchToMonthEditor"),
                              Lang.get("projectView.item.edition.switchToSingleAmount"),
                              repository);
    builder.add("toggleMonthAmounts", toggleMonthAmounts);

    panel = builder.load();

    ModeSelector modeSelector = new ModeSelector();
    disposables.add(modeSelector);
    modeSelector.update();
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

  public void requestFocus() {
    GuiUtils.selectAndRequestFocus(monthAmountEditorField);
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

    protected void update() {
      Glob item = repository.find(itemKey);
      if (item == null) {
        return;
      }

      panel.removeAll();
      if (item.isTrue(ProjectItem.USE_SAME_AMOUNTS)) {
        panel.add(singleAmountPanel);
      }
      else {
        panel.add(monthEditorPanel);
      }
      GuiUtils.revalidate(panel);
    }

    public void dispose() {
      repository.removeChangeListener(this);
    }
  }
}
