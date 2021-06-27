package common;

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
}
