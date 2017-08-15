package com.budgetview.desktop.utils.datacheck.check;

import com.budgetview.desktop.model.DesktopModel;
import com.budgetview.desktop.utils.datacheck.DataCheckReport;
import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class GlobCheck {

  public static void requiredFieldsAreSet(Glob glob, DataCheckReport report) {
    for (Field field : glob.getType().getFields()) {
      if (field.isRequired() && glob.getValue(field) == null) {
        report.addError(field + " should not be null", glob.toString());
      }
    }
  }

  public static void linksAreAllConnected(GlobRepository repository, DataCheckReport report) {
    for (GlobType type : DesktopModel.getUserSpecificTypes()) {
      for (Glob glob : repository.getAll()) {
        for (Field field : glob.getType().getFields()) {
          if (field.isRequired() && glob.getValue(field) == null) {
            report.addError(field + " should not be null", glob.toString());
          }
          if (field instanceof LinkField) {
            Object value = glob.getValue(field);
            if ((value != null) && repository.findLinkTarget(glob, (LinkField)field) == null) {
              report.addError("Link " + field + " has value " + value + " but no corresponding object", glob.toString());
            }
          }
        }
      }
    }
  }
}
