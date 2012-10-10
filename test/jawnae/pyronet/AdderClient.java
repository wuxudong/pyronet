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
import jawnae.pyronet.traffic.ByteSinkLength;
import jawnae.pyronet.traffic.PyroByteSinkFeeder;

public class AdderClient extends PyroClientAdapter
{
   public static final String HOST = "127.0.0.1";
   public static final int    PORT = 8421;

   public static void main(String[] args) throws IOException
   {
      AdderClient adder = new AdderClient();
      PyroSelector selector = new PyroSelector();

      InetSocketAddress bind = new InetSocketAddress(HOST, PORT);
      System.out.println("connecting...");
      PyroClient client = selector.connect(bind);
      client.addListener(adder);

      while (true)
      {
         // perform network I/O

         selector.select();
      }
   }

   static int bytesInInteger = 4;

   @Override
   public void connectedClient(final PyroClient client)
   {
      System.out.println("connected: " + client);

      // first create 
      final PyroByteSinkFeeder feeder = new PyroByteSinkFeeder(client);

      // generate two random numbers to add
      final int value1 = (int) (Math.random() * 100);
      final int value2 = (int) (Math.random() * 100);

      System.out.println("client: going to let the server calculate: " + value1 + "+" + value2);

      ByteBuffer payload1 = (ByteBuffer) ByteBuffer.allocate(4).putInt(value1).flip();
      ByteBuffer payload2 = (ByteBuffer) ByteBuffer.allocate(4).putInt(value2).flip();

      client.write(payload1);
      client.write(payload2);

      // lets create a handler, it will be executed once the server sends the answer
      ByteSink answerSink = new ByteSinkLength(bytesInInteger)
      {
         @Override
         public void onReady(ByteBuffer buffer)
         {
            int value = buffer.getInt();

            System.out.println("server said: " + value);

            // this is all we needed
            client.shutdown();
         }
      };

      // add the packet handler to the 
      feeder.addByteSink(answerSink);
      client.addListener(feeder);
   }
}
