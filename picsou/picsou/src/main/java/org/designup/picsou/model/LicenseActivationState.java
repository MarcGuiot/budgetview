package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.InvalidParameter;

import static org.globsframework.model.FieldValue.value;

public enum LicenseActivationState implements GlobConstantContainer {
  ACTIVATION_IN_PROGRESS(1),
  ACTIVATION_OK(2),
  ACTIVATION_FAILED_BAD_SIGNATURE(3),
  ACTIVATION_FAILED_CAN_NOT_CONNECT(4),
  ACTIVATION_FAILED_HTTP_REQUEST(5),
  ACTIVATION_FAILED_MAIL_UNKNOWN(6),
  // On n'a pas envoyé au serveur les mail, signature et code d'activation alors que c'est un utilisateur
  // enregistré (donc a priori le repo a ete modifié manuellement)
  ACTIVATION_FAILED_MAIL_SENT(8),
  ACTIVATION_FAILED_MAIL_NOT_SENT(9),
  STARTUP_CHECK_KILL_USER(10),
  STARTUP_CHECK_MAIL_SENT(11),
  STARTUP_CHECK_JAR_VERSION(12);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private int id;

  LicenseActivationState(int id) {
    this.id = id;
  }

  static {
    GlobTypeLoader.init(LicenseActivationState.class, "userActivationState");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(TYPE, value(ID, id));
  }

  public int getId() {
    return id;
  }

  public static LicenseActivationState get(Integer state) {
    if (state == null) {
      return null;
    }
    switch (state) {
      case 1:
        return ACTIVATION_IN_PROGRESS;
      case 2:
        return ACTIVATION_OK;
      case 3:
        return ACTIVATION_FAILED_BAD_SIGNATURE;
      case 4:
        return ACTIVATION_FAILED_CAN_NOT_CONNECT;
      case 5:
        return ACTIVATION_FAILED_HTTP_REQUEST;
      case 6:
        return ACTIVATION_FAILED_MAIL_UNKNOWN;
      // On n'a pas envoyé au serveur les mail, signature et code d'activation alors que c'est un utilisateur
      // enregistré (donc a priori le repo a ete modifié manuellement)
      case 8:
        return ACTIVATION_FAILED_MAIL_SENT;
      case 9:
        return ACTIVATION_FAILED_MAIL_NOT_SENT;
      case 10:
        return STARTUP_CHECK_KILL_USER;
      case 11:
        return STARTUP_CHECK_MAIL_SENT;
      case 12:
        return STARTUP_CHECK_JAR_VERSION;
    }
    throw new InvalidParameter("Unexpected value " + state);
  }
}
