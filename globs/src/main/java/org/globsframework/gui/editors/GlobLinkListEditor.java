package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.views.GlobListView;
import org.globsframework.metamodel.Link;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

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
    if (globListView != null) {
      globListView.dispose();
      selectionService.removeListener(this);
      globListView = null;
    }
  }
}
