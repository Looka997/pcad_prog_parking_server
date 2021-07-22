package server;

import com.google.gson.InstanceCreator;
import common.ContentMessage;
import common.TipoRichiesta;

import java.lang.reflect.Type;
import java.time.Instant;

public class MessageInstanceCreator implements InstanceCreator<ContentMessage> {

    private TipoRichiesta request;
    private String plate;
    private String brand;
    private Instant date;

    @Override
    public ContentMessage createInstance(Type type) {
        return date == null? new ContentMessage(request, plate, brand) : new ContentMessage(request, plate, brand, date);
    }
}
