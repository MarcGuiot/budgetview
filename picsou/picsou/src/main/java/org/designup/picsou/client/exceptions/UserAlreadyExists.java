package org.designup.picsou.client.exceptions;

public class UserAlreadyExists extends RemoteException {
  public static final int ID = 5;

  static {
    checkId(ID);
  }

  public UserAlreadyExists(String details) {
    super(details);
  }

  public void visit(Visitor visitor) {
    visitor.visitUserAlreadyExists(this);
  }

  public int getId() {
    return ID;
  }

}
