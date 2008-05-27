package org.designup.picsou.client.exceptions;

public class InvalidActionForState extends RemoteException {
  static public final int ID = 7;

  static {
    checkId(ID);
  }

  public InvalidActionForState(String action, String currentState) {
    super("Action '" + action + " not allowed from state: " + currentState);
  }


  public InvalidActionForState(String message) {
    super(message);
  }

  public void visit(Visitor visitor) {
    visitor.visitInvalidActionForState(this);
  }

  public int getId() {
    return ID;
  }

  public InvalidActionForState() {
    super("hidden");
  }
}
