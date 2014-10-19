package org.designup.picsou.gui.analysis;

import org.designup.picsou.gui.View;
import org.designup.picsou.model.AnalysisViewType;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.SelectGlobAction;
import org.globsframework.gui.components.GlobSelectablePanel;
import org.globsframework.gui.components.GlobSelectionToggle;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobSelectionNodeStyleUpdater;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.UnexpectedValue;

import javax.swing.*;
import java.util.Arrays;

public class AnalysisSelector extends View {

  public AnalysisSelector(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/analysis/analysisSelector.splits",
                                                      repository, directory);

    builder.addRepeat("repeat",
                      Arrays.asList(AnalysisViewType.BUDGET, AnalysisViewType.TABLE),
                      new AccountTypeComponentFactory());

    parentBuilder.add("analysisSelector", builder);
  }

  public void reset() {
    selectionService.select(repository.find(AnalysisViewType.BUDGET.getGlob().getKey()));
  }

  private class AccountTypeComponentFactory  implements RepeatComponentFactory<AnalysisViewType> {
    public void registerComponents(PanelBuilder cellBuilder, AnalysisViewType type) {
      SplitsNode<JPanel> selectionPanel = cellBuilder.add("selectionPanel", new JPanel());
      Key key = type.getGlob().getKey();
      final GlobSelectablePanel selectablePanel =
              new GlobSelectablePanel(selectionPanel,
                                      "selectedPanel", "unselectedPanel",
                                      "selectedRolloverPanel", "unselectedRolloverPanel",
                                      repository, directory, key);
      selectablePanel.setUnselectEnabled(false);
      cellBuilder.addDisposable(selectablePanel);

      ImageLocator imageLocator = directory.get(ImageLocator.class);
      SelectGlobAction selectAction = new SelectGlobAction(key, type.getLabel(), repository, directory);
      JButton selector = new JButton(selectAction);
      selector.setName("selector:" + type.name().toLowerCase());
      selector.setIcon(imageLocator.get(getIconFile(type)));
      cellBuilder.add("selector", selector);

      SplitsNode<JButton> arrow = cellBuilder.add("arrow", new JButton(selectAction));
      cellBuilder.addDisposable(GlobSelectionNodeStyleUpdater.init(key, arrow,
                                                                   "sidebarSelectionArrowSelected",
                                                                   "sidebarSelectionArrowUnselected",
                                                                   directory));
    }

    public String getIconFile(AnalysisViewType type) {
      switch (type) {
        case BUDGET:
          return "analysis/analysis_composition.png";
        case TABLE:
          return "analysis/analysis_table.png";
        case SERIES:
          return "analysis/analysis_evolution.png";
      }
      throw new UnexpectedValue(type);
    }
  }
}
