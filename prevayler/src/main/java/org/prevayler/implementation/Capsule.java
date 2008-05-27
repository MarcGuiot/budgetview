package org.prevayler.implementation;

import org.prevayler.RWLock;
import org.prevayler.foundation.Chunk;
import org.prevayler.foundation.serialization.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.Date;
//import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class Capsule implements Serializable {

  private final byte[] _serialized;

  protected Capsule(Object transaction, Serializer journalSerializer) {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      journalSerializer.writeObject(bytes, transaction);
      _serialized = bytes.toByteArray();
    } catch (Exception exception) {
      throw new TransactionNotSerializableError("Unable to serialize transaction", exception);
    }
  }

  protected Capsule(byte[] serialized) {
    _serialized = serialized;
  }

  /**
   * Get the serialized representation of the transaction. Callers must not modify the returned array.
   */
  public byte[] serialized() {
    return _serialized;
  }

  /**
   * Deserialize the contained Transaction or TransactionWithQuery.
   */
  public Object deserialize(Serializer journalSerializer) {
    try {
      return journalSerializer.readObject(new ByteArrayInputStream(_serialized));
    } catch (Exception exception) {
      throw new TransactionNotSerializableError("Unable to deserialize transaction", exception);
    }
  }


  /**
   * Execute a freshly deserialized copy of the transaction. This method will synchronize on the prevalentSystem
   * while running the transaction but after deserializing it.
   */
  public void executeOn(Object prevalentSystem, Date executionTime, Serializer journalSerializer) {
    Object transaction = deserialize(journalSerializer);

    synchronized (prevalentSystem) {
      justExecute(transaction, prevalentSystem, executionTime);
    }
  }

  /**
   * Execute a freshly deserialized copy of the transaction. This method will synchronize on the prevalentSystem
   * while running the transaction but after deserializing it.
   */
  public void executeOn(Object prevalentSystem, Date executionTime, Serializer journalSerializer, RWLock rwLock) {
    Object transaction = deserialize(journalSerializer);

    try {
      rwLock.excecuteWrite(new Execute(this, transaction, prevalentSystem, executionTime));
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new TransactionNotSerializableError("Unexpected Exception thrown.", e);
    }
  }

//  public void executeOn(Object prevalentSystem, Date executionTime, Serializer journalSerializer,
//                        ReentrantReadWriteLock reentrantReadWriteLock) {
//    Object transaction = deserialize(journalSerializer);
//
//    try {
//      reentrantReadWriteLock.writeLock().lock();
//      justExecute(transaction, prevalentSystem, executionTime);
//    } catch (RuntimeException e) {
//      throw e;
//    } catch (Exception e) {
//      throw new RuntimeException("Unexpected Exception thrown.", e);
//    } finally {
//      reentrantReadWriteLock.writeLock().unlock();
//    }
//  }


  /**
   * Actually execute the Transaction or TransactionWithQuery. The caller
   * is responsible for synchronizing on the prevalentSystem.
   */
  protected abstract void justExecute(Object transaction, Object prevalentSystem, Date executionTime);

  /**
   * Make a clean copy of this capsule that will have its own query result fields.
   */
  public abstract Capsule cleanCopy();

  Chunk toChunk() {
    Chunk chunk = new Chunk(_serialized);
    chunk.setParameter("withQuery", String.valueOf(this instanceof TransactionWithQueryCapsule));
    return chunk;
  }

  static Capsule fromChunk(Chunk chunk) {
    boolean withQuery = Boolean.valueOf(chunk.getParameter("withQuery")).booleanValue();
    if (withQuery) {
      return new TransactionWithQueryCapsule(chunk.getBytes());
    } else {
      return new TransactionCapsule(chunk.getBytes());
    }
  }

  static private final class Execute implements RWLock.Action {
    private Capsule _capsule;
    private Object _transaction;
    private Object _prevalentSystem;
    private Date _executionTime;

    public Execute(Capsule capsule, Object transaction, Object prevalentSystem, Date executionTime) {
      _capsule = capsule;
      _transaction = transaction;
      _prevalentSystem = prevalentSystem;
      _executionTime = executionTime;
    }

    final public Object run() throws Exception {
      _capsule.justExecute(_transaction, _prevalentSystem, _executionTime);
      return null;
    }
  }
}
