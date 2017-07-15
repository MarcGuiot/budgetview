package com.budgetview.desktop.utils.datacheck.check;

import com.budgetview.desktop.utils.datacheck.DataCheckReport;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.model.Glob;

public class GlobCheck {
  public static void requiredFieldsAreSet(Glob glob, DataCheckReport report) {
    Field[] fields = glob.getType().getFields();
    for (Field field : fields) {
      if (field.hasAnnotation(Required.class) && glob.getValue(field) == null) {
        report.addError(field + " should not be null", glob.toString());
      }
    }
  }
}
