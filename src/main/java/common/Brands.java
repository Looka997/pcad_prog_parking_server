package common;

import java.util.Arrays;
import java.util.Random;

public enum Brands {
    HONDA,
    MAZDA,
    FIAT,
    VOLKSWAGEN,
    TOYOTA,
    BMW,
    ALFA;

    public static Brands random(){
        return Brands.values()[new Random().nextInt(Brands.values().length)];
    }

    public static String[] allNames(){
        return Arrays.toString(Brands.values()).replaceAll("^.|.$", "").split(", ");
    }
}
