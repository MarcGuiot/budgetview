package org.designup.picsou.client.exceptions;

public class BadPassword extends RemoteException {
  public static final int ID = 2;

  static {
    checkId(ID);
  }

  public BadPassword(String details) {
    super(details);
  }

  public void visit(Visitor visitor) {
    visitor.visitBadPassword(this);
  }

  public int getId() {
    return ID;
  }

}
