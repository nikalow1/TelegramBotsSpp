package org.example;

import org.example.dao.UserInfoRepository;
import org.example.models.UserInfo;
import org.example.models.UserState;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class Bot extends TelegramLongPollingBot {

    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private KeyboardManager keyboardManager;

    public Bot(@Value("${bot.token}") String botToken) {
        super(botToken);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            UserInfo userInfo = userInfoRepository.findByChatId(chatId);
            if (userInfo == null) {
                String userName = update.getMessage().getChat().getFirstName() + " " +
                        update.getMessage().getChat().getLastName();
                userInfo = new UserInfo(chatId, userName);
                userInfoRepository.save(userInfo);
            }

            String text = update.getMessage().getText();
            if (userInfo.getStates() == UserState.WAITING_FOR_REG_NUM) {
                userInfo.setRegNum(text.trim());
                sendMessage(chatId, "Регистрационный номер установлен: " + text);
                userInfo.setStates(UserState.NONE);
                userService.updateUserInfo(userInfo);
                return;
            }

            switch (text) {
                case "/start":
                    sendMessage(chatId, "👋 Привет! Я — ваш помощник в отслеживании очереди грузовых автомобилей на пропускных пунктах РБ.\n\n" +
                            "🛠️ С помощью меня вы можете:\n" +
                            "1. 📋 Получить порядок вашей очереди.\n" +
                            "2. 🌍 Выбрать зону для отслеживания.\n" +
                            "3. 🔔 Получать уведомления каждый час когда ваша очередь будет меньше 50!\n" +
                            "🤖 Чтобы начать, выберите зону, в дальнейшем ее можно будет поменять командой из меню или ввести /zone\n" +
                            "Все команды находятся в меню снизу слева!");
                    sendZoneSelectionMenu(chatId, userInfo);
                    break;
                case "/get":
                    String regNum = userInfo.getRegNum();
                    String zone = userInfo.getZone();
                    if (regNum != null && zone != null) {
                        sendRegNum(chatId, regNum, zone);
                    } else if(zone == null){
                        sendMessage(chatId, "Пожалуйста, укажите зону командой /zone");
                    } else {
                        sendMessage(chatId, "Пожалуйста, укажите регистрационный номер командой /set");
                    }
                    break;
                case "/set":
                    sendMessage(chatId, "Укажите регистрационный номер");
                    userInfo.setStates(UserState.WAITING_FOR_REG_NUM);
                    break;
                case "/notification":
                    toggleNotification(userInfo, chatId);
                    break;
                case "/zone":
                    sendZoneSelectionMenu(chatId, userInfo);
                    break;
                default:
                    sendMessage(chatId, "Неизвестная команда!");
                    break;
            }
            userService.updateUserInfo(userInfo);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update);
        }
    }

    private void handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        setZone(chatId, callbackData);
        updateMainMenu(chatId, messageId, userInfoRepository.findByChatId(chatId));
    }

    private void sendZoneSelectionMenu(long chatId, UserInfo userInfo) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выберите зону:");
        sendMessage.setReplyMarkup(keyboardManager.createInlineKeyboard(userInfo));

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void updateMainMenu(long chatId, int messageId, UserInfo userInfo) {
        InlineKeyboardMarkup keyboardMarkup = keyboardManager.createInlineKeyboard(userInfo);

        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(chatId);
        editMessageReplyMarkup.setMessageId(messageId);
        editMessageReplyMarkup.setReplyMarkup(keyboardMarkup);

        try {
            execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void setZone(long chatId, String zone) {
        UserInfo userInfo = userInfoRepository.findByChatId(chatId);
        userInfo.setZone(zone);
        userInfo.setStates(UserState.NONE);
        userService.updateUserInfo(userInfo);
    }

    private void toggleNotification(UserInfo userInfo, long chatId) {
        if (!userInfo.isSubscribed()) {
            userInfo.setSubscribed(true);
            sendMessage(chatId, "Оповещения каждый час включены");
        } else {
            userInfo.setSubscribed(false);
            sendMessage(chatId, "Оповещения выключены");
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

    private void sendRegNum(long chatId, String regNum, String zone) {
        String message = WebScraper.fetchRegNumData(regNum, zone);
        sendMessage(chatId, message);
    }

    public void auto() {
        List<UserInfo> subscribedUsers = userInfoRepository.findAll()
                .stream()
                .filter(UserInfo::isSubscribed)
                .collect(Collectors.toList());
        for (UserInfo userInfo : subscribedUsers) {
            String regNum = userInfo.getRegNum();
            String zone = userInfo.getZone();
            if (regNum != null && zone != null) {
                String message = WebScraper.fetchRegNumData(regNum, zone);
                if (message.contains("Порядок вызова")) {
                    String orderIdStr = message.split("\n")[0].split(":")[1].trim();
                    orderIdStr = orderIdStr.replaceAll("[\uD83D\uDFE2\uD83D\uDFE1]", "");
                    int orderId = Integer.parseInt(orderIdStr);
                    if (orderId < 50) {
                        sendMessage(userInfo.getChatId(), message);
                    }
                }
            }
        }
    }
}