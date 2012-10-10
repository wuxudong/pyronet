/*
 * Created on 26 apr 2010
 */

package jawnae.pyronet.events;

import jawnae.pyronet.PyroClient;

public interface PyroServerListener
{
   /**
    * Note: invoked from the PyroSelector-thread that created this PyroClient
    */
   public void acceptedClient(PyroClient client);
}
