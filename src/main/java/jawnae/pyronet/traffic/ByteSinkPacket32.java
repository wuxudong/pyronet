/*
 * Created on 24 nov 2009
 */

package jawnae.pyronet.traffic;

import java.nio.ByteBuffer;

import jawnae.pyronet.PyroClient;

public abstract class ByteSinkPacket32 implements ByteSink
{
   public static void sendTo(PyroClient client, byte[] payload)
   {
      boolean isExtreme = (payload.length > 0xFFFFFFFF - 4);

      byte[] wrapped = new byte[4 + (isExtreme ? 0 : payload.length)];
      wrapped[0] = (byte) (payload.length >> 24);
      wrapped[1] = (byte) (payload.length >> 16);
      wrapped[2] = (byte) (payload.length >> 8);
      wrapped[3] = (byte) (payload.length >> 0);

      if (!isExtreme)
      {
         System.arraycopy(payload, 0, wrapped, 4, payload.length);
      }

      client.write(client.selector().malloc(wrapped));

      if (isExtreme)
      {
         client.write(client.selector().malloc(payload));
      }
   }

   //

   ByteSinkLength current;

   public ByteSinkPacket32()
   {
      this.reset();
   }

   @Override
   public void reset()
   {
      this.current = new ByteSinkLength(4)
      {
         @Override
         public void onReady(ByteBuffer buffer)
         {
            // header is received
            int len = buffer.getInt(0);

            current = new ByteSinkLength(len)
            {
               @Override
               public void onReady(ByteBuffer buffer)
               {
                  // content is received
                  ByteSinkPacket32.this.onReady(buffer);
                  current = null;
               }
            };
         }
      };
   }

   @Override
   public int feed(byte b)
   {
      if (this.current == null)
      {
         throw new IllegalStateException();
      }

      int result = this.current.feed(b);

      if (result == FEED_ACCEPTED)
      {
         return result;
      }

      // 'current' will be replaced by now

      if (this.current == null)
      {
         return result;
      }

      return this.current.feed(b);
   }
}