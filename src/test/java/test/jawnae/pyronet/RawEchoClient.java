/*
 * Created on 30 dec 2008
 */

package test.jawnae.pyronet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import jawnae.pyronet.PyroClient;
import jawnae.pyronet.PyroSelector;
import jawnae.pyronet.events.PyroClientAdapter;

public class RawEchoClient extends PyroClientAdapter
{
   public static final String HOST = "127.0.0.1";
   public static final int    PORT = 8421;

   public static void main(String[] args) throws IOException
   {
      RawEchoClient handler = new RawEchoClient();
      PyroSelector selector = new PyroSelector();

      InetSocketAddress bind = new InetSocketAddress(HOST, PORT);
      System.out.println("connecting...");
      PyroClient client = selector.connect(bind);
      client.addListener(handler);

      while (true)
      {
         // perform network I/O

         selector.select();
      }
   }

   @Override
   public void connectedClient(final PyroClient client)
   {
      System.out.println("connected: " + client);

      final String message = "hello there!";

      System.out.println("client: yelling \"" + message + "\" to the server");

      // send "hello there!"
      client.write(ByteBuffer.wrap(message.getBytes()));

      client.addListener(new PyroClientAdapter()
      {
         @Override
         public void receivedData(PyroClient client, ByteBuffer buffer)
         {
            // convert data to text
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String text = new String(data);

            System.out.println("server echoed: \"" + text + "\"");
         }

         @Override
         public void droppedClient(PyroClient client, IOException cause)
         {
            System.out.println("lost connection");
         }

         @Override
         public void disconnectedClient(PyroClient client)
         {
            System.out.println("disconnected");
         }
      });
   }
}