/*
 * Dumbster - a dummy SMTP server
 * Copyright 2004 Jason Paul Kitchen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dumbster.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SimpleSmtpServer implements Runnable {

  public static final int DEFAULT_SMTP_PORT = 25;

  private static final int TIMEOUT = 500;

  private List receivedMail;
  private volatile boolean stopped = true;
  private ServerSocket serverSocket;
  private int port = DEFAULT_SMTP_PORT;

  public SimpleSmtpServer(int port) {
    receivedMail = new ArrayList();
    this.port = port;
  }

  public void run() {
    stopped = false;
    try {
      try {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(TIMEOUT);
      }
      finally {
        synchronized (this) {
          // Notify when server socket has been created
          notifyAll();
        }
      }

      while (!isStopped()) {
        // Start server socket and listen for client connections
        Socket socket = null;
        try {
          socket = serverSocket.accept();
        }
        catch (Exception e) {
          if (socket != null) {
            socket.close();
          }
          continue; // Non-blocking socket timeout occurred: try accept() again
        }

        // Get the input and output streams
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream());

        synchronized (this) {
          /*
           * We synchronize over the handle method and the list update because the client call completes inside
           * the handle method and we have to prevent the client from reading the list until we've updated it.
           * For higher concurrency, we could just change handle to return void and update the list inside the method
           * to limit the duration that we hold the lock.
           */
          List msgs = handleSMTPTransaction(out, input);
          receivedMail.addAll(msgs);
        }
        socket.close();
      }
    }
    catch (Exception e) {
      /** @todo Should throw an appropriate exception here. */
      e.printStackTrace();
    }
    finally {
      if (serverSocket != null) {
        try {
          serverSocket.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public synchronized boolean isStopped() {
    return stopped;
  }

  public synchronized void stop() {
    stopped = true;
    try {
      serverSocket.close();
    }
    catch (IOException e) {
      // Ignore
    }
  }

  private List handleSMTPTransaction(PrintWriter out, BufferedReader input) throws IOException {

    // Initialize the state machine
    SmtpState smtpState = SmtpState.CONNECT;
    SmtpRequest smtpRequest = new SmtpRequest(SmtpActionType.CONNECT, "", smtpState);

    // Execute the connection request
    SmtpResponse smtpResponse = smtpRequest.execute();

    // Send initial response
    sendResponseToClient(out, smtpResponse);
    smtpState = smtpResponse.getNextState();

    List msgList = new ArrayList();
    SmtpMessage msg = new SmtpMessage();

    while (smtpState != SmtpState.CONNECT) {
      String line = input.readLine();
      if (line == null) {
        break;
      }

      // Create request from client input and current state
      SmtpRequest request = SmtpRequest.createRequest(line, smtpState);
      // Execute request and create response object
      SmtpResponse response = request.execute();
      // Move to next internal state
      smtpState = response.getNextState();
      // Send reponse to client
      sendResponseToClient(out, response);

      // Store input in message
      String params = request.getParams();
      msg.store(response, params);

      // If message reception is complete save it
      if (smtpState == SmtpState.QUIT) {
        msgList.add(msg);
        msg = new SmtpMessage();
      }
    }

    return msgList;
  }

  private static void sendResponseToClient(PrintWriter out, SmtpResponse smtpResponse) {
    if (smtpResponse.getCode() > 0) {
      int code = smtpResponse.getCode();
      String message = smtpResponse.getMessage();
      out.print(code + " " + message + "\r\n");
      out.flush();
    }
  }

  public synchronized Iterator getReceivedEmail() {
    return receivedMail.iterator();
  }

  public static SimpleSmtpServer start() {
    return start(DEFAULT_SMTP_PORT);
  }

  public static SimpleSmtpServer start(int port) {
    SimpleSmtpServer server = new SimpleSmtpServer(port);
    Thread t = new Thread(server);
    t.start();

    // Block until the server socket is created
    synchronized (server) {
      try {
        server.wait();
      }
      catch (InterruptedException e) {
        // Ignore don't care.
      }
    }
    return server;
  }

}
