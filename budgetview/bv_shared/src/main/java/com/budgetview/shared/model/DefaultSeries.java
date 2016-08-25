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
  INCOME("income1", 1),
  RENT("rent", 2),
  ELECTRICITY("electricity", 3),
  GAS("gas", 4),
  WATER("water", 5),
  CAR_CREDIT("carCredit", 6),
  CAR_INSURANCE("carInsurance", 7),
  INCOME_TAXES("incomeTaxes", 8),
  CELL_PHONE("cellPhone", 9),
  INTERNET("internet", 11),
  FIXED_PHONE("fixedPhone", 12),
  GROCERIES("groceries", 13),
  HEALTH("health", 14),
  PHYSICIAN("physician", 15),
  PHARMACY("pharmacy", 16),
  REIMBURSEMENTS("reimbursements", 17),
  LEISURES("leisures", 18),
  CLOTHING("clothing", 19),
  BEAUTY("beauty", 20),
  FUEL("fuel", 21),
  CASH("cash", 22),
  BANK_FEES("bankFees", 23),
  RESTAURANT("restaurant", 24),
  MISC("misc", 25);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private String name;
  private int id;

  DefaultSeries(String name, int id) {
    this.name = name;
    this.id = id;
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

  public int getId() {
    return id;
  }

}
