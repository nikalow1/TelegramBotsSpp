package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.util.HashMap;


public class Bot extends TelegramLongPollingBot {
    public HashMap<Long, UserInfo> userInfos;

    public Bot(String botToken) {
        super(botToken);
        userInfos = new HashMap<>();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();

            String text = update.getMessage().getText();
            String userName = update.getMessage().getChat().getFirstName() + " "
                    + update.getMessage().getChat().getLastName();
            userInfos.putIfAbsent(chatId, new UserInfo(null));
            UserInfo userInfo = userInfos.get(chatId);

            if(userInfo.getStates() == UserState.WAITING_FOR_REG_NUM) {
                    userInfo.setRegNum(text.trim());
                    sendMessage(chatId, "Регистрационный номер установлен: " + text);
                    userInfo.setStates(UserState.NONE);
                    return;
            }

                switch (text) {
                    case "/start":
                        sendMessage(chatId, "Добро пожаловать в бота! Используйте команду /set <Регистрационный номер> для установки номера и /get для получения информации.");
                        break;
                    case "/get":
                        String regNum = userInfo.getRegNum();
                        if (regNum != null) {
                            sendRegNum(chatId, regNum);
                        } else {
                            sendMessage(chatId, "Пожалуйста, укажите регистрационный номер командой /set");
                        }
                        break;
                    case "/set":
                        sendMessage(chatId, "Укажите регистрационный номер");
                        userInfo.setStates(UserState.WAITING_FOR_REG_NUM);
                        break;
                    case "/notification":
                        if(!userInfo.isSubscribed()){
                            userInfo.setSubscribed(true);
                            sendMessage(chatId, "Оповещения каждый час включены");
                        } else {
                            userInfo.setSubscribed(false);
                            sendMessage(chatId, "Оповещения выключены");
                        }
                        break;
                    default:
                        sendMessage(chatId, "Неизвестная команда!");
                        System.out.println(userName + " " + text);
                        break;
                }
        }
    }

    @Override
    public String getBotUsername() {
        return "DeclarantQueue";
    }

    protected void sendMessage(long chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendRegNum(long chatId, String regNum) {
        String message = WebScraper.fetchRegNumData(regNum);
        sendMessage(chatId,message); // Отправляем сообщение
    }
    public String getUserRegNum(long chatId) {
        UserInfo userInfo = userInfos.get(chatId);
        return userInfo != null ? userInfo.getRegNum() : null;
    }


    public void auto(){
        for (Long chatId : userInfos.keySet()) {
            UserInfo userInfo = userInfos.get(chatId);
            if(userInfo.isSubscribed()) {
                String regNum = getUserRegNum(chatId);
                if (regNum != null) {
                    String message = WebScraper.fetchRegNumData(regNum);
                    if (message.contains("Порядок вызова")) {
                        String orderIdStr = message.split("\n")[0].split(":")[1].trim();
                        orderIdStr = orderIdStr.replaceAll("[\uD83D\uDFE2\uD83D\uDFE1]", "");
                        int orderId = Integer.parseInt(orderIdStr);
                        if (orderId < 50) {
                            sendMessage(chatId, message);
                        }
                    }
                }
            }
        }
    }

}