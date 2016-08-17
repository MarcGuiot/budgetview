package com.budgetview.desktop.importer.components;

import com.budgetview.budgea.model.BudgeaBankField;
import com.budgetview.budgea.model.BudgeaBankFieldType;
import com.budgetview.budgea.model.BudgeaConnectionValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

public class CloudConnectionFieldEditorFactory {
  public static CloudConnectionFieldEditor create(Glob budgeaConnectionValue, GlobRepository repository, Directory directory) {
    Glob field = repository.findLinkTarget(budgeaConnectionValue, BudgeaConnectionValue.FIELD);
    if (field == null) {
      throw new InvalidParameter("Value not linked to a field: " + budgeaConnectionValue);
    }
    BudgeaBankFieldType fieldType = BudgeaBankField.getFieldType(field);
    switch (fieldType) {
      case LIST:
        return new CloudConnectionListFieldEditor(field, budgeaConnectionValue, repository, directory);
      case TEXT:
        return new CloudConnectionNameFieldEditor(field, budgeaConnectionValue, repository, directory);
      case PASSWORD:
        return new CloudConnectionPasswordFieldEditor(field, budgeaConnectionValue, repository, directory);
      case DATE:
        return new CloudConnectionDateFieldEditor(field, budgeaConnectionValue, repository, directory);
      default:
        throw new InvalidParameter("Unexpected field type: " + fieldType);
    }
  }
}
