/*
 * Created on 24 nov 2009
 */

package jawnae.pyronet.traffic;

import java.nio.ByteBuffer;

public abstract class ByteSinkEndsWith implements ByteSink
{
   private final ByteBuffer result;
   private final byte[]     endsWith;
   private final boolean    includeEndsWith;
   private int              matchCount;
   private int              filled;

   public ByteSinkEndsWith(byte[] endsWith, int capacity, boolean includeEndsWith)
   {
      if (endsWith == null || endsWith.length == 0)
         throw new IllegalStateException();
      this.result = ByteBuffer.allocate(capacity);
      this.endsWith = endsWith;
      this.includeEndsWith = includeEndsWith;

      this.reset();
   }

   @Override
   public void reset()
   {
      this.result.clear();
      this.matchCount = 0;
      this.filled = 0;
   }

   @Override
   public int feed(byte b)
   {
      if (this.endsWith[this.matchCount] == b)
      {
         this.matchCount++;
      }
      else
      {
         this.matchCount = 0;
      }

      this.result.put(this.filled, b);

      this.filled += 1;

      if (this.matchCount == this.endsWith.length)
      {
         int len = this.filled - (this.includeEndsWith ? 0 : this.endsWith.length);
         this.result.limit(len);
         this.onReady(this.result);
         return FEED_ACCEPTED_LAST;
      }

      return ByteSink.FEED_ACCEPTED;
   }
}