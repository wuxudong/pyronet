/*
 * Created on 26 apr 2010
 */

package jawnae.pyronet.addon;

import java.nio.channels.SocketChannel;

import jawnae.pyronet.PyroSelector;

public interface PyroSelectorProvider
{
   public PyroSelector provideFor(SocketChannel channel);
}