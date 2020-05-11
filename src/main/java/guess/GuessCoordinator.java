package guess;

import cluster.management.ServiceRegistry;
import model.*;
import networking.OnRequestCallback;
import networking.WebClient;
import org.apache.zookeeper.KeeperException;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class GuessCoordinator implements OnRequestCallback {
    private static final String ENDPOINT = "/guess";
    private final ServiceRegistry workersServiceRegistry;
    private final WebClient client;

    public GuessCoordinator(ServiceRegistry workersServiceRegistry, WebClient client) {
        this.workersServiceRegistry = workersServiceRegistry;
        this.client = client;
    }

    public void sendTasksToWorkers(List<String> workers, String correct_password, ServiceRegistry workersServiceRegistry) throws KeeperException, InterruptedException {
        LinkedHashMap<String, Range> ranges = getRanges(correct_password, workers.size());
        boolean isCorrectPasswordGuessed = false;
        String solvedBy = null;
        while (!isCorrectPasswordGuessed) {
            if (workers.size() != workersServiceRegistry.getAllServiceAddresses().size()) {
                workers = workersServiceRegistry.getAllServiceAddresses();
                ranges = getRanges(correct_password, workers.size());
            }
            if (ranges == null) {
                System.out.println("Workers are not available. Waiting 30s...");
                Thread.sleep(30000);
                System.out.println("Retrying");
            } else {
                CompletableFuture<Password>[] futures = new CompletableFuture[workers.size()];
                int worker_index = 0;
                for (String key : ranges.keySet()) {
                    String worker = workers.get(worker_index);
                    Range range = ranges.get(key);
                    byte[] payload = SerializationUtils.serialize(range);

                    futures[worker_index] = client.sendTask(worker, payload);
                    worker_index++;
                }

                List<Password> passwords = new ArrayList<>();
                int worker_index_response = 0;
                for (CompletableFuture<Password> future : futures) {
                    try {
                        Password password = future.get();
                        passwords.add(password);
                        if (password.getPassword().equals(correct_password)) {
                            isCorrectPasswordGuessed = true;
                            ranges.get(password.getRange()).setCorrect(true);
                            solvedBy = workers.get(worker_index_response)+" Node ID: "+workersServiceRegistry.getNodes().get(workers.get(worker_index_response));
                            System.out.println("Password Solved by " + workers.get(worker_index_response)+" Node ID: "+workersServiceRegistry.getNodes().get(workers.get(worker_index_response)));
                        }
                        if (password.isInitial()) {
                            ranges.get(password.getRange()).setInitial(false);
                        }
                        ranges.get(password.getRange()).setCurrentSeed(password.getCurrentSeed());
                    } catch (InterruptedException | ExecutionException e) {
                    }
                    worker_index_response++;
                }
                if (isCorrectPasswordGuessed) {
                    int worker_index_correct = 0;
                    for (String key : ranges.keySet()) {
                        String worker = workers.get(worker_index_correct);
                        Range range = ranges.get(key);
                        if (!range.isCorrect()) {
                            range.setSolvedBy(solvedBy);
                        }
                        byte[] payload = SerializationUtils.serialize(range);
                        client.sendTask(worker, payload);
                        worker_index_correct++;
                    }
                }
            }
        }

    }

    private LinkedHashMap<String, Range> getRanges(String password, int nodeCount) {
        LinkedHashMap<String, Range> ranges = new LinkedHashMap<>();
        long result;
        if (nodeCount > 0) {
            result = 56800235584L / nodeCount;
            for (int i = 1; i <= nodeCount; i++) {
                Range range = new Range();
                range.setMinSeed(result * (i - 1));
                if (i == nodeCount) {
                    range.setMaxSeed(result * i + (nodeCount - 1));
                } else {
                    range.setMaxSeed(result * i);
                }
                ranges.put(range.getMinSeed() + "" + range.getMaxSeed(), range);
                System.out.println(range.getMinSeed()+" "+range.getMaxSeed());
            }
        } else {
            ranges = null;
        }

        return ranges;
    }

    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        return new byte[0];
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }

}
