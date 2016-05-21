package com.budgetview.gui.categorization.special;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import com.budgetview.gui.categorization.utils.FilteredRepeats;
import com.budgetview.gui.categorization.utils.SeriesCreationHandler;
import com.budgetview.gui.categorization.CategorizationView;
import com.budgetview.gui.help.HyperlinkHandler;
import com.budgetview.utils.Lang;

import javax.swing.*;

public class HtmlCategorizationPanel implements SpecialCategorizationPanel {

  private String id;

  public HtmlCategorizationPanel(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public JPanel loadPanel(final GlobRepository repository,
                          final Directory directory,
                          FilteredRepeats filteredRepeats,
                          SeriesCreationHandler seriesCreationHandler) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(CategorizationView.class,
                            "/layout/categorization/specialCategorizationPanels/htmlCategorizationPanel.splits",
                            repository, directory);

    builder.add("hyperlinkHandler", createHyperlinkHandler(repository, directory,
                                                           seriesCreationHandler));

    JEditorPane htmlEditor = GuiUtils.createReadOnlyHtmlComponent(getMessageKey(repository, directory));
    builder.add("message", htmlEditor);

    return builder.load();
  }

  protected HyperlinkHandler createHyperlinkHandler(final GlobRepository repository, final Directory directory,
                                                    SeriesCreationHandler seriesCreationHandler) {
    HyperlinkHandler handler = new HyperlinkHandler(directory);
    registerHyperlinkActions(handler, repository, directory, seriesCreationHandler);
    return handler;
  }

  protected void registerHyperlinkActions(HyperlinkHandler handler,
                                          GlobRepository repository,
                                          Directory directory,
                                          SeriesCreationHandler seriesCreationHandler) {
  }

  protected String getMessageKey(GlobRepository repository, Directory directory) {
    return Lang.get("categorization.specialCases." + id + ".message");
  }

  public void registerController(SpecialCategorizationPanelController controller) {
  }
}
