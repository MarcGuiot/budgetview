package org.designup.picsou.client.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.designup.picsou.client.ClientTransport;
import org.designup.picsou.client.exceptions.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class HttpsClientTransport implements ClientTransport {
  public static final String SESSION_ID = "sessionId";
  private String serverUrl;
  private HttpClient httpClient;

  public HttpsClientTransport(String serverUrl) {
    this.serverUrl = serverUrl;
    this.httpClient = new DefaultHttpClient();
  }

  public SerializedInput connect(long version) throws BadConnection {
    SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
    serializedByteArrayOutput.getOutput().writeBoolean(false);
    return sendRequest(null, "/connect", serializedByteArrayOutput.toByteArray());
  }

  public void localRegister(Long sessionId, byte[] privateId, byte[] mail, byte[] signature, String activationCode) {
  }

  public SerializedInput createUser(Long sessionId, byte[] bytes) {
    return sendRequest(sessionId, "/createUser", bytes);
  }

  public SerializedInput deleteUser(Long sessionId, byte[] bytes) {
    return SerializedInputOutputFactory.init(new byte[0]);
  }

  public SerializedInput rename(Long sessionId, byte[] data) {
    return sendRequest(sessionId, "/renameUser", data);
  }

  public void localDownload(Long sessionId, byte[] privateId, long version) {
  }

  public SerializedInput getSnapshotInfos(Long sessionId, byte[] bytes) {
    return null;
  }

  public SerializedInput getSnapshotData(Long sessionId, byte[] data) {
    return null;
  }

  public void setLang(Long sessionId, byte[] privateId, String lang) {
  }

  public SerializedInput identifyUser(Long sessionId, byte[] data) {
    return sendRequest(sessionId, "/identifyUser", data);
  }

  public SerializedInput updateUserData(Long sessionId, byte[] bytes) {
    return sendRequest(sessionId, "/addUserData", bytes);
  }

  public SerializedInput getUserData(Long sessionId, byte[] bytes) {
    return sendRequest(sessionId, "/getUserData", bytes);
  }

  public SerializedInput hasChanged(Long sessionId, byte[] bytes) {
    return null;
  }

  public SerializedInput restore(Long sessionId, byte[] bytes) {
    return sendRequest(sessionId, "/replaceUserData", bytes);
  }

  public void disconnect(Long sessionId, byte[] bytes) {
    sendRequest(sessionId, "/disconnect", bytes).close();
  }

  public void takeSnapshot(Long sessionId, byte[] data) {
//    sendRequest(sessionId, "/takeSnapshot", data);
  }

  public SerializedInput getLocalUsers() {
    return null;
  }

  public void removeLocalUser(String user) {
  }

  public void confirmUser(Long sessionId, byte[] data) {
    sendRequest(sessionId, "/confirmUser", data).close();
  }

  private SerializedInput sendRequest(Long sessionId, String url, byte[] data) {
    boolean hasError = true;
    final HttpPost postMethod = new HttpPost(serverUrl + url);
    try {
      if (sessionId != null) {
        postMethod.setHeader(SESSION_ID, sessionId.toString());
      }
      postMethod.setEntity(new ByteArrayEntity(data));
      HttpResponse method = sendRequest(postMethod);
      final InputStream inputStream = method.getEntity().getContent();
      return SerializedInputOutputFactory.init(new InputStream() {
        public int read() throws IOException {
          return inputStream.read();
        }

        public void close() throws IOException {
          super.close();
          postMethod.releaseConnection();
        }
      });
    }
    catch (RuntimeException e){
      postMethod.releaseConnection();
      throw e;
    }
    catch (IOException e) {
      postMethod.releaseConnection();
      Log.write("ex : ", e);
      throw new BadConnection(e);
    }
  }

  private HttpResponse sendRequest(HttpPost postMethod) throws IOException, InvalidActionForState, UnknownId {
    HttpResponse httpResponse = httpClient.execute(postMethod);
    int code = httpResponse.getStatusLine().getStatusCode();
    if (code == 400) {
      InputStream inputStream = httpResponse.getEntity().getContent();
      final byte result[] = new byte[256];
      int length = inputStream.read(result);
      postMethod.releaseConnection();
      //On passe par un buffer intermediaire pour eviter une attaque type buffer overflow
      ObjectInputStream objectInputStream =
        new ObjectInputStream(new ByteArrayInputStream(result, 0, length));
      int exceptionCode = objectInputStream.readInt();
      String message = null;
      try {
        message = (String)objectInputStream.readObject();
      }
      catch (ClassNotFoundException e) {
      }
      final String exceptionMessage = message == null ? null : message;
      ExceptionMapping.visit(exceptionCode, new ExceptionMapping.Visitor() {
        public void visitUserAlreadyExist() {
          throw new UserAlreadyExists(exceptionMessage);
        }

        public void visitInvalidActionForState() {
          throw new InvalidActionForState(exceptionMessage);
        }

        public void visitIdentificationFailed() {
          throw new IdentificationFailed(exceptionMessage);
        }

        public void visitBadPassword() {
          throw new BadPassword(exceptionMessage);
        }

        public void visitUnknownId() {
          throw new UnknownId();
        }

        public void visitUserNotRegistered() {
          throw new UserNotRegistered(exceptionMessage);
        }

        public void visitUndefinedId() {
          throw new InvalidState(new String(result));
        }
      });
    }
    else if (code != 200) {
      InputStream inputStream = httpResponse.getEntity().getContent();
      byte result[] = new byte[256];
      inputStream.read(result);
      postMethod.releaseConnection();
      throw new InvalidState(new String(result));
    }
    return httpResponse;
  }

}
