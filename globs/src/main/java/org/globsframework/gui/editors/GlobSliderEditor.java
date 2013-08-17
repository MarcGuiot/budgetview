package org.globsframework.gui.editors;

import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Set;

public class GlobSliderEditor extends AbstractGlobFieldEditor<GlobSliderEditor> {

  private DoubleField field;
  private GlobSliderAdapter adapter;
  private JSlider slider;

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
  }

  public JSlider getComponent() {
    return slider;
  }

  public void dispose() {
    super.dispose();
    slider = null;
  }

  protected void updateFromGlobs() {
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
