import model.Range;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

public class Test {

    public static void main(String[] args) {
        char[] alphanumerics = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
//        for (int i = 0; i < 10; i++) {
            System.out.println(RandomStringUtils.random(6,
                    0,
                    61,
                    false,
                    false,
                    alphanumerics,new Random(28400117900L))
            );
//        }


    }
}
