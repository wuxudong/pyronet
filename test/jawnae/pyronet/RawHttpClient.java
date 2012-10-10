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
import jawnae.pyronet.events.PyroClientListener;

public class RawHttpClient
{
   public static final String HOST = "www.google.com";
   public static final int    PORT = 80;

   public static void main(String[] args) throws IOException
   {
      PyroSelector selector = new PyroSelector();

      InetSocketAddress bind = new InetSocketAddress(HOST, PORT);
      System.out.println("connecting...");
      PyroClient client = selector.connect(bind);

      PyroClientListener listener = new PyroClientAdapter()
      {
         @Override
         public void connectedClient(PyroClient client)
         {
            System.out.println("<connected>");

            // create HTTP request
            StringBuilder request = new StringBuilder();
            request.append("GET / HTTP/1.1\r\n");
            request.append("Host: " + HOST + "\r\n");
            request.append("Connection: close\r\n");
            request.append("\r\n");

            byte[] data = request.toString().getBytes();
            client.write(ByteBuffer.wrap(data));
         }

         @Override
         public void receivedData(PyroClient client, ByteBuffer data)
         {
            while (data.hasRemaining())
               System.out.print((char) data.get());
            System.out.flush();
         }

         @Override
         public void disconnectedClient(PyroClient client)
         {
            System.out.println("<disconnected>");
         }
      };

      client.addListener(listener);

      while (true)
      {
         selector.select();
      }
   }
}