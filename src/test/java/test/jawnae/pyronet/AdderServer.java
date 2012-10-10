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
import jawnae.pyronet.events.PyroServerListener;
import jawnae.pyronet.traffic.ByteSinkLength;
import jawnae.pyronet.traffic.PyroByteSinkFeeder;

public class AdderServer
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

            if (Math.random() < 0.5)
               handleWithOneRead(client);
            else
               handleWithTwoReads(client);
         }
      });

      while (true)
      {
         selector.select();
      }
   }

   static void handleWithOneRead(final PyroClient client)
   {
      System.out.println("handleWithOneRead");

      final PyroByteSinkFeeder feeder = new PyroByteSinkFeeder(client);

      // we expect 2 integers from the client, so read 8 bytes
      ByteSinkLength data12 = new ByteSinkLength(4 + 4)
      {
         @Override
         public void onReady(ByteBuffer buffer)
         {
            // add the values
            int value1 = buffer.getInt();
            int value2 = buffer.getInt();
            int answer = value1 + value2;
            System.out.println("calculated: " + value1 + "+" + value2 + "=" + answer);

            ByteBuffer result = ByteBuffer.allocate(4);
            result.putInt(answer);
            result.flip();

            // send it to the client
            client.write(result);
         }
      };
      feeder.addByteSink(data12);

      client.addListener(feeder);
   }

   static void handleWithTwoReads(final PyroClient client)
   {
      System.out.println("handleWithTwoReads");

      final PyroByteSinkFeeder feeder = new PyroByteSinkFeeder(client);

      final int[] values = new int[2];

      // we expect 2 integers from the client
      ByteSinkLength data1 = new ByteSinkLength(4)
      {
         @Override
         public void onReady(ByteBuffer buffer)
         {
            values[0] = buffer.getInt();
         }
      };

      ByteSinkLength data2 = new ByteSinkLength(4)
      {
         @Override
         public void onReady(ByteBuffer buffer)
         {
            values[1] = buffer.getInt();

            System.out.println("calculated: " + values[0] + "+" + values[1] + "=" + (values[0] + values[1]));

            // add the values
            ByteBuffer result = ByteBuffer.allocate(4);
            result.putInt(values[0] + values[1]);
            result.flip();

            // send it to the client
            client.write(result);
         }
      };
      feeder.addByteSink(data1);
      feeder.addByteSink(data2);

      // 
      client.addListener(feeder);
   }
}