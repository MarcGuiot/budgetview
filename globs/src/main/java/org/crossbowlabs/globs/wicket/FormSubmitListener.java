package org.crossbowlabs.globs.wicket;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import java.io.Serializable;

public interface FormSubmitListener extends Serializable {
  void onSubmit(Key key, FieldValues values, GlobRepository repository);
}
