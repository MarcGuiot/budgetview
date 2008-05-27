package org.crossbowlabs.globs.gui.editors;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.utils.AbstractGlobComponentHolder;
import org.crossbowlabs.globs.gui.views.GlobComboView;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;

import javax.swing.*;

public class GlobLinkComboEditor extends AbstractGlobComponentHolder implements GlobSelectionListener {
  private Link link;
  private GlobComboView globComboView;
  private Glob selectedGlob;

  public GlobLinkComboEditor(final Link link, final GlobRepository repository, Directory directory) {
    super(link.getSourceType(), repository, directory);
    this.link = link;
    selectionService.addListener(this, link.getSourceType());

    globComboView =
      GlobComboView.init(link.getSourceType(), repository, directory)
        .setShowEmptyOption(true)
        .setUpdateWithIncomingSelections(false)
        .setSelectionHandler(new GlobComboView.GlobSelectionHandler() {
          public void processSelection(Glob glob) {
            if (selectedGlob == null) {
              return;
            }
            repository.setTarget(selectedGlob.getKey(), link,
                                 glob != null ? glob.getKey() : null);
          }
        });
    setSelectedGlob(null);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList globs = selection.getAll(link.getSourceType());
    if (globs.size() != 1) {
      setSelectedGlob(null);
      return;
    }
    setSelectedGlob(globs.get(0));
  }

  private void setSelectedGlob(Glob glob) {
    this.selectedGlob = glob;
    globComboView.getComponent().setEnabled(selectedGlob != null);
    Glob target = repository.findLinkTarget(glob, link);
    globComboView.select(target);
  }

  public JComboBox getComponent() {
    return globComboView.getComponent();
  }

  public void dispose() {
    selectionService.removeListener(this);
    globComboView.dispose();
  }
}