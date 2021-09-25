package common;

import java.time.Instant;
import java.util.Objects;

public class ContentMessage {

    private final TipoRichiesta request;
    private final String plate;
    private final String brand;
    private final Instant date;

    private void checkArguments(){
        Objects.requireNonNull(date);
        if (plate.equals(""))
            throw new IllegalArgumentException("plate must not be empty");
    }

    public ContentMessage(TipoRichiesta request, String plate, String brand, Instant date){
        this.request = request;
        this.plate = plate;
        this.date = date;
        this.brand = Brands.valueOf(brand).name();
        checkArguments();
    }
    public ContentMessage(TipoRichiesta request, String plate, String brand){
        this(request, plate, brand, Instant.now());
    }
    @Override
    public String toString() {
        return request.name() + "," + plate + "," + brand + "," + date.toString() + "\n";
    }

    public synchronized static ContentMessage fromString(String str){
        String[] split = str.split(",");
        if (split.length != 4)
            throw new IllegalArgumentException("usage: $TipoRichiesta,$Targa,$Instant");
        return new ContentMessage(TipoRichiesta.valueOf(split[0]), split[1], split[2], Instant.parse(split[3]));
    }

    public String getPlate() {
        return plate;
    }

    public Instant getDate() {
        return date;
    }

    public TipoRichiesta getTipoRichiesta() {
        return request;
    }

    public String getBrand() {
        return brand;
    }
}
