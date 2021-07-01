package common;

import java.util.Random;

public class Targhe {
    private static int length = 6;
    private static char[] arr = new char[length];
    private static boolean init = false;
    private static int firstCharCode;
    private static int lastCharCode;

    public Targhe(){
        this(6, 65, 90);
    }

    public Targhe(int length, int firstCharCode, int lastCharCode){
        if (length <= 0)
            throw new IllegalArgumentException("length must be positive");
        if (firstCharCode > lastCharCode)
            throw new IllegalArgumentException(
                    "first char code must be less then or equal to last char code");
        Targhe.firstCharCode = firstCharCode;
        Targhe.lastCharCode = lastCharCode;
        for (int i=0; i<length; ++i) arr[i] = (char) firstCharCode;
        arr[length-1] = (char) (firstCharCode - 1);
    }

    public Targhe(String str){
        String[] split = str.split(",");
        if (split.length != 3)
            throw new IllegalArgumentException("usage: $length,$firstCharCode,$lastCharCode");
        this.length = Integer.parseInt(split[0]);
        this.firstCharCode = Integer.parseInt(split[1]);
        this.lastCharCode = Integer.parseInt(split[2]);
    }

    public String nextTarga(){
        increment(length-1);
        return String.valueOf(arr);
    }

    private boolean nextChar(int i){
        if (arr[i] + 1 <= lastCharCode){
            arr[i] = (char) ((int) arr[i] + 1);
            return false;
        }
        else{
            arr[i] = (char) firstCharCode;
            return true;
        }
    }

    private void increment(int i){
        if (i < 0 || i >= length)
            return;
        if (nextChar(i))
            increment(i - 1);
    }

    public boolean isTarga(String arg){
        if (arg.length() != length)
            return false;
        for (char c: arg.toCharArray()) {
            if ((int) c < firstCharCode || (int) c > lastCharCode)
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return length + "," + firstCharCode + "," + lastCharCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Targhe))
            return false;
        Targhe targhe = (Targhe) obj;
        return this.toString().equals(targhe.toString());
    }
    public String random(){
        StringBuilder stringBuilder = new StringBuilder();
        for(int i=0; i<length; ++i)
            stringBuilder.append((char)(new Random().nextInt(lastCharCode - firstCharCode) + firstCharCode));
        return stringBuilder.toString();
    }
}
