package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WebScraper {
    public static String fetchRegNumData(String regNum) {
        StringBuilder result = new StringBuilder();
        try {
            Document doc = Jsoup.connect("https://mon.declarant.by/zone/kozlovichi").get();
            Elements rows = doc.select("tr.cdk-row");
            System.out.println(doc);
            boolean found = false;
            for (Element row : rows) {
                String currentRegNum = row.select("td.cdk-column-regnum").text();
                if (currentRegNum.equalsIgnoreCase(regNum)) {
                    String orderId = row.select("td.cdk-column-order_id").text();
                    int order = orderId.isEmpty() ? 0 : Integer.parseInt(orderId);
                    String typeQueue = row.select("td.cdk-column-type_queue").text();
                    String regDate = row.select("td.cdk-column-registration_date").text();
                    String editDate = row.select("td.cdk-column-changed_date").text();
                    String status = row.select("td.cdk-column-status").text();
                    String emoji = "\uD83D\uDFE2";
                    if(status.equals("Вызван в ПП")){
                        emoji = "\uD83D\uDFE1";
                    }

                    result.append(emoji).append("Порядок вызова: ").append(order).append(emoji)
                            .append("\nРегистрационный номер: ").append(currentRegNum)
                            .append("\nТип очереди: ").append(typeQueue)
                            .append("\nДата регистрации: ").append(regDate)
                            .append("\nСтатус изменен: ").append(editDate)
                            .append("\nСтатус: ").append(status).append(emoji)
                            .append("\nПримерное время запуска: ").append(estimatedTime(order, doc))
                            .append("\n");
                    found = true;
                    break;
                }
            }
            if (!found) {
                result.append("Регистрационный номер ").append(regNum).append(" не найден.");
            }

        } catch (IOException e) {
            return "Ошибка при получении данных: " + e.getMessage();
        }

        return result.toString();
    }
    public static String estimatedTime(int orderId, Document doc){
        Elements stats = doc.select(".zone-statistics-wrapper p span.font-bold");
        double last24Hours = 0;
        if (stats.size() >= 2) {
            last24Hours = Integer.parseInt(stats.get(1).text()); // Значение за последние 24 часа
        } else {
            System.out.println("Не удалось найти нужные значения.");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.yyyy");

        LocalDateTime now = LocalDateTime.now();

        double hoursToAdd = orderId / (last24Hours / 24);

        long hours = (long) hoursToAdd;
        long minutes = (long) ((hoursToAdd % 1) * 60);

        LocalDateTime newDateTime = now.plusHours(hours).plusMinutes(minutes);

        return newDateTime.format(formatter);

    }
}
