package org.designup.picsou.gui.categorization.special;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.gui.categorization.utils.FilteredRepeats;
import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.accounts.AccountEditionDialog;
import org.designup.picsou.utils.Lang;

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
                          SeriesEditionDialog seriesEditionDialog) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(CategorizationView.class,
                            "/layout/specialCategorizationPanels/htmlCategorizationPanel.splits",
                            repository, directory);

    builder.add("hyperlinkHandler", new HyperlinkHandler(directory) {
      protected void processCustomLink(String href) {
        if ("href".equals("createDeferredCardAccount")) {
          AccountEditionDialog dialog = new AccountEditionDialog(directory.get(JFrame.class),
                                                                 repository, directory);
          dialog.showWithNewAccount();
        }
      }
    });

    JEditorPane htmlEditor = new JEditorPane();
    GuiUtils.initReadOnlyHtmlComponent(htmlEditor);
    htmlEditor.setText(getMessageKey(repository, directory));
    builder.add("message", htmlEditor);

    return builder.load();
  }

  public boolean canBeHidden() {
    return true;
  }

  protected String getMessageKey(GlobRepository repository, Directory directory) {
    return Lang.get("categorization.specialCases." + id + ".message");
  }
}
