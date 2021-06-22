package common;

public class Targa {
    private String targa;
    private Targhe targhe;
    public Targa(Targhe targhe, String arg){
        if (!targhe.isTarga(arg))
            throw new IllegalArgumentException("Targa as argument isn't a valid Targa");
        targa = arg;
        this.targhe = targhe;
    }

    public Targhe getTarghe() {
        return targhe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Targa targa1 = (Targa) o;

        if (!targa.equals(targa1.targa)) return false;
        return targhe.equals(targa1.targhe);
    }

    @Override
    public int hashCode() {
        int result = targa.hashCode();
        result = 31 * result + targhe.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return targa;
    }
}

