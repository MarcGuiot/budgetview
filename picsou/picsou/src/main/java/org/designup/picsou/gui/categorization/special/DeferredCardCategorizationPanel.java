package org.designup.picsou.gui.categorization.special;

import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.categorization.components.NoSeriesMessage;
import org.designup.picsou.gui.categorization.components.NoSeriesMessageFactory;
import org.designup.picsou.gui.categorization.components.SeriesChooserComponentFactory;
import org.designup.picsou.gui.categorization.utils.FilteredRepeats;
import org.designup.picsou.gui.description.SeriesNameComparator;
import org.designup.picsou.gui.help.HelpAction;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class DeferredCardCategorizationPanel implements SpecialCategorizationPanel {
  public DeferredCardCategorizationPanel(FilteredRepeats seriesRepeat) {
  }

  public String getId() {
    return "deferredCard";
  }

  public JPanel loadPanel(GlobRepository repository,
                          Directory directory,
                          FilteredRepeats filteredRepeats,
                          SeriesEditionDialog seriesEditionDialog) {

    BudgetArea budgetArea = BudgetArea.OTHER;

    GlobsPanelBuilder panelBuilder = new GlobsPanelBuilder(CategorizationView.class,
                                                           "/layout/specialCategorizationPanels/deferredCardCategorizationPanel.splits",
                                                           repository, directory);

    panelBuilder.add("hyperlinkHandler", new HyperlinkHandler(directory));

    NoSeriesMessage noSeriesMessage = NoSeriesMessageFactory.create(budgetArea, repository, directory);
    panelBuilder.add("noSeriesMessage", noSeriesMessage.getComponent());

    JRadioButton invisibleRadio = new JRadioButton("noDeferredCard");
    panelBuilder.add("invisibleToggle", invisibleRadio);

    Matchers.CategorizationFilter filter = Matchers.seriesFilter(budgetArea.getId());
    GlobRepeat repeat = panelBuilder.addRepeat("seriesRepeat",
                                               Series.TYPE,
                                               filter,
                                               SeriesNameComparator.INSTANCE,
                                               new SeriesChooserComponentFactory(budgetArea, invisibleRadio,
                                                                                 seriesEditionDialog,
                                                                                 repository,
                                                                                 directory));
    filteredRepeats.add(filter, repeat);

    return panelBuilder.load();
  }

  public boolean canBeHidden() {
    return true;
  }
}
