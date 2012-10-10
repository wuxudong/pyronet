/*
 * Created on 26 apr 2010
 */

package jawnae.pyronet.events;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import jawnae.pyronet.PyroClient;
import jawnae.pyronet.PyroServer;

public class PyroLazyBastardAdapter implements PyroSelectorListener, PyroServerListener, PyroClientListener
{
   // --------------- PyroSelectorListener

   public void executingTask(Runnable task)
   {
      //
   }

   public void taskCrashed(Runnable task, Throwable cause)
   {
      System.out.println(this.getClass().getSimpleName() + ".taskCrashed() caught exception:");
      cause.printStackTrace();
   }

   public void selectedKeys(int count)
   {
      //
   }

   public void selectFailure(IOException cause)
   {
      System.out.println(this.getClass().getSimpleName() + ".selectFailure() caught exception:");
      cause.printStackTrace();
   }

   public void serverSelected(PyroServer server)
   {
      //
   }

   public void clientSelected(PyroClient client, int readyOps)
   {
      //
   }

   // ------------- PyroServerListener

   public void acceptedClient(PyroClient client)
   {
      //
   }

   // ------------- PyroClientListener

   public void connectedClient(PyroClient client)
   {
      //
   }

   public void unconnectableClient(PyroClient client)
   {
      System.out.println(this.getClass().getSimpleName() + ".unconnectableClient()");
   }

   public void droppedClient(PyroClient client, IOException cause)
   {
      if (cause != null && !(cause instanceof EOFException))
      {
         System.out.println(this.getClass().getSimpleName() + ".droppedClient() caught exception: " + cause);
      }
   }

   public void disconnectedClient(PyroClient client)
   {
      //
   }

   //

   public void receivedData(PyroClient client, ByteBuffer data)
   {
      //
   }

   public void sentData(PyroClient client, int bytes)
   {
      //
   }

   @Override
   public void serverBindFailed(IOException cause)
   {
      System.out.println(this.getClass().getSimpleName() + ".serverBindFailed() caught exception:");
      cause.printStackTrace();
   }

   @Override
   public void clientBindFailed(IOException cause)
   {
      System.out.println(this.getClass().getSimpleName() + ".serverBindFailed() caught exception:");
      cause.printStackTrace();
   }
}