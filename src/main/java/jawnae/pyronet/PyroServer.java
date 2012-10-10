/*
 * Created on Sep 24, 2008
 */

package jawnae.pyronet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jawnae.pyronet.addon.PyroSelectorProvider;
import jawnae.pyronet.events.PyroServerListener;

public class PyroServer implements Iterable<PyroClient> {
    private final PyroSelector selector;

    public final SelectionKey serverKey;

    final List<PyroClient> clients;

    PyroServer(PyroSelector selector, Selector nioSelector,
            InetSocketAddress endpoint, int backlog) throws IOException {
        this.selector = selector;
        this.selector.checkThread();

        ServerSocketChannel ssc;
        ssc = ServerSocketChannel.open();
        ssc.socket().bind(endpoint, backlog);
        ssc.configureBlocking(false);

        this.serverKey = ssc.register(nioSelector, SelectionKey.OP_ACCEPT);
        this.serverKey.attach(this);

        this.clients = new ArrayList<PyroClient>();
        this.listeners = new CopyOnWriteArrayList<PyroServerListener>();
    }

    //

    private final List<PyroServerListener> listeners;

    public void addListener(PyroServerListener listener) {
        this.selector.checkThread();

        this.listeners.add(listener);
    }

    public void removeListener(PyroServerListener listener) {
        this.selector.checkThread();

        this.listeners.remove(listener);
    }

    public void removeListeners() {
        this.selector.checkThread();

        this.listeners.clear();
    }

    /**
     * Returns the network that created this server
     */

    public PyroSelector selector() {
        return this.selector;
    }

    private PyroSelectorProvider selectorProvider;

    /**
     * By installing a PyroSelectorProvider you alter the PyroSelector used to
     * do the I/O of a PyroClient. This can be used for multi-threading your
     * network code
     */

    public void installSelectorProvider(PyroSelectorProvider selectorProvider) {
        this.selector().checkThread();

        this.selectorProvider = selectorProvider;
    }

    void onInterestOp() {
        if (!serverKey.isValid())
            throw new PyroException("invalid selection key");

        try {
            if (serverKey.isAcceptable()) {
                this.onReadyToAccept();
            }
        } catch (IOException exc) {
            throw new IllegalStateException(exc);
        }
    }

    /**
     * Returns an iterator to access all connected clients of this server
     */

    @Override
    public Iterator<PyroClient> iterator() {
        this.selector.checkThread();

        List<PyroClient> copy = new ArrayList<PyroClient>();
        copy.addAll(this.clients);
        return copy.iterator();
    }

    /**
     * Closes the server socket. Any current connections will continue.
     */

    public void close() throws IOException {
        this.selector.checkThread();

        this.serverKey.channel().close();
    }

    /**
     * Closes the server socket. Any current connections will be closed.
     */

    public void terminate() throws IOException {
        this.close();

        for (PyroClient client: this) {
            client.dropConnection();
        }
    }

    private void onReadyToAccept() throws IOException {
        this.selector.checkThread();

        final SocketChannel channel = ((ServerSocketChannel) serverKey
                .channel()).accept();

        final PyroSelector acceptedClientSelector;
        {
            if (this.selectorProvider == null)
                acceptedClientSelector = this.selector;
            else
                acceptedClientSelector = this.selectorProvider
                        .provideFor(channel);
        }

        if (acceptedClientSelector == this.selector) {
            SelectionKey clientKey = PyroClient.configure(
                    acceptedClientSelector, channel, false);
            PyroClient client = new PyroClient(acceptedClientSelector, this,
                    clientKey);
            this.fireAcceptedClient(client);
            this.clients.add(client);
        } else {
            // create client in PyroClient-selector thread
            acceptedClientSelector.scheduleTask(new Runnable() {
                @Override
                public void run() {
                    SelectionKey clientKey;
                    try {
                        clientKey = PyroClient.configure(
                                acceptedClientSelector, channel, false);
                    } catch (IOException exc) {
                        throw new IllegalStateException(exc);
                    }
                    final PyroClient client = new PyroClient(
                            acceptedClientSelector, PyroServer.this, clientKey);
                    PyroServer.this.fireAcceptedClient(client);

                    // add client to list in PyroServer-selector thread
                    PyroServer.this.selector().scheduleTask(new Runnable() {
                        @Override
                        public void run() {
                            PyroServer.this.clients.add(client);
                        }
                    });
                    PyroServer.this.selector().wakeup();
                }
            });
            acceptedClientSelector.wakeup();
        }
    }

    void fireAcceptedClient(PyroClient client) {
        for (PyroServerListener listener: this.listeners) {
            listener.acceptedClient(client);
        }
    }

    void onDisconnect(final PyroClient client) {
        if (this.selector().isNetworkThread()) {
            this.clients.remove(client);
        } else {
            // we are in the PyroClient-selector thread
            this.selector().scheduleTask(new Runnable() {
                @Override
                public void run() {
                    // call again from the PyroServer-selector thread
                    PyroServer.this.onDisconnect(client);
                }
            });
            this.selector().wakeup();
        }
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.serverKey.channel()
                + "]";
    }
}
