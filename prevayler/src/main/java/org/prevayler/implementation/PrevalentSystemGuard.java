package org.prevayler.implementation;

import org.prevayler.Clock;
import org.prevayler.Query;
import org.prevayler.RWLock;
import org.prevayler.foundation.Cool;
import org.prevayler.foundation.DeepCopier;
import org.prevayler.foundation.serialization.Serializer;
import org.prevayler.implementation.publishing.TransactionPublisher;
import org.prevayler.implementation.publishing.TransactionSubscriber;
import org.prevayler.implementation.snapshot.GenericSnapshotManager;

import java.io.IOException;
import java.util.Date;
//import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PrevalentSystemGuard implements TransactionSubscriber {

  private Object _prevalentSystem; // All access to field is synchronized on "this", and all access to object is synchronized on itself; "this" is always locked before the object
  private long _systemVersion; // All access is synchronized on "this"
  private boolean _ignoreRuntimeExceptions; // All access is synchronized on "this"
  private final Serializer _journalSerializer;
  private RWLock rwLock = new RWLock();
  boolean useRWLock = System.getProperty("USE_RWLOCK") != null;

//  private ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock(true);
//  boolean useJavaRWLock = System.getProperty("USE_JAVA_RWLOCK") != null;

  public PrevalentSystemGuard(Object prevalentSystem, long systemVersion, Serializer journalSerializer) {
    _prevalentSystem = prevalentSystem;
    _systemVersion = systemVersion;
    _ignoreRuntimeExceptions = false;
    _journalSerializer = journalSerializer;
  }

  public Object prevalentSystem() {
    synchronized (this) {
      if (_prevalentSystem == null) {
        throw new ErrorInEarlierTransactionError("Prevayler is no longer allowing access to the prevalent system due to an Error thrown from an earlier transaction.");
      }
      return _prevalentSystem;
    }
  }

  public void subscribeTo(TransactionPublisher publisher) throws IOException, ClassNotFoundException {
    long initialTransaction;
    synchronized (this) {
      _ignoreRuntimeExceptions = true;     //During pending transaction recovery (rolling forward), RuntimeExceptions are ignored because they were already thrown and handled during the first transaction execution.
      initialTransaction = _systemVersion + 1;
    }

    publisher.subscribe(this, initialTransaction);

    synchronized (this) {
      _ignoreRuntimeExceptions = false;
    }
  }

  public void receive(TransactionTimestamp transactionTimestamp) {
    Capsule capsule = transactionTimestamp.capsule();
    long systemVersion = transactionTimestamp.systemVersion();
    Date executionTime = transactionTimestamp.executionTime();

    synchronized (this) {
      if (_prevalentSystem == null) {
        throw new ErrorInEarlierTransactionError("Prevayler is no longer processing transactions due to an Error thrown from an earlier transaction.");
      }

      if (systemVersion != _systemVersion + 1) {
        throw new IllegalStateException("Attempted to apply transaction " + systemVersion +
                                        " when prevalent system was only at " +
                                        _systemVersion);
      }

      _systemVersion = systemVersion;

      try {
        // Don't synchronize on _prevalentSystem here so that the capsule can deserialize a fresh
        // copy of the transaction without blocking queries.
        if (useRWLock) {
          capsule.executeOn(_prevalentSystem, executionTime, _journalSerializer, rwLock);
//        } else
//        if (useJavaRWLock) {
//          capsule.executeOn(_prevalentSystem, executionTime, _journalSerializer, reentrantReadWriteLock);
        }
        else {
          capsule.executeOn(_prevalentSystem, executionTime, _journalSerializer);
        }
      }
      catch (RuntimeException rx) {
        if (!_ignoreRuntimeExceptions) {
          throw rx;
        }
      }
      catch (Error error) {
        _prevalentSystem = null;
        throw error;
      }
      finally {
        notifyAll();
      }
    }
  }

  public Object executeQuery(Query sensitiveQuery, Clock clock) throws Exception {
    synchronized (this) {
      if (_prevalentSystem == null) {
        throw new ErrorInEarlierTransactionError("Prevayler is no longer processing queries due to an Error thrown from an earlier transaction.");
      }
      if (useRWLock) {
        return rwLock.excecuteRead(new ExecuteQueryAction(_prevalentSystem, sensitiveQuery, clock));
//    } else if (useJavaRWLock) {
//      try {
//        reentrantReadWriteLock.readLock().lock();
//        return sensitiveQuery.query(_prevalentSystem, clock.time());
//      } finally {
//        reentrantReadWriteLock.readLock().unlock();
//      }
      }
      else {
        synchronized (_prevalentSystem) {
          return sensitiveQuery.query(_prevalentSystem, clock.time());
        }
      }
    }
  }

  public void takeSnapshot(GenericSnapshotManager snapshotManager) throws IOException {
    synchronized (this) {
      if (_prevalentSystem == null) {
        throw new ErrorInEarlierTransactionError("Prevayler is no longer allowing snapshots due to an Error thrown from an earlier transaction.");
      }
      if (useRWLock) {
        synchronized (this) {
          try {
            rwLock.excecuteRead(new TakeSnapshot(snapshotManager, _prevalentSystem, _systemVersion));
          }
          catch (IOException e) {
            throw e;
          }
          catch (Exception e) {
            throw new RuntimeException("Unexpected Exception thrown.", e);
          }
        }
//    } else if (useJavaRWLock) {
//      try {
//        reentrantReadWriteLock.readLock().lock();
//        snapshotManager.writeSnapshot(_prevalentSystem, _systemVersion);
//      } finally {
//        reentrantReadWriteLock.readLock().unlock();
//      }
      }
      else {
        synchronized (this) {
          synchronized (_prevalentSystem) {
            snapshotManager.writeSnapshot(_prevalentSystem, _systemVersion);
          }
        }
      }
    }

  }

  public PrevalentSystemGuard deepCopy(long systemVersion, Serializer snapshotSerializer) throws IOException,
                                                                                                 ClassNotFoundException {
    if (useRWLock) {
      synchronized (this) {
        while (_systemVersion < systemVersion && _prevalentSystem != null) {
          Cool.wait(this);
        }

        if (_systemVersion > systemVersion) {
          throw new IllegalStateException("Already at " + _systemVersion + "; can't go back to " + systemVersion);
        }

        try {
          return (PrevalentSystemGuard)rwLock.excecuteRead(
            new DeepCopyAction(_prevalentSystem, snapshotSerializer, _systemVersion, _journalSerializer));
        }
        catch (IOException e) {
          throw e;
        }
        catch (ClassNotFoundException e) {
          throw e;
        }
        catch (Exception e) {
          throw new RuntimeException("Unexpected Exception thrown.", e);
        }
      }
//    } else if (useJavaRWLock) {
//      synchronized (this) {
//        while (_systemVersion < systemVersion) {
//          Cool.wait(this);
//        }
//
//        if (_systemVersion > systemVersion) {
//          throw new IllegalStateException("Already at " + _systemVersion + "; can't go back to " + systemVersion);
//        }
//
//        try {
//          reentrantReadWriteLock.readLock().lock();
//          return new PrevalentSystemGuard(DeepCopier.deepCopyParallel(_prevalentSystem, snapshotSerializer),
//                                          _systemVersion, _journalSerializer);
//        } finally {
//          reentrantReadWriteLock.readLock().unlock();
//        }
//      }
    }
    else {
      synchronized (this) {
        while (_systemVersion < systemVersion && _prevalentSystem != null) {
          Cool.wait(this);
        }

        if (_systemVersion > systemVersion) {
          throw new IllegalStateException("Already at " + _systemVersion + "; can't go back to " + systemVersion);
        }
        prevalentSystem();
        synchronized (_prevalentSystem) {
          return new PrevalentSystemGuard(DeepCopier.deepCopyParallel(_prevalentSystem, snapshotSerializer),
                                          _systemVersion, _journalSerializer);
        }
      }
    }
  }

  static class ExecuteQueryAction implements RWLock.Action {
    private Object _prevalentSystem;
    private Query _sensitiveQuery;
    private Clock _clock;

    public ExecuteQueryAction(Object prevalentSystem, Query sensitiveQuery, Clock clock) {
      this._prevalentSystem = prevalentSystem;
      this._sensitiveQuery = sensitiveQuery;
      this._clock = clock;
    }

    public Object run() throws Exception {
      return _sensitiveQuery.query(_prevalentSystem, _clock.time());
    }
  }

  static class TakeSnapshot implements RWLock.Action {
    private GenericSnapshotManager _snapshotManager;
    private Object _prevalentSystem;
    private long _systemVersion;

    public TakeSnapshot(GenericSnapshotManager snapshotManager, Object prevalentSystem, long systemVersion) {
      _snapshotManager = snapshotManager;
      _prevalentSystem = prevalentSystem;
      _systemVersion = systemVersion;
    }

    public Object run() throws Exception {
      _snapshotManager.writeSnapshot(_prevalentSystem, _systemVersion);
      return null;
    }
  }

  static class DeepCopyAction implements RWLock.Action {
    private Object _prevalentSystem;
    private Serializer _snapshotSerializer;
    private long _systemVersion;
    private Serializer _journalSerializer;

    public DeepCopyAction(Object prevalentSystem, Serializer snapshotSerializer,
                          long systemVersion, Serializer journalSerializer) {
      _prevalentSystem = prevalentSystem;
      _snapshotSerializer = snapshotSerializer;
      _systemVersion = systemVersion;
      _journalSerializer = journalSerializer;
    }

    public Object run() throws Exception {
      return new PrevalentSystemGuard(DeepCopier.deepCopyParallel(_prevalentSystem, _snapshotSerializer),
                                      _systemVersion, _journalSerializer);
    }
  }

}
