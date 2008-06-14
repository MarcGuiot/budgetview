package org.globsframework.wicket;

import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import java.io.Serializable;

public interface FormSubmitListener extends Serializable {
  void onSubmit(Key key, FieldValues values, GlobRepository repository);
}
