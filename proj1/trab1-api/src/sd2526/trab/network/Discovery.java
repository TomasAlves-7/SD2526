package sd2526.trab.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Optional;
import java.util.TreeSet;
import java.util.logging.Logger;

import static sd2526.trab.network.ServiceAnnouncer.DELIMITER;
import static sd2526.trab.network.ServiceAnnouncer.DISCOVERY_ADDR;

public class Discovery {

    private static final Logger log = Logger.getLogger(Discovery.class.getName());

    private static final int MAX_DATAGRAM_SIZE = 65536;

    private final MulticastSocket ms;
    private final HashMap<String, TreeSet<URI>> announcements = new HashMap<>();

    private static Discovery singleton;

    public static Discovery getInstance() {
        if (singleton == null) {
            synchronized (Discovery.class) {
                if (singleton == null)
                    singleton = new Discovery();
            }
        }
        return singleton;
    }

    private Discovery() {
        try {
            this.ms = new MulticastSocket(DISCOVERY_ADDR.getPort());
            ms.joinGroup(DISCOVERY_ADDR, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
            new Thread(this::listenAnnouncements).start();
        } catch (IOException e) {
            log.severe("Unable to compute MulticastSocket");
            throw new RuntimeException(e);
        }
    }

    private void listenAnnouncements() {
        DatagramPacket pkt = new DatagramPacket(new byte[MAX_DATAGRAM_SIZE], MAX_DATAGRAM_SIZE);
        for (;;) {
            try {
                pkt.setLength(MAX_DATAGRAM_SIZE);
                ms.receive(pkt);
                String msg = new String(pkt.getData(), 0, pkt.getLength());
                String[] msgElems = msg.split(DELIMITER);
                if (msgElems.length == 2) {
                    log.fine(String.format("FROM %s (%s) : %s\n", pkt.getAddress().getHostName(),
                            pkt.getAddress().getHostAddress(), msg));
                    storeAnnouncement(msgElems[0], msgElems[1]);
                } else {
                    log.warning(String.format("Received unexpected packet in discovery multicast address: %s", msg));
                }
            } catch (IOException e) {
                log.warning("Unable to receive announcement packet");
                e.printStackTrace();
            }
        }
    }

    private void storeAnnouncement(String service, String uriStr) {
        var uri = convertToUri(uriStr);
        uri.ifPresentOrElse(u -> {
            synchronized (this) {
                TreeSet<URI> uris = this.announcements.computeIfAbsent(service, k -> new TreeSet<>());
                uris.add(u);
                this.notify();
            }
        }, () -> log.warning(String.format("Unable to parse announcement URI: %s", uriStr)));
    }

    private Optional<URI> convertToUri(String announcement) {
        try {
            return Optional.of(new URI(announcement));
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the known services.
     *
     * @param serviceName the name of the service being discovered
     * @param minReplies  - minimum number of requested URIs. Blocks until the
     *                    number is satisfied.
     * @return an array of URI with the service instances discovered.
     *
     */
    public URI[] knownUrisOf(String serviceName, String domain, int minReplies) {
        synchronized (this) {
            while(true) {
                String fullKey = serviceName + "@" + domain;
                var uris = this.announcements.get(fullKey);
                if(uris != null && uris.size() >= minReplies) {
                    return uris.toArray(new URI[0]);
                }
                try {
                    log.info("Looking for key: " + fullKey + " in announcements: " + this.announcements.keySet());
                    this.wait();
                } catch (InterruptedException ignored) {}
            }
        }
    }

    //Clears Discovery instance in tests
    public static void teardown() {
        singleton = null;
    }

}


