/*
 * Created on 24 nov 2009
 */

package jawnae.pyronet.traffic;

import java.nio.ByteBuffer;

public abstract class ByteSinkLength implements ByteSink
{
   private final ByteBuffer result;
   private int              filled;

   public ByteSinkLength(int size)
   {
      if (size == 0)
         throw new IllegalArgumentException();
      this.result = ByteBuffer.allocate(size);

      this.reset();
   }

   @Override
   public void reset()
   {
      this.result.clear();
      this.filled = 0;
   }

   @Override
   public int feed(byte b)
   {
      this.result.put(this.filled, b);

      this.filled += 1;

      if (this.filled == this.result.capacity())
      {
         this.onReady(this.result);
         return FEED_ACCEPTED_LAST;
      }

      return ByteSink.FEED_ACCEPTED;
   }
}