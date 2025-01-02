package org.example.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class UserInfo {
    @Id
    private Long chatId;
    private String username;
    private String zone;
    private String regNum;
    private UserState states;
    private boolean isSubscribed;

    public UserInfo(long chatId, String username) {
        this.chatId = chatId;
        this.username = username;
        states = UserState.NONE;
        isSubscribed = false;
    }
    public UserInfo() {

    }
}
