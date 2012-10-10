/*
 * Created on 19 jun 2009
 */

package test.jawnae.pyronet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import craterstudio.data.Duo;
import craterstudio.text.Text;

import jawnae.pyronet.PyroClient;
import jawnae.pyronet.PyroSelector;
import jawnae.pyronet.PyroServer;
import jawnae.pyronet.addon.PyroSingletonSelectorProvider;
import jawnae.pyronet.events.PyroClientAdapter;
import jawnae.pyronet.events.PyroServerListener;

public class GatewayTwoSelectors extends PyroClientAdapter implements PyroServerListener
{
   public static void main(String[] args) throws IOException
   {
      if (args.length == 0)
      {
         System.out.println("Usage:");
         System.out.println("   {sourceHost}@{sourcePort} {destinationHost}@{destinationPort}");

         System.exit(0);
      }

      PyroSelector listenHub = new PyroSelector();
      PyroSelector trafficHub = new PyroSelector();

      GatewayTwoSelectors gateway = new GatewayTwoSelectors(listenHub, trafficHub);

      for (int i = 0; i < args.length; i += 2)
      {
         String srcArg = args[i + 0];
         String dstArg = args[i + 1];

         String srcHost = Text.before(srcArg, '@');
         String dstHost = Text.before(dstArg, '@');
         int srcPort = Integer.parseInt(Text.after(srcArg, '@'));
         int dstPort = Integer.parseInt(Text.after(dstArg, '@'));
         InetSocketAddress src = new InetSocketAddress(srcHost, srcPort);
         InetSocketAddress dst = new InetSocketAddress(dstHost, dstPort);

         gateway.addBridge(src, dst);
      }

      listenHub.spawnNetworkThread("listen");
      trafficHub.spawnNetworkThread("traffic");
   }

   //

   private final PyroSelector                 listenSelector, trafficSelector;
   private final List<Duo<InetSocketAddress>> srcToDst;
   private final List<PyroServer>             servers;

   public GatewayTwoSelectors(PyroSelector listenSelector, PyroSelector trafficSelector)
   {
      this.listenSelector = listenSelector;
      this.trafficSelector = trafficSelector;
      this.srcToDst = new ArrayList<Duo<InetSocketAddress>>();
      this.servers = new ArrayList<PyroServer>();
   }

   //

   public void addBridge(InetSocketAddress src, InetSocketAddress dst) throws IOException
   {
      PyroServer server = this.listenSelector.listen(src);
      server.addListener(this);

      server.installSelectorProvider(new PyroSingletonSelectorProvider(trafficSelector));

      this.servers.add(server);
      this.srcToDst.add(new Duo<InetSocketAddress>(src, dst));
   }

   //

   @Override
   public void acceptedClient(PyroClient client)
   {
      System.out.println("acceptedClient[client-thread=" + Thread.currentThread().getName() + ", server-thread=" + client.getServer().selector().networkThread().getName() + "]:" + client);

      InetSocketAddress src = client.getLocalAddress();
      InetSocketAddress dst = null;

      for (Duo<InetSocketAddress> duo : this.srcToDst)
      {
         if (duo.first().equals(src))
         {
            dst = duo.second();
            break;
         }
      }

      System.out.println("Redirecting: " + src.getAddress().getHostAddress() + " => " + ((dst == null) ? "*" : dst.getAddress().getHostAddress()));

      if (dst == null)
      {
         client.dropConnection();
         return;
      }

      try
      {
         // make a connection to the other guy
         PyroClient otherGuy = client.selector().connect(dst);

         client.addListener(this);
         otherGuy.addListener(this);

         client.attach(otherGuy);
         otherGuy.attach(client);
      }
      catch (IOException exc)
      {
         exc.printStackTrace();
      }
   }

   @Override
   public void receivedData(PyroClient client, ByteBuffer data)
   {
      client.selector().checkThread();

      // send what you received to the other guy
      PyroClient otherGuy = (PyroClient) client.attachment();

      System.out.println("traffic:" + client + " => " + otherGuy + " (" + data.remaining() + " bytes)");

      try
      {
         otherGuy.writeCopy(data);
      }
      catch (IllegalStateException exc)
      {
         // boo
      }
   }

   @Override
   public void droppedClient(PyroClient client, IOException cause)
   {
      this.disconnectTheOtherGuy(client);
   }

   @Override
   public void disconnectedClient(PyroClient client)
   {
      this.disconnectTheOtherGuy(client);
   }

   private final void disconnectTheOtherGuy(PyroClient client)
   {
      PyroClient otherGuy = (PyroClient) client.attachment();

      // also disconnect the other guy
      if (otherGuy != null && !otherGuy.isDisconnected())
      {
         client.attach(null);
         otherGuy.shutdown();
      }

      // note that you don't know whether you are the
      // other guy, or the other guy is! if either end
      // disconnects, the _other_ guy disconnects too.
   }
}
