package org.example;

public class UserInfo {
    private String regNum;
    private UserState states;
    private boolean isSubscribed;

    public UserInfo(String regNum) {
        this.regNum = regNum;
        this.states = UserState.NONE;
        this.isSubscribed = false;
    }

    public String getRegNum() {
        return regNum;
    }

    public void setRegNum(String regNum) {
        this.regNum = regNum;
    }

    public UserState getStates(){
        return states;
    }

    public void setStates(UserState states){
        this.states = states;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    public void setSubscribed(boolean subscribed) {
        isSubscribed = subscribed;
    }
}
