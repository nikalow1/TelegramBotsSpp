package org.example;

import org.example.dao.ZoneRepository;
import org.example.models.UserInfo;
import org.example.models.Zone;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
@Component
public class KeyboardManager {

    private final ZoneRepository zoneRepository;

    public KeyboardManager(ZoneRepository zoneRepository) {
        this.zoneRepository = zoneRepository;
    }

    public InlineKeyboardMarkup createInlineKeyboard(UserInfo userInfo) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        String selectedZone = userInfo.getZone();

        for (Zone zone : zoneRepository.findAll()) {
            String buttonText = selectedZone != null && selectedZone.equals(zone.getEngName())
                    ? "✅ " + zone.getRuName() + " ✅"
                    : zone.getRuName();
            rows.add(createButtonRow(buttonText, zone.getEngName()));
        }

        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    private List<InlineKeyboardButton> createButtonRow(String buttonText, String callbackData) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text(buttonText).callbackData(callbackData).build());
        return row;
    }
}