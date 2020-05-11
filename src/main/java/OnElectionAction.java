import cluster.management.OnElectionCallback;
import cluster.management.ServiceRegistry;
import networking.WebClient;
import networking.WebServer;
import org.apache.zookeeper.KeeperException;
import guess.GuessCoordinator;
import guess.GuessWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OnElectionAction implements OnElectionCallback {
    private final ServiceRegistry workersServiceRegistry;
    private final ServiceRegistry coordinatorsServiceRegistry;
    private final int port;
    private int node_count;
    private List<String> nodes;
    private WebServer webServer;
    private static final String PASSWORD_FILE = "D:\\IDEA Projects\\Distributed Password Cracker\\src\\main\\resources\\passwords.txt";

    public OnElectionAction(ServiceRegistry workersServiceRegistry,
                            ServiceRegistry coordinatorsServiceRegistry,
                            int port) {
        this.workersServiceRegistry = workersServiceRegistry;
        this.coordinatorsServiceRegistry = coordinatorsServiceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() {
        workersServiceRegistry.unregisterFromCluster();
        workersServiceRegistry.registerForUpdates();

        if (webServer != null) {
            webServer.stop();
        }

        GuessCoordinator guessCoordinator = new GuessCoordinator(workersServiceRegistry, new WebClient());
        webServer = new WebServer(port, guessCoordinator);
        webServer.startServer();
        Thread cordinationThread = new Thread(() -> {
            try {
                Thread.sleep(30000);
                nodes = workersServiceRegistry.getAllServiceAddresses();
                node_count = nodes.size();

                List<String> passwords = new ArrayList<>();
                try (BufferedReader br = Files.newBufferedReader(Paths.get(PASSWORD_FILE))) {

                    passwords = br.lines().collect(Collectors.toList());

                } catch (IOException e) {
                    e.printStackTrace();
                }
                passwords.forEach(System.out::println);
                int i = 0;
                while (i < passwords.size()) {
                    System.out.println("Trying password " + passwords.get(i));
                    if (node_count > 0) {
                        guessCoordinator.sendTasksToWorkers(nodes, passwords.get(i), workersServiceRegistry);
                        i++;
                    } else {
                        System.out.println("Workers are not available. Waiting 30s...");
                        Thread.sleep(30000);
                        System.out.println("Retrying");
                    }
                    nodes = workersServiceRegistry.getAllServiceAddresses();
                    node_count = nodes.size();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        cordinationThread.start();
        try {
            String currentServerAddress =
                    String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(), port, guessCoordinator.getEndpoint());
            coordinatorsServiceRegistry.registerToCluster(currentServerAddress);
        } catch (InterruptedException | UnknownHostException | KeeperException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onWorker() {
        GuessWorker guessWorker = new GuessWorker();
        if (webServer == null) {
            webServer = new WebServer(port, guessWorker);
            webServer.startServer();
        }

        try {
            String currentServerAddress =
                    String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(), port, guessWorker.getEndpoint());

            workersServiceRegistry.registerToCluster(currentServerAddress);
        } catch (InterruptedException | UnknownHostException | KeeperException e) {
            e.printStackTrace();
            return;
        }
    }
}
