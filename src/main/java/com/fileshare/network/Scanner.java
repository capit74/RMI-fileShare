package com.fileshare.network;

import com.fileshare.communication.PeerService;
import com.fileshare.concurrency.Parallel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * @author Jan Paw
 *         Date: 6/8/13
 */
public enum Scanner {
    INSTANCE;
    static volatile LinkedList<Address> addresses = new LinkedList<>();
    static volatile LinkedList<Connection> connections = new LinkedList<>();
    private static final Logger logger = LogManager.getLogger(PeerService.class.getName());

    public static LinkedList<Connection> scan() {
        addresses = new LinkedList<>();
        connections = new LinkedList<>();

        String localPrefix = "0.0.0.";
        String localPostfix = "0";
        try {
            InetAddress address = InetAddress.getLocalHost();
            String hostAddress = address.getHostAddress();
            localPrefix = hostAddress.substring(0, hostAddress.lastIndexOf(".") + 1);
            localPostfix = hostAddress.substring(hostAddress.lastIndexOf(".") + 1, hostAddress.length());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        for (int host = 0; host < 256; host++) {
            if (host != Integer.valueOf(localPostfix))
                addresses.add(new Address(localPrefix + host, "peer"));
        }

        Parallel.For(255, addresses, new Parallel.Operation<Address>() {
            @Override
            public void perform(Address address) {
                if (address.isReachable(Connection.TIMEOUT))
                    connections.add(new Connection(address));
            }
        });

        logger.info("Peers in network: " + connections.size());
        return connections;
    }
}
