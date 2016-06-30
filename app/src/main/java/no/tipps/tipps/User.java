package no.tipps.tipps;

/**
 * Created by rikardeide on 29/06/16.
 *
 */


public class User {

    public static final int USER_PROFILE_NEUTRAL = 0;
    public static final int USER_PROFILE_CHEAP = 1;
    public static final int USER_PROFILE_BIG_SPENDER = 2;

    private String name;
    private int profile;

    public User(){
        name = "A girl has no name";
        profile = USER_PROFILE_NEUTRAL;
    }

    public User(String name, int profile){
        this.name = toTitleCase(name);
        this.profile = profile;
    }

    public String getName() {
        return name;
    }

    public int getProfile() {
        return profile;
    }

    private String toTitleCase(String s){
        return s.toUpperCase(); // // TODO: 30/06/16 Implement title case? 
    }
}
