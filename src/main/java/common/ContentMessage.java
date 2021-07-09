package common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ContentMessage {

    private TipoRichiesta request;
    private String plate;
    private String brand;
    private Date date;
    private static final String dateFormat = "EEE MMM dd HH:mm:ss zzz yyyy";
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);


    public ContentMessage(TipoRichiesta request, String plate, String brand, Date date){
        this.request = request;
        this.plate = plate;
        this.date = date;
        this.brand = brand;
    }
    public ContentMessage(TipoRichiesta request, String plate, String brand){
        this.request = request;
        this.plate = plate;
        this.brand = brand;
        this.date = new Date();
    }
    @Override
    public String toString() {
        return request.name() + "," + plate.toString() + "," + brand + "," + date.toString() + "\n";
    }

    public synchronized static ContentMessage fromString(String str){
        String[] split = str.split(",");
        if (split.length != 4)
            throw new IllegalArgumentException("usage: $TipoRichiesta,$Targa,$Date");
        String date_str = split[3];
        Date date = null;
        try {
            date = dateFormatter.parse(date_str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new ContentMessage(TipoRichiesta.valueOf(split[0]), split[1], split[2], date);
    }

    public String getPlate() {
        return plate;
    }

    public Date getDate() {
        return date;
    }

    public TipoRichiesta getTipoRichiesta() {
        return request;
    }

    public String getBrand() {
        return brand;
    }
}
