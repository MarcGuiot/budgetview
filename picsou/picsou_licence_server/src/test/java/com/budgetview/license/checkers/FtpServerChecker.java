package com.budgetview.license.checkers;

import junit.framework.Assert;
import org.mockftpserver.core.command.Command;
import org.mockftpserver.core.command.InvocationRecord;
import org.mockftpserver.core.session.Session;
import org.mockftpserver.stub.StubFtpServer;
import org.mockftpserver.stub.command.AbstractStubDataCommandHandler;

public class FtpServerChecker {
  private StubFtpServer ftpServer;
  boolean started = false;

  public FtpServerChecker(int serverControlPort) {
    ftpServer = new StubFtpServer();
    ftpServer.setServerControlPort(serverControlPort);
  }

  public void start() {
    ftpServer.start();
    started = true;
  }

  public void dispose() {
    if (ftpServer != null) {
      if (started) {
        ftpServer.stop();
        started = false;
      }
      ftpServer = null;
    }
  }

  public Retr setFtpReply(final String firstExpectedFile, final String firstContent,
                          final String secondExpectedFile, final byte[] secondContent) {
    final CompositeRetrHandler commandHandler =
      new CompositeRetrHandler(firstExpectedFile, firstContent, secondExpectedFile, secondContent);
    ftpServer.setCommandHandler("RETR", commandHandler);
    return new Retr() {
      public void assertOk() {
        commandHandler.assertOk();
      }
    };
  }

  private static class CompositeRetrHandler extends AbstractStubDataCommandHandler {
    int result;
    private final String firstExpectedFile;
    private final String firstContent;
    private final String secondExpectedFile;
    private final byte[] secondContent;
    private int exptectedResult;

    public CompositeRetrHandler(String firstExpectedFile, String firstContent,
                                String secondExpectedFile, byte[] secondContent) {
      this.firstExpectedFile = firstExpectedFile;
      this.firstContent = firstContent;
      this.secondExpectedFile = secondExpectedFile;
      this.secondContent = secondContent;
      result = 0;
      exptectedResult = 0;
      if (firstExpectedFile != null) {
        exptectedResult++;
      }
      if (secondExpectedFile != null) {
        exptectedResult++;
      }
    }

    protected void processData(Command command, Session session,
                               InvocationRecord invocationRecord) throws Exception {
      String filename = command.getRequiredString(0);
      if (filename.equals(firstExpectedFile)) {
        byte[] data = firstContent.getBytes();
        session.sendData(data, data.length);
        result++;
      }
      else if (filename.equals(secondExpectedFile)) {
        session.sendData(secondContent, secondContent.length);
        result++;
      }
      else {
        result = -1;
        session.close();
      }
    }

    public void assertOk() {
      long end = System.currentTimeMillis() + 30000;
      while (result != exptectedResult && System.currentTimeMillis() < end) {
        try {
          Thread.sleep(100);
        }
        catch (InterruptedException e) {
        }
      }
      Assert.assertEquals(exptectedResult, result);
    }
  }

  public interface Retr {
    void assertOk();
  }
}
