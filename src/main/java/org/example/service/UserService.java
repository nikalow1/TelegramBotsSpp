package org.example.service;

import org.example.dao.UserInfoRepository;
import org.example.models.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class UserService {

    @Autowired
    private UserInfoRepository userInfoRepository;

    public void updateUserInfo(UserInfo userInfo) {
        UserInfo existingUserInfo = userInfoRepository.findByChatId(userInfo.getChatId());

        if (existingUserInfo != null) {
            boolean isUpdated = false;

            if (!Objects.equals(existingUserInfo.getRegNum(), userInfo.getRegNum())) {
                existingUserInfo.setRegNum(userInfo.getRegNum());
                isUpdated = true;
            }

            if (!existingUserInfo.getStates().equals(userInfo.getStates())) {
                existingUserInfo.setStates(userInfo.getStates());
                isUpdated = true;
            }

            if (existingUserInfo.isSubscribed() != userInfo.isSubscribed()) {
                existingUserInfo.setSubscribed(userInfo.isSubscribed());
                isUpdated = true;
            }

            if (!Objects.equals(existingUserInfo.getZone(), userInfo.getZone())){
                existingUserInfo.setZone(userInfo.getZone());
                isUpdated = true;
            }

            if (isUpdated) {
                userInfoRepository.save(existingUserInfo);
                System.out.println("Информация о пользователе обновлена.");
            } else {
                System.out.println("Информация о пользователе не изменилась.");
            }
        } else {
            System.out.println("Пользователь с chatId " + userInfo.getChatId() + " не найден.");
        }
    }
}