package common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ContentMessage {

    private TipoRichiesta tipoRichiesta;
    private String targa;
    private String marca;
    private Date date;
    private static final String dateFormat = "EEE MMM d HH:mm:ss zzz yyyy";
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);


    public ContentMessage(TipoRichiesta tipoRichiesta, String targa, String marca, Date date){
        this.tipoRichiesta = tipoRichiesta;
        this.targa = targa;
        this.date = date;
        this.marca = marca;
    }
    @Override
    public String toString() {
        return tipoRichiesta.name() + "," + targa.toString() + "," + marca + "," + date.toString() + "\n";
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

    public String getTarga() {
        return targa;
    }

    public Date getDate() {
        return date;
    }

    public TipoRichiesta getTipoRichiesta() {
        return tipoRichiesta;
    }

    public String getMarca() {
        return marca;
    }
}
