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
import jawnae.pyronet.traffic.ByteSink;
import jawnae.pyronet.traffic.ByteSinkEndsWith;
import jawnae.pyronet.traffic.PyroByteSinkFeeder;

public class ByteSinkEchoServer
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

            readLineAndEcho(client);
         }
      });

      while (true)
      {
         selector.select();
      }
   }

   static final byte[]  newline                  = "\r\n".getBytes();
   static final int     maxLineLength            = 1024;
   static final boolean includeDelimiterInBuffer = false;

   static void readLineAndEcho(final PyroClient client)
   {
      // we need some way to process received bytes, in a
      // way makes it easier to handle them
      //
      // we are going to read a line (delimited by "\r\n")
      // so we use the ByteSinkEndsWith class

      ByteSink lineSink = new ByteSinkEndsWith(newline, maxLineLength, includeDelimiterInBuffer)
      {
         @Override
         public void onReady(ByteBuffer buffer)
         {
            ByteBuffer echo = buffer.slice();

            // convert data to text
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String text = new String(data);

            // dump to console
            System.out.println("received \"" + text + "\" from " + client);

            // echo data back to client            
            client.write(echo);
            client.write(ByteBuffer.wrap(newline)); // append newline

            // disconnect after data was sent
            client.shutdown();
         }
      };

      // connect the raw network events to a structure that
      // transfers received bytes into ByteSinks (byte handlers
      // that split the byte stream into chunks)
      PyroByteSinkFeeder feeder;
      feeder = new PyroByteSinkFeeder(client);

      // schedule the next ByteSink, which is going to be 'filled' first
      feeder.addByteSink(lineSink);

      // PyroByteSinkFeeder is a PyroClientListener, which
      // enables it to hook into the network-events
      client.addListener(feeder);
   }
}