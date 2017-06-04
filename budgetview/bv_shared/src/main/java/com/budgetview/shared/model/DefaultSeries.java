package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;

import static org.globsframework.model.FieldValue.value;

public enum DefaultSeries implements GlobConstantContainer {
  INCOME("income", 1, BudgetArea.INCOME),
  INTEREST("interest", 29, BudgetArea.INCOME),

  RENT("rent", 2, BudgetArea.RECURRING),
  ELECTRICITY("electricity", 3, BudgetArea.RECURRING),
  GAS("gas", 4, BudgetArea.RECURRING),
  WATER("water", 5, BudgetArea.RECURRING),
  CAR_CREDIT("carCredit", 6, BudgetArea.RECURRING),
  CAR_INSURANCE("carInsurance", 7, BudgetArea.RECURRING),
  INCOME_TAXES("incomeTaxes", 8, BudgetArea.RECURRING),
  HOUSING_TAXES("housingTaxes", 28, BudgetArea.RECURRING),
  PHONE("phone", 9, BudgetArea.RECURRING),
  INTERNET("internet", 11, BudgetArea.RECURRING),
  HEALTH_INSURANCE("healthInsurance", 28, BudgetArea.RECURRING),

  GROCERIES("groceries", 13, BudgetArea.VARIABLE),
  HOME("home", 33, BudgetArea.VARIABLE),
  HEALTH("health", 14, BudgetArea.VARIABLE),
  PHYSICIAN("physician", 15, BudgetArea.VARIABLE),
  PHARMACY("pharmacy", 16, BudgetArea.VARIABLE),
  REIMBURSEMENTS("reimbursements", 17, BudgetArea.VARIABLE),
  LEISURES("leisures", 18, BudgetArea.VARIABLE),
  CLOTHING("clothing", 19, BudgetArea.VARIABLE),
  BEAUTY("beauty", 20, BudgetArea.VARIABLE),
  FUEL("fuel", 21, BudgetArea.VARIABLE),
  CASH("cash", 22, BudgetArea.VARIABLE),
  BANK_FEES("bankFees", 23, BudgetArea.VARIABLE),
  RESTAURANT("restaurant", 24, BudgetArea.VARIABLE),
  MISC("misc", 25, BudgetArea.VARIABLE),

  GIFTS("gifts", 26, BudgetArea.VARIABLE),
  ANIMALS("animals", 27, BudgetArea.VARIABLE),
  TOLL("toll", 30, BudgetArea.VARIABLE),
  PARKING("parking", 31, BudgetArea.VARIABLE),
  TRANSPORT("transport", 32, BudgetArea.VARIABLE),

  UNCATEGORIZED("uncategorized", -1, BudgetArea.UNCATEGORIZED);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private final String name;
  private final int id;
  private final BudgetArea budgetArea;

  DefaultSeries(String name, int id, BudgetArea budgetArea) {
    this.name = name;
    this.id = id;
    this.budgetArea = budgetArea;
  }

  static {
    GlobTypeLoader.init(DefaultSeries.class, "defaultSeries");
  }

  public String getName() {
    return name;
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(Provider.TYPE,
                            value(ID, id),
                            value(NAME, name));
  }

  public static DefaultSeries find(int id) {
    for (DefaultSeries defaultSeries : values()) {
      if (defaultSeries.id == id) {
        return defaultSeries;
      }
    }
    return null;
  }

  public int getId() {
    return id;
  }

  public String toString() {
    return id + "|" + name;
  }

  public BudgetArea getBudgetArea() {
    return budgetArea;
  }
}
