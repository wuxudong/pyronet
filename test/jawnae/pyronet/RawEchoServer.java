/*
 * Created on 30 dec 2008
 */

package test.jawnae.pyronet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import jawnae.pyronet.PyroClient;
import jawnae.pyronet.PyroSelector;
import jawnae.pyronet.PyroServer;
import jawnae.pyronet.events.PyroClientAdapter;
import jawnae.pyronet.events.PyroServerListener;

public class RawEchoServer
{
   public static final String HOST = "127.0.0.1";
   public static final int    PORT = 8421;

   public static void main(String[] args) throws IOException
   {
      PyroSelector selector = new PyroSelector();
      PyroServer server = selector.listen(new InetSocketAddress(HOST, PORT));
      System.out.println("listening: " + server);

      server.addListener(new PyroServerListener()
      {
         @Override
         public void acceptedClient(PyroClient client)
         {
            System.out.println("accepted-client: " + client);

            echoBytesForTwoSeconds(client);
         }
      });

      while (true)
      {
         selector.select(100);
      }
   }

   static void echoBytesForTwoSeconds(PyroClient client)
   {
      try
      {
         client.setTimeout(2 * 1000);
      }
      catch (IOException exc)
      {
         exc.printStackTrace();
         return;
      }

      client.addListener(new PyroClientAdapter()
      {
         @Override
         public void receivedData(PyroClient client, ByteBuffer buffer)
         {
            ByteBuffer echo = buffer.slice();

            // convert data to text
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String text = new String(data);

            // dump to console
            System.out.println("received \"" + text + "\" from " + client);

            client.write(echo);
         }

         @Override
         public void disconnectedClient(PyroClient client)
         {
            System.out.println("disconnected");
         }
      });
   }
}