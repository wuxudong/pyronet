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
import jawnae.pyronet.traffic.ByteSink;
import jawnae.pyronet.traffic.ByteSinkEndsWith;
import jawnae.pyronet.traffic.PyroByteSinkFeeder;

public class ByteSinkEchoClient extends PyroClientAdapter
{
   public static final String HOST = "127.0.0.1";
   public static final int    PORT = 8421;

   public static void main(String[] args) throws IOException
   {
      ByteSinkEchoClient handler = new ByteSinkEchoClient();
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

   static byte[] delimiter = "\r\n".getBytes();

   @Override
   public void connectedClient(final PyroClient client)
   {
      System.out.println("connected: " + client);

      String message = "hello there!";

      System.out.println("client: yelling \"" + message + "\" to the server");

      // send "hello there!\r\n"
      client.write(ByteBuffer.wrap(message.getBytes()));
      client.write(ByteBuffer.wrap(delimiter));

      ByteSink echoSink = new ByteSinkEndsWith(delimiter, 1024, false)
      {
         @Override
         public void onReady(ByteBuffer buffer)
         {
            // convert data to text
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String text = new String(data);

            System.out.println("server echoed: \"" + text + "\"");
         }
      };

      PyroByteSinkFeeder feeder = new PyroByteSinkFeeder(client);
      feeder.addByteSink(echoSink);
      client.addListener(feeder);

      client.addListener(new PyroClientAdapter()
      {
         @Override
         public void disconnectedClient(PyroClient client)
         {
            System.out.println("server closed connection.");
         }
      });
   }
}
