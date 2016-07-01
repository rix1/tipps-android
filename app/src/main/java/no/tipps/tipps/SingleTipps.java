package no.tipps.tipps;

/**
 * Created by rikardeide on 01/07/16.
 */
public class SingleTipps {

    private String title;
    private String message;
    private int price;
    public static Boolean isSet;

    public SingleTipps(){
        isSet = false;
    }

    public SingleTipps(String title, String message, String price) {
        this.title = title;
        this.message = message;
        this.price = Integer.parseInt(price);
        isSet = true;
    }


    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public int getPrice() {
        return price;
    }

    public boolean isSet(){
        return isSet;
    }
}
