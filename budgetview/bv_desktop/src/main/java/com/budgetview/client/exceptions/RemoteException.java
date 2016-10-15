package com.budgetview.client.exceptions;

import org.globsframework.utils.exceptions.GlobsException;

import java.util.HashSet;
import java.util.Set;

public abstract class RemoteException extends GlobsException {
  private static Set<Integer> IDS = new HashSet<Integer>();

  public RemoteException(String message) {
    super(message);
  }

  public RemoteException(Exception e) {
    super(e);
  }

  public abstract void visit(Visitor visitor);

  public abstract int getId();

  public interface Visitor {
    void visitBadConnection(BadConnection ex);

    void visitBadPassword(BadPassword ex);

    void visitIdentificationFailed(IdentificationFailed ex);

    void visitUserAlreadyExists(UserAlreadyExists ex);

    void visitUserNotRegistered(UserNotRegistered ex);

    void visitUnknownId(UnknownId ex);

    void visitInvalidActionForState(InvalidActionForState ex);

    void visitUnableToWrite(UnableToWrite write);
  }

  protected static void checkId(int id) {
    if (IDS.contains(id)) {
      throw new RuntimeException("Id already in use");
    }
    IDS.add(id);
  }
}
