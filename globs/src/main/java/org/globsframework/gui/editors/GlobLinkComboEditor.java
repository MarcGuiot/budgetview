package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.metamodel.Link;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

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