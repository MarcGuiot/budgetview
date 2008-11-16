package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.gui.views.GlobComboView;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.Link;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Comparator;

public class GlobLinkComboEditor extends AbstractGlobComponentHolder implements GlobSelectionListener {
  private Link link;
  private GlobComboView globComboView;
  private Glob selectedGlob;
  private boolean updateInProgress = false;

  public GlobLinkComboEditor(final Link link, final GlobRepository repository, Directory directory) {
    super(link.getTargetType(), repository, directory);
    this.link = link;
    selectionService.addListener(this, link.getSourceType());

    globComboView =
      GlobComboView.init(link.getTargetType(), repository, directory)
        .setShowEmptyOption(true)
        .setUpdateWithIncomingSelections(false)
        .setSelectionHandler(new GlobComboView.GlobSelectionHandler() {
          public void processSelection(Glob glob) {
            if (selectedGlob == null) {
              return;
            }
            if (!updateInProgress) {
              repository.setTarget(selectedGlob.getKey(), link,
                                   glob != null ? glob.getKey() : null);
            }
          }
        });
  }

  public void selectionUpdated(GlobSelection selection) {
    updateInProgress = true;
    try {
      GlobList globs = selection.getAll(link.getTargetType());
      select(globs);
    }
    finally {
      updateInProgress = false;
    }
  }

  private void select(GlobList globs) {
    if (globs.size() != 1) {
      setSelectedGlob(null);
    }
    else {
      setSelectedGlob(globs.get(0));
    }
  }

  public GlobLinkComboEditor setFilter(final GlobMatcher filter) {
    globComboView.setFilter(filter);
    return this;
  }


  public GlobLinkComboEditor setRenderer(Field field) {
    globComboView.setRenderer(field);
    return this;
  }

  public GlobLinkComboEditor setRenderer(GlobStringifier stringifier) {
    globComboView.setRenderer(stringifier);
    return this;
  }

  public GlobLinkComboEditor setRenderer(ListCellRenderer renderer, Comparator<Glob> comparator) {
    globComboView.setRenderer(renderer, comparator);
    return this;
  }

  public GlobLinkComboEditor setComparator(final Comparator<Glob> comparator) {
    globComboView.setComparator(comparator);
    return this;
  }

  public GlobLinkComboEditor setShowEmptyOption(boolean showEmpty) {
    globComboView.setShowEmptyOption(showEmpty);
    return this;
  }

  public GlobLinkComboEditor setEmptyOptionLabel(String label) {
    globComboView.setEmptyOptionLabel(label);
    return this;
  }

  private void setSelectedGlob(Glob glob) {
    this.selectedGlob = glob;
    globComboView.getComponent().setEnabled(selectedGlob != null);
    Glob target = repository.findLinkTarget(glob, link);
    globComboView.select(target);
  }

  public GlobLinkComboEditor setName(String name) {
    super.setName(name);
    return this;
  }

  public JComboBox getComponent() {
    JComboBox jComboBox = globComboView.getComponent();
    GlobList selection = selectionService.getSelection(type);
    select(selection);
    return jComboBox;
  }

  public void dispose() {
    selectionService.removeListener(this);
    globComboView.dispose();
  }

  public void setEnable(boolean enable) {
    globComboView.setEnable(enable);
  }
}