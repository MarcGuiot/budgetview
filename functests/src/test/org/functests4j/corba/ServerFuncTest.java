package org.functests4j.corba;

import functests4j.ClientPOATie;
import functests4j.ServerHelper;
import org.functests4j.*;
import org.functests4j.kernel.FuncTestCmd;
import org.functests4j.kernel.FuncTestCase;
import org.functests4j.kernel.AsynCmd;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

import junit.framework.TestCase;

public class ServerFuncTest extends TestCase {
  private ORB orb;
  private boolean result;
  private FuncTestCase funcTestCase;

  public void setUp() throws Exception {
    System.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
    System.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");


    funcTestCase = new FuncTestCase();
    orb = ORB.init(new String[0], null);
    POA poa = org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    poa.the_POAManager().activate();
    ClientPOATie p_servant = new ClientPOATie(new Client_i(funcTestCase));
    org.omg.CORBA.Object object = poa.servant_to_reference(p_servant);
    String s = orb.object_to_string(object);
    FileWriter fileWriter = new FileWriter("ior_client");
    fileWriter.write(s);
    fileWriter.close();
  }

  public void testCallToServeurCallClient() throws Exception {
    String ior = new BufferedReader(new FileReader("ior_server")).readLine();
    org.omg.CORBA.Object object = orb.string_to_object(ior);
    final functests4j.Server server = ServerHelper.narrow(object);
    AsynCmd asynCmd = funcTestCase.asyncCall(new FuncTestCmd() {
      public void call() throws Exception {
        result = server.callMe("some information");
      }

      public String getDescription() {
        return "any";
      }
    });

    DefaultFuncTestEvent expected = new DefaultFuncTestEvent("Client.callToClient");
    expected.setAttributes("arg1", "some information");
    expected.setReturnValue("arg2", "some information");
    expected.setReturnValue("return", Boolean.FALSE);
    funcTestCase.expect(expected);
    funcTestCase.waitEnd(asynCmd, 1);
    assertTrue(asynCmd.isDone());
    assertTrue(result);
  }

  public void testReturnReturnValueIfReturnValueIsDifferent() throws Exception{
    String ior = new BufferedReader(new FileReader("ior_server")).readLine();
    org.omg.CORBA.Object object = orb.string_to_object(ior);
    final functests4j.Server server = ServerHelper.narrow(object);
    AsynCmd asynCmd = funcTestCase.asyncCall(new MyFuncTestCmd(server));

    DefaultFuncTestEvent expected = new DefaultFuncTestEvent("Client.callToClient");
    expected.setAttributes("arg1", "some information");
    expected.setReturnValue("return", Boolean.FALSE);
    expected.setReturnValue("arg2", "other data");
    funcTestCase.expect(expected);
    funcTestCase.waitEnd(asynCmd, 1);
    assertTrue(asynCmd.isDone());
    assertFalse(result);

    asynCmd = funcTestCase.asyncCall(new MyFuncTestCmd(server));
    expected = new DefaultFuncTestEvent("Client.callToClient");
    expected.setAttributes("arg1", "some information");
    expected.setReturnValue("return", Boolean.TRUE);
    expected.setReturnValue("arg2", "other data");
    funcTestCase.expect(expected);
    funcTestCase.waitEnd(asynCmd, 1);
    assertTrue(asynCmd.isDone());
    assertTrue(result);
  }

  private class MyFuncTestCmd implements FuncTestCmd {
    private final functests4j.Server server;

    public MyFuncTestCmd(functests4j.Server server) {
      this.server = server;
    }

    public void call() throws Exception {
      result = server.callMe("some information");
    }

    public String getDescription() {
      return "any";
    }
  }
}
