/*
 * Created on 26 apr 2010
 */

package jawnae.pyronet.events;

import java.io.IOException;
import java.nio.ByteBuffer;

import jawnae.pyronet.PyroClient;

public interface PyroClientListener
{
   public void connectedClient(PyroClient client);

   public void unconnectableClient(PyroClient client);

   public void droppedClient(PyroClient client, IOException cause);

   public void disconnectedClient(PyroClient client);

   //

   public void receivedData(PyroClient client, ByteBuffer data);

   public void sentData(PyroClient client, int bytes);
}