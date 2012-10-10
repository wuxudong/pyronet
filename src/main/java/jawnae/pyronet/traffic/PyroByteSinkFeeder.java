/*
 * Created on 26 apr 2010
 */

package jawnae.pyronet.traffic;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import jawnae.pyronet.PyroClient;
import jawnae.pyronet.PyroSelector;
import jawnae.pyronet.events.PyroClientAdapter;

public class PyroByteSinkFeeder extends PyroClientAdapter
{
   private final PyroSelector         selector;
   private final ByteBuffer           buf;
   private final ByteStream           inbound;
   private final LinkedList<ByteSink> sinks;

   public PyroByteSinkFeeder(PyroClient client)
   {
      this(client.selector());
   }

   public PyroByteSinkFeeder(PyroSelector selector)
   {
      this(selector, 8 * 1024);
   }

   public PyroByteSinkFeeder(PyroSelector selector, int bufferSize)
   {
      this.selector = selector;
      this.buf = this.selector.malloc(bufferSize);
      this.inbound = new ByteStream();
      this.sinks = new LinkedList<ByteSink>();
   }

   //

   @Override
   public void receivedData(PyroClient client, ByteBuffer data)
   {
      this.feed(data);
   }

   //

   public ByteBuffer shutdown()
   {
      int bytes = this.inbound.getByteCount();
      ByteBuffer tmp = this.selector.malloc(bytes);
      this.inbound.get(tmp);
      this.inbound.discard(bytes);
      tmp.flip();
      return tmp;
   }

   public void addByteSink(ByteSink sink)
   {
      this.selector.checkThread();

      this.register(sink);
   }

   public void feed(ByteBuffer data)
   {
      ByteBuffer copy = this.selector.copy(data);

      this.inbound.append(copy);

      this.fill();
   }

   final void register(ByteSink sink)
   {
      this.sinks.addLast(sink);

      this.fill();
   }

   private final void fill()
   {
      if (this.sinks.isEmpty())
      {
         return;
      }

      this.buf.clear();
      this.inbound.get(this.buf);
      int off = 0;
      int end = this.buf.position();

      ByteSink currentSink = this.sinks.removeFirst();

      outer: while (off < end)
      {
         switch (currentSink.feed(this.buf.get(off)))
         {
            case ByteSink.FEED_ACCEPTED:
               off += 1;
               continue outer;

            case ByteSink.FEED_ACCEPTED_LAST:
               off += 1;
               break;

            case ByteSink.FEED_REJECTED:
               off += 0;
               break;
         }

         if (this.sinks.isEmpty())
         {
            currentSink = null;
            break;
         }

         currentSink = this.sinks.removeFirst();
      }

      if (currentSink != null)
      {
         this.sinks.addFirst(currentSink);
      }

      this.inbound.discard(off);
   }
}