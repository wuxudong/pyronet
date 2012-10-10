/*
 * Created on 30 dec 2008
 */

package test.jawnae.pyronet;

import java.io.IOException;
import java.net.InetSocketAddress;

import jawnae.pyronet.PyroClient;
import jawnae.pyronet.PyroSelector;
import jawnae.pyronet.PyroServer;
import jawnae.pyronet.traffic.ByteSinkPacket16;

public class PacketServer
{
   public static final String HOST = "127.0.0.1";
   public static final int    PORT = 8421;

   public static void main(String[] args) throws IOException
   {
      final PyroSelector selector = new PyroSelector();

      final PyroServer server = selector.listen(new InetSocketAddress(HOST, PORT));

      final long selectTimeout = 1000;
      final long broadcastInterval = 5000;
      long lastSent = 0;

      while (true)
      {
         // perform network I/O
         selector.select(selectTimeout);

         final long now = System.currentTimeMillis();

         // wait for broadcast interval
         if (now - lastSent < broadcastInterval)
         {
            continue;
         }

         // create the packet data
         String text = "Server time: " + now;
         System.out.println(text);
         byte[] data = text.getBytes();

         // send packet to all clients
         for (PyroClient client : server)
         {
            ByteSinkPacket16.sendTo(client, data);
         }

         lastSent = now;
      }
   }
}
