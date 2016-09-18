package com.budgetview.budgea.model;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.DefaultGlobModel;

public class BudgeaModel {

  private static GlobModel MODEL = new DefaultGlobModel(
    BudgeaBank.TYPE,
    BudgeaBankField.TYPE,
    BudgeaBankFieldType.TYPE,
    BudgeaBankFieldValue.TYPE,
    BudgeaConnection.TYPE,
    BudgeaConnectionValue.TYPE
  );

  public static GlobModel get() {
    return MODEL;
  }
}
