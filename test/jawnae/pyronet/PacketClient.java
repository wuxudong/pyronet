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
import jawnae.pyronet.traffic.ByteSinkPacket16;
import jawnae.pyronet.traffic.PyroByteSinkFeeder;

public class PacketClient extends PyroClientAdapter
{
   public static final String HOST = "127.0.0.1";
   public static final int    PORT = 8421;

   public static void main(String[] args) throws IOException
   {
      PacketClient main = new PacketClient();
      PyroSelector selector = new PyroSelector();

      InetSocketAddress bind = new InetSocketAddress(HOST, PORT);
      PyroClient client = selector.connect(bind);
      client.addListener(main);

      while (true)
      {
         // perform network I/O

         selector.select();
      }
   }

   @Override
   public void connectedClient(PyroClient client)
   {
      // for this example the following is overkill...

      // first create 
      final PyroByteSinkFeeder feeder = new PyroByteSinkFeeder(client);

      // lets create a packet handler
      ByteSink handler = new ByteSinkPacket16()
      {
         @Override
         public void onReady(ByteBuffer buffer)
         {
            // we have received a full packet
            byte[] payload = new byte[buffer.remaining()];
            buffer.get(payload);

            // print it to the console
            System.out.println("Received packet: " + new String(payload));

            // we're done, but are interested in the next packet...

            // reset this packet handler
            this.reset();
            
            // refill this packet
            feeder.addByteSink(this);
         }
      };

      // add the packet handler to the 
      feeder.addByteSink(handler);
   }
}
