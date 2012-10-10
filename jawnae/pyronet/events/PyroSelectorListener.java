/*
 * Created on 20 mei 2009
 */

package jawnae.pyronet.events;

import java.io.IOException;

import jawnae.pyronet.PyroClient;
import jawnae.pyronet.PyroServer;

public interface PyroSelectorListener
{
   public void executingTask(Runnable task);

   public void taskCrashed(Runnable task, Throwable cause);

   //

   public void selectedKeys(int count);

   public void selectFailure(IOException cause);

   //

   public void serverSelected(PyroServer server);

   public void clientSelected(PyroClient client, int readyOps);

   //

   public void serverBindFailed(IOException cause);

   public void clientBindFailed(IOException cause);
}
