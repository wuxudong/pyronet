/*
 * Created on 24 nov 2009
 */

package jawnae.pyronet.traffic;

import java.nio.ByteBuffer;

public interface ByteSink
{
   public static int FEED_ACCEPTED      = 1;
   public static int FEED_ACCEPTED_LAST = 2;
   public static int FEED_REJECTED      = 3;

   /**
    * determines what to do with the specified byte: accept, accept as final byte, reject
    */

   public int feed(byte b);

   /**
    * Resets the state of this ByteSink, allowing it to be enqueued again
    */

   public void reset();

   /**
    * Called by the client when this ByteSink is complete
    */

   public void onReady(ByteBuffer buffer);
}
