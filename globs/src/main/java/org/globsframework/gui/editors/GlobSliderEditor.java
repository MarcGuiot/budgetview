package org.globsframework.gui.editors;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.utils.AbstractGlobComponentHolder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Set;

public class GlobSliderEditor extends AbstractGlobComponentHolder<GlobSliderEditor>
  implements ChangeSetListener, GlobSelectionListener {

  private DoubleField field;
  private GlobList currentGlobs = new GlobList();
  private GlobSliderAdapter adapter;
  private JSlider slider;
  private boolean isAdjusting;

  public static GlobSliderEditor init(DoubleField field, GlobRepository repository, Directory directory, GlobSliderAdapter adapter) {
    return new GlobSliderEditor(field, repository, directory, adapter);
  }

  private GlobSliderEditor(DoubleField field, GlobRepository repository, Directory directory, GlobSliderAdapter adapter) {
    super(field.getGlobType(), repository, directory);
    this.field = field;
    this.adapter = adapter;

    this.slider = new JSlider();
    slider.setPaintLabels(false);
    slider.setEnabled(false);
    slider.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        updateGlobsFromSlider();
      }
    });

    adapter.init(slider);

    repository.addChangeListener(this);
    selectionService.addListener(this, type);
  }

  public JSlider getComponent() {
    return slider;
  }

  public void dispose() {
    repository.removeChangeListener(this);
    selectionService.removeListener(this);
    slider = null;
  }

  public void selectionUpdated(GlobSelection selection) {
    this.currentGlobs = selection.getAll(type);
    updateSliderFromGlobs();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (isAdjusting || !changeSet.containsChanges(type)) {
      return;
    }

    currentGlobs.keepExistingGlobsOnly(repository);
    updateSliderFromGlobs();
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(type)) {
      currentGlobs.keepExistingGlobsOnly(repository);
      updateSliderFromGlobs();
    }
  }

  private void updateSliderFromGlobs() {
    if (isAdjusting) {
      return;
    }
    isAdjusting = true;
    try {
      if (currentGlobs.size() == 0) {
        slider.setValue(0);
        // Used to circumvent AquaLabelUI exception when displaying disabled labels
        slider.setPaintLabels(false);
        slider.setEnabled(false);
        return;
      }

      slider.setEnabled(true);
      slider.setPaintLabels(true);

      Set<Double> values = currentGlobs.getSortedSet(field);
      Double newValue = values.size() > 0 ? values.iterator().next() : null;
      if (values.size() > 1) {
        for (Double value : values) {
          newValue = Math.max(value, newValue);
        }
      }

      adapter.setSliderValue(newValue, slider, currentGlobs);
    }
    finally {
      isAdjusting = false;
    }
  }

  private void updateGlobsFromSlider() {
    if (isAdjusting) {
      return;
    }

    try {
      isAdjusting = true;
      Double adaptedValue = adapter.convertToGlobsValue(slider.getValue());
      repository.startChangeSet();
      for (Glob glob : currentGlobs) {
        repository.update(glob.getKey(), field, adaptedValue);
      }
    }
    finally {
      repository.completeChangeSet();
      isAdjusting = false;
    }
  }
}
