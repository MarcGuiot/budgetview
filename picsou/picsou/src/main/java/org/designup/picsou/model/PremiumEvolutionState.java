package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.InvalidParameter;

import static org.globsframework.model.FieldValue.value;

public enum PremiumEvolutionState implements GlobConstantContainer {
  FREE(0, false, false),
  TRIAL_IN_PROGRESS(1, true, false),
  TRIAL_OVER(2, false, false),
  REGISTERED(3, true, true),
  REGISTRATION_EXPIRING(4, true, true),
  FREE_AFTER_REGISTRATION(5, false, false);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private int id;
  private final boolean premiumFeaturesEnabled;
  private final boolean userIsRegistered;

  PremiumEvolutionState(int id,boolean premiumFeaturesEnabled,boolean userIsRegistered) {
    this.id = id;
    this.premiumFeaturesEnabled = premiumFeaturesEnabled;
    this.userIsRegistered = userIsRegistered;
  }

  static {
    GlobTypeLoader.init(PremiumEvolutionState.class, "freemiumState");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(TYPE, value(ID, id));
  }

  public int getId() {
    return id;
  }

  public static PremiumEvolutionState get(Integer state) {
    if (state == null) {
      return null;
    }
    switch (state) {
      case 0:
        return FREE;
      case 1:
        return TRIAL_IN_PROGRESS;
      case 2:
        return TRIAL_OVER;
      case 3:
        return REGISTERED;
      case 4:
        return REGISTRATION_EXPIRING;
      case 5:
        return FREE_AFTER_REGISTRATION;
    }
    throw new InvalidParameter("Unexpected value " + state);
  }

  public boolean isPremiumFeaturesEnabled() {
    return premiumFeaturesEnabled;
  }

  public boolean isRegistered() {
    return userIsRegistered;
  }
}
