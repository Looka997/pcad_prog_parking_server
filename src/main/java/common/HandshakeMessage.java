package common;

import java.util.Objects;

public class HandshakeMessage {
    Targhe targhe;
    public HandshakeMessage(String msg){
        Objects.requireNonNull(msg);
        targhe = new Targhe(msg);
    }

    public HandshakeMessage(Targhe targhe){
        this.targhe = targhe;
    }

    public Targhe getTarghe() {
        return targhe;
    }

    @Override
    public String toString() {
        return targhe.toString();
    }
}
