/*
 * Created on 20 mei 2009
 */

package jawnae.pyronet.events;

import java.io.IOException;

import jawnae.pyronet.PyroClient;
import jawnae.pyronet.PyroServer;

public class PyroSelectorAdapter implements PyroSelectorListener
{
   public void executingTask(Runnable task)
   {
      //
   }

   public void taskCrashed(Runnable task, Throwable cause)
   {
      System.out.println(this.getClass().getSimpleName() + " caught exception: " + cause);
   }

   //

   public void selectedKeys(int count)
   {
      //
   }

   public void selectFailure(IOException cause)
   {
      System.out.println(this.getClass().getSimpleName() + " caught exception: " + cause);
   }

   //

   public void serverSelected(PyroServer server)
   {
      //
   }

   public void clientSelected(PyroClient client, int readyOps)
   {
      //
   }

   //

   @Override
   public void serverBindFailed(IOException cause)
   {
      System.out.println(this.getClass().getSimpleName() + ".serverBindFailed() caught exception: " + cause);
   }

   @Override
   public void clientBindFailed(IOException cause)
   {
      System.out.println(this.getClass().getSimpleName() + ".serverBindFailed() caught exception: " + cause);
   }
}
