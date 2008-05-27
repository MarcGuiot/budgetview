package org.prevayler.demos.scalability.prevayler.rwlock;

import org.prevayler.Prevayler;
import org.prevayler.PrevaylerFactory;
import org.prevayler.Query;
import org.prevayler.Transaction;
import org.prevayler.demos.scalability.Record;
import org.prevayler.demos.scalability.RecordIterator;
import org.prevayler.demos.scalability.prevayler.QuerySystem;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class RWLockTestRun {
  private int readThreadCount;
  private int writeThreadCount;
  private Prevayler prevayler;
  private volatile boolean shouldContinue = true;
  private ReadThread[] readThreads;
  private WriteThread[] writeThreads;
  private int total;
  private boolean started = false;

  public RWLockTestRun(int readThreadCount, int writeThreadCount,
                       Prevayler prevayler) {
    this.readThreadCount = readThreadCount;
    this.writeThreadCount = writeThreadCount;
    this.prevayler = prevayler;
  }

  void run() throws InterruptedException {

    ((QuerySystem) prevayler.prevalentSystem()).replaceAllRecords(new RecordIterator(10000));
     readThreads = new ReadThread[readThreadCount];
    for (int i = 0; i < readThreadCount; i++) {
      readThreads[i] = new ReadThread(1000 * i);
      readThreads[i].start();
    }
    writeThreads = new WriteThread[writeThreadCount];
     for (int i = 0; i < writeThreadCount; i++) {
       writeThreads[i] = new WriteThread(1000 * i);
       writeThreads[i].start();
     }

    Thread.sleep(2 * 1000);
    synchronized(this){
      started = true;
      notifyAll();
    }
    long begin = System.currentTimeMillis();
    Thread.sleep(20 * 1000);
    synchronized (this) {
      shouldContinue = false;
    }
    for (int i = 0; i < readThreadCount; i++) {
      readThreads[i].join();
    }
    for (int i = 0; i < writeThreadCount; i++) {
      writeThreads[i].join();
    }
    long end = System.currentTimeMillis();

    for (int i = 0; i < writeThreads.length; i++) {
      WriteThread writeThread = writeThreads[i];
      System.out.println("write in : " + (end - begin) / 1000
                         + "s ==> " + ((1000 * writeThread.count) / (end - begin))
                         + " w/s");
      total += writeThread.count;
    }
    System.out.println("total write tr/s : " + ((total * 1000) / (end - begin)));
    long totalReadCount = 0;
    for (int i = 0; i < readThreads.length; i++) {
      ReadThread readThread = readThreads[i];
      System.out.println("read : " + readThread.count);
      totalReadCount += readThread.count;
    }
    System.out.println("total read r/s : " + ((totalReadCount * 1000) / (end - begin)));
  }


  class ReadThread extends Thread implements Query {
    private int count;
    private int start;

    public ReadThread(int start) {
      this.start = start;
    }

    public void run() {
      synchronized(RWLockTestRun.this) {
        while(!RWLockTestRun.this.started){
          try {
            RWLockTestRun.this.wait();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      }
      boolean shouldContinue = RWLockTestRun.this.shouldContinue;
      while (shouldContinue) {
        try {
          prevayler.execute(this);
          count++;
        } catch (Exception e) {
          e.printStackTrace();
        }
        synchronized (RWLockTestRun.this) {
          shouldContinue = RWLockTestRun.this.shouldContinue;
        }
      }
    }

    public Object query(Object prevalentSystem, Date executionTime) throws Exception {
      ((QuerySystem) prevalentSystem).queryByName("NAME" + ((count + start) % 10000));
      ((QuerySystem) prevalentSystem).queryByName("NAME" + ((count + start + 1) % 10000));
      ((QuerySystem) prevalentSystem).queryByName("NAME" + ((count + start + 2) % 10000));
      return null;
    }
  }

  class WriteThread extends Thread {
    int count;
    private int start;

    public WriteThread(int start) {
      this.start = start;
    }

    public void run() {
      synchronized(RWLockTestRun.this) {
        while(!RWLockTestRun.this.started){
          try {
            RWLockTestRun.this.wait();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      }
      try {
        boolean shouldContinue = RWLockTestRun.this.shouldContinue;
        while (shouldContinue) {
          prevayler.execute(new WriteTransaction(new Record((start + count) % 10000)));
          count++;
          synchronized (RWLockTestRun.this) {
            shouldContinue = RWLockTestRun.this.shouldContinue;
          }

        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  static class WriteTransaction implements Transaction {
    private Record record;

    public WriteTransaction(Record record) {
      this.record = record;
    }

    public void executeOn(Object prevalentSystem, Date executionTime) {
      ((QuerySystem) prevalentSystem).put(record);
    }
  }

  public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
    int writeThreadCount = Integer.parseInt(args[0]);
    int readThreadCount = Integer.parseInt(args[1]);
    String prevalenceBase = "tmp/RWLock";
    System.out.println("RWLockTestRun.main" + new File(prevalenceBase).getAbsolutePath());
    PrevaylerFactory prevaylerFactory =new PrevaylerFactory();
    prevaylerFactory.configurePrevalenceDirectory(prevalenceBase);
    prevaylerFactory.configurePrevalentSystem(new QuerySystem());
    prevaylerFactory.configureTransactionFiltering(false);
    new RWLockTestRun(readThreadCount, writeThreadCount, prevaylerFactory.create()).run();
  }
}
