package org.designup.picsou.client.http;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpClientError;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.designup.picsou.client.ClientTransport;
import org.designup.picsou.client.exceptions.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpsClientTransport implements ClientTransport {
  public static final String SESSION_ID = "sessionId";
  private String serverUrl;
  private HttpClient httpClient;

  public HttpsClientTransport(String serverUrl) {
    this.serverUrl = serverUrl;
    Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 8443);
    Protocol.registerProtocol("https", easyhttps);
    this.httpClient = new HttpClient();
  }

  public SerializedInput createUser(byte[] bytes) {
    return sendRequest(null, "/createUser", bytes);
  }

  public SerializedInput identifyUser(byte[] data) {
    return sendRequest(null, "/identifyUser", data);
  }

  public SerializedInput updateUserData(Long sessionId, byte[] bytes) {
    return sendRequest(sessionId, "/addUserData", bytes);
  }

  public SerializedInput getUserData(Long sessionId, byte[] bytes) {
    return sendRequest(sessionId, "/getUserData", bytes);
  }

  public SerializedInput getNextId(Long sessionId, byte[] bytes) {
    return sendRequest(sessionId, "/getNextId", bytes);
  }

  public void disconnect(Long sessionId, byte[] bytes) {
    sendRequest(sessionId, "/disconnect", bytes);
  }

  public void takeSnapshot(Long sessionId, byte[] data) {
    sendRequest(sessionId, "/takeSnapshot", data);
  }

  public void confirmUser(Long sessionId, byte[] data) {
    sendRequest(sessionId, "/confirmUser", data);
  }

  private SerializedInput sendRequest(Long sessionId, String url, byte[] data) {
    boolean hasError = true;
    try {
      Log.enter("send request " + url);
      PostMethod postMethod = new PostMethod(serverUrl + url);
      if (sessionId != null) {
        postMethod.setRequestHeader(SESSION_ID, sessionId.toString());
      }
      postMethod.setRequestEntity(new ByteArrayRequestEntity(data));
      SerializedInput serializedInput = SerializedInputOutputFactory.init(sendRequest(postMethod).getResponseBodyAsStream());
      Log.leave("send Ok");
      hasError = false;
      return serializedInput;
    }
    catch (IOException e) {
      Log.write("ex : ", e);
      throw new BadConnection(e);
    }
    finally {
      if (hasError) {
        Log.leave("send with Error");
      }
    }
  }

  private PostMethod sendRequest(PostMethod postMethod) throws IOException, InvalidActionForState, UnknownId {
    httpClient.executeMethod(postMethod);
    int code = postMethod.getStatusCode();
    if (code == 400) {
      InputStream inputStream = postMethod.getResponseBodyAsStream();
      final byte result[] = new byte[256];
      int length = inputStream.read(result);
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
      InputStream inputStream = postMethod.getResponseBodyAsStream();
      byte result[] = new byte[256];
      inputStream.read(result);
      throw new InvalidState(new String(result));
    }
    return postMethod;
  }

  static public class EasySSLProtocolSocketFactory implements ProtocolSocketFactory {

    private SSLContext sslcontext = null;

    private static SSLContext createEasySSLContext() {
      try {
        SSLContext context = SSLContext.getInstance("SSL");
        context.init(null, new TrustManager[]{new EasyX509TrustManager(null)}, null);
        return context;
      }
      catch (Exception e) {
        throw new HttpClientError(e.toString());
      }
    }

    private SSLContext getSSLContext() {
      if (this.sslcontext == null) {
        this.sslcontext = createEasySSLContext();
      }
      return this.sslcontext;
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(String,int,InetAddress,int)
     */
    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort)
      throws IOException, UnknownHostException {

      return getSSLContext().getSocketFactory().createSocket(host, port, clientHost, clientPort);
    }

    /**
     * Attempts to get a new socket connection to the given host within the given time limit.
     * <p/>
     * To circumvent the limitations of older JREs that do not support connect timeout a
     * controller thread is executed. The controller thread attempts to create a new socket
     * within the given limit of time. If socket constructor does not return until the
     * timeout expires, the controller terminates and throws an {@link ConnectTimeoutException}
     * </p>
     *
     * @param host   the host name/IP
     * @param port   the port on the host
     * @param params {@link HttpConnectionParams Http connection parameters}
     * @return Socket a new socket
     * @throws IOException          if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     *                              determined
     */
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort,
                               HttpConnectionParams params)
      throws IOException, UnknownHostException, ConnectTimeoutException {
      if (params == null) {
        throw new IllegalArgumentException("Parameters may not be null");
      }
      int timeout = params.getConnectionTimeout();
      if (timeout == 0) {
        timeout = 2 * 1000;
      }
      SocketFactory socketfactory = getSSLContext().getSocketFactory();
      Socket socket = socketfactory.createSocket();
      SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
      SocketAddress remoteaddr = new InetSocketAddress(host, port);
      socket.bind(localaddr);
      socket.connect(remoteaddr, timeout);
      return socket;
    }

    public Socket createSocket(String host, int port)
      throws IOException {
      return getSSLContext().getSocketFactory().createSocket(host, port);
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
      return getSSLContext().getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    public boolean equals(Object obj) {
      return ((obj != null) && obj.getClass().equals(EasySSLProtocolSocketFactory.class));
    }

    public int hashCode() {
      return EasySSLProtocolSocketFactory.class.hashCode();
    }

  }

  static public class EasyX509TrustManager implements X509TrustManager {
    private X509TrustManager standardTrustManager = null;

    public EasyX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
      TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      factory.init(keystore);
      TrustManager[] trustmanagers = factory.getTrustManagers();
      if (trustmanagers.length == 0) {
        throw new NoSuchAlgorithmException("no trust manager found");
      }
      this.standardTrustManager = (X509TrustManager)trustmanagers[0];
    }

    public void checkClientTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
      standardTrustManager.checkClientTrusted(certificates, authType);
    }

    public void checkServerTrusted(X509Certificate[] certificates, String authType) throws CertificateException {
//      if ((certificates != null)) {
//        System.out.println("server certificate chain:");
//        for (int i = 0; i < certificates.length; i++) {
//          System.out.println("X509Certificate[" + i + "]=" + certificates[i]);
//        }
//      }
//      if ((certificates != null) && (certificates.length == 1)) {
//        certificates[0].checkValidity();
//      }
//      else {
//        standardTrustManager.checkServerTrusted(certificates, authType);
//      }
    }

    public X509Certificate[] getAcceptedIssuers() {
      return this.standardTrustManager.getAcceptedIssuers();
    }
  }

}
