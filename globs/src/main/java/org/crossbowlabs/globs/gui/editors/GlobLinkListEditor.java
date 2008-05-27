package org.crossbowlabs.globs.gui.editors;

import org.crossbowlabs.globs.gui.GlobSelection;
import org.crossbowlabs.globs.gui.GlobSelectionListener;
import org.crossbowlabs.globs.gui.utils.AbstractGlobComponentHolder;
import org.crossbowlabs.globs.gui.views.GlobListView;
import org.crossbowlabs.globs.metamodel.Link;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;

import javax.swing.*;

public class GlobLinkListEditor extends AbstractGlobComponentHolder implements GlobSelectionListener {
  private Link link;
  private GlobListView globListView;
  private Glob selectedGlob;

  public GlobLinkListEditor(final Link link, final GlobRepository repository, Directory directory) {
    super(link.getSourceType(), repository, directory);
    this.link = link;
    selectionService.addListener(this, type);

    globListView =
      GlobListView.init(link.getSourceType(), repository, directory)
        .setShowEmptyOption(true)
        .setUpdateWithIncomingSelections(false)
        .setSelectionHandler(new GlobListView.GlobSelectionHandler() {
          public void processSelection(GlobList selection) {
            if (selectedGlob == null) {
              return;
            }
            Glob target = selection.size() == 1 ? selection.get(0) : null;
            repository.setTarget(selectedGlob.getKey(), link, target != null ? target.getKey() : null);
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
    globListView.getComponent().setEnabled(selectedGlob != null);
    Glob target = repository.findLinkTarget(glob, link);
    if (target != null) {
      globListView.select(target);
    }
    else {
      globListView.select();
    }
  }

  public JList getComponent() {
    return globListView.getComponent();
  }

  public void dispose() {
    globListView.dispose();
    selectionService.removeListener(this);
  }
}
