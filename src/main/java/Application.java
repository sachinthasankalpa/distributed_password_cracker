import cluster.management.LeaderElection;
import cluster.management.ServiceRegistry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class Application implements Watcher {
    private static final String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static final int SESSION_TIMEOUT = 4000;
    private ZooKeeper zooKeeper;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        int currentServerPort = 8081;
        if (args.length == 1) {
            currentServerPort = Integer.parseInt(args[0]);
        }
        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToZookeeper();

        ServiceRegistry workersServiceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.WORKERS_REGISTRY_ZNODE);
        ServiceRegistry coordinatorsServiceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.COORDINATORS_REGISTRY_ZNODE);

        OnElectionAction onElectionAction = new OnElectionAction(workersServiceRegistry, coordinatorsServiceRegistry, currentServerPort);

        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction);
        leaderElection.volunteerForLeadership();
        leaderElection.reelectLeader();

        application.run();
        application.close();
        System.out.println("Exiting application due to disconnection from Zookeeper");
    }

    public ZooKeeper connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
        return zooKeeper;
    }

    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Zookeeper Connection Successful");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Event - Disconnection from Zookeeper");
                        zooKeeper.notifyAll();
                    }
                }
        }
    }
}
