package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    private static final int COUNT_PAGE = 5;

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= COUNT_PAGE; i++) {
            Connection connection = Jsoup.connect(String.format("%s?page=%d", PAGE_LINK, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                Element dateElement = row.select(".vacancy-card__date").first();
                Element dateElementFull = dateElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String description = new HabrCareerParse().retrieveDescription(link);
                String dateFull = dateElementFull.attr("datetime");
                System.out.printf("%s %s %s %s%n", vacancyName, link, dateFull, description);
            });
        }
    }

    private String retrieveDescription(String link) {
        Connection connection = Jsoup.connect(link);
        try {
            Document document = connection.get();
            Elements rows = document.select(".style-ugc");
            return rows.text();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}