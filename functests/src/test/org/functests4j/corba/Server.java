package org.functests4j.corba;

import functests4j.Client;
import functests4j.ClientHelper;
import functests4j.ServerOperations;
import functests4j.ServerPOATie;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.StringHolder;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Server {

  public static void main(String[] args) throws InvalidName, AdapterInactive, WrongPolicy, ServantNotActive,
                                                IOException, InterruptedException {
    System.setProperty("org.omg.CORBA.ORBClass", "org.jacorb.orb.ORB");
    System.setProperty("org.omg.CORBA.ORBSingletonClass", "org.jacorb.orb.ORBSingleton");


    final ORB orb = ORB.init(new String[0], null);
    POA poa = org.omg.PortableServer.POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
    poa.the_POAManager().activate();
    ServerPOATie p_servant = new ServerPOATie(new ServerPOA(orb));
    org.omg.CORBA.Object object = poa.servant_to_reference(p_servant);
    String s = orb.object_to_string(object);
    FileWriter fileWriter = new FileWriter("ior_server");
    fileWriter.write(s);
    fileWriter.close();
    synchronized (orb) {
      orb.wait();
    }
  }

  static class ServerPOA implements ServerOperations {
    private ORB orb;

    public ServerPOA(ORB orb) {
      this.orb = orb;
    }

    public boolean callMe(String s) {
      try {
        String ior = new BufferedReader(new FileReader("ior_client")).readLine();
        org.omg.CORBA.Object object = orb.string_to_object(ior);
        Client client = ClientHelper.narrow(object);
        StringHolder stringHolder = new StringHolder();
        boolean b = client.callToClient(s, stringHolder);
        if (stringHolder.value.equals(s)) {
          return true;
        }
        return b;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}

