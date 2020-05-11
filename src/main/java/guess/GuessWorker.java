package guess;

import model.Password;
import model.Range;
import model.SerializationUtils;
import networking.OnRequestCallback;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class GuessWorker implements OnRequestCallback {
    private static final String ENDPOINT = "/task";

    public byte[] handleRequest(byte[] requestPayload) {
        Range range = (Range) SerializationUtils.deserialize(requestPayload);
        Password password = createResult(range);
        return SerializationUtils.serialize(password);
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }

    private Password createResult(Range range) {
        Password password = null;
        if (!range.isCorrect()) {
            password = new Password();
            if (range.getSolvedBy() != null) {
                System.out.println("Password Solved by " + range.getSolvedBy());
            } else {
                if (range.getCurrentSeed() < range.getMaxSeed()) {
                    if (range.isInitial()) {
                        password.setPassword(generatePassword(range.getMinSeed()));
                        password.setInitial(true);
                        password.setCurrentSeed(range.getMinSeed());
                    } else {
                        password.setCurrentSeed(range.getCurrentSeed()+1);
                        password.setPassword(generatePassword(password.getCurrentSeed()));
                        password.setInitial(false);
                        System.out.println(generatePassword(password.getCurrentSeed()) + " Wrong");
                    }

                } else {
                    password = new Password();
                    password.setPassword("end");
                }
                password.setRange(range.getMinSeed() + "" + range.getMaxSeed());
            }

        } else {
            password = new Password();
            password.setPassword(null);
            System.out.println(generatePassword(range.getCurrentSeed()) + " Correct");
        }
        return password;
    }


    private String generatePassword(long seed) {
        char[] alphanumerics = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        return RandomStringUtils.random(6,
                0,
                61,
                false,
                false,
                alphanumerics, new Random(seed));

    }
}
