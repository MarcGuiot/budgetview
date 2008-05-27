package org.designup.picsou.client.exceptions;

public class UserNotRegistered extends RemoteException {
  public static final int ID = 6;

  static {
    checkId(ID);
  }

  public UserNotRegistered(String details) {
    super(details);
  }

  public void visit(Visitor visitor) {
    visitor.visitUserNotRegistered(this);
  }

  public int getId() {
    return ID;
  }

}
