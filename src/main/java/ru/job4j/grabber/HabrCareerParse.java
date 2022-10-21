package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final int COUNT_PAGE = 1;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
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

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> post = new ArrayList<>();
        for (int i = 1; i <= COUNT_PAGE; i++) {
            Connection connection = Jsoup.connect(String.format("%s%d", link, i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> post.add(parseElement(row)));
        }
        return post;
    }

    private Post parseElement(Element element) {
        Element titleElement = element.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        Element dateElement = element.select(".vacancy-card__date").first();
        Element dateElementFull = dateElement.child(0);
        String vacancyName = titleElement.text();
        String linkVacancy = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        String description = retrieveDescription(linkVacancy);
        String dateFull = dateElementFull.attr("datetime");
        LocalDateTime dateTime = dateTimeParser.parse(dateFull);
        System.out.printf("%s %s %s %s%n", vacancyName, linkVacancy, dateFull, description);
        return new Post(vacancyName, linkVacancy, description, dateTime);
    }

}
