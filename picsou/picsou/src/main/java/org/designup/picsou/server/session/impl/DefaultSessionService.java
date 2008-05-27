package org.designup.picsou.server.session.impl;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.designup.picsou.client.exceptions.UnknownId;
import org.designup.picsou.server.session.ConnectingState;
import org.designup.picsou.server.session.Persistence;
import org.designup.picsou.server.session.SessionService;
import org.designup.picsou.server.session.SessionState;

import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultSessionService implements SessionService {
  private Directory directory;
  private ConcurrentMap<Long, SessionState> sessions = new ConcurrentHashMap<Long, SessionState>();
  private Random random = new SecureRandom();

  public DefaultSessionService(Directory directory) {
    this.directory = directory;
  }

  public ConnectingState createSessionState() {
    Long sessionId = getNewSessionId();
    byte[] privateId = generatePrivateId();
    return new DefaultConnectingState(directory.get(Persistence.class), this, privateId, sessionId);
  }

  private byte[] generatePrivateId() {
    byte[] bytes = new byte[30];
    random.nextBytes(bytes);
    return bytes;
  }

  public SessionState getSessionState(Long sessionId) {
    SessionState sessionState = sessions.get(sessionId);
    if (sessionState == null) {
      throw new UnknownId();
    }
    return sessionState;
  }

  private Long getNewSessionId() {
    Long sessionId = random.nextLong();
    while (sessions.containsKey(sessionId)) {
      sessionId = random.nextLong();
    }
    return sessionId;
  }

  public void register(Long sessionId, SessionState sessionState) {
    sessions.put(sessionId, sessionState);
  }

  public void remove(Long sessionId) {
    sessions.remove(sessionId);
  }

  public GlobModel getModel() {
    return directory.get(GlobModel.class);
  }

  public void flushStateBefore(long date) {
    for (Iterator<SessionState> iterator = sessions.values().iterator(); iterator.hasNext();) {
      SessionState sessionState = iterator.next();
      if (sessionState.getLastAccess() < date) {
        iterator.remove();
      }
    }
  }

}
