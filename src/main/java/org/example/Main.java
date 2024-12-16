package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        try {
            Config config = new Config();
            String botToken = config.getBotToken();

            TelegramBotsApi tba = new TelegramBotsApi(DefaultBotSession.class);
            Bot bot = new Bot(botToken);
            tba.registerBot(bot);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

            Runnable task = () -> {
                bot.auto();
            };

            scheduler.scheduleAtFixedRate(task, 0, 60, TimeUnit.MINUTES);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}