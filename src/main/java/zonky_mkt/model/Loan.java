package zonky_mkt.model;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class Loan {

    private int id;
    private URL url;
    private String name;
    private String story;
    private OffsetDateTime datePublished;

    public Loan(int id, URL url, String name, String story, OffsetDateTime datePublished) {
        this.id = id;
        this.url = url;
        this.name = name;
        this.story = story;
        this.datePublished = datePublished;
    }

    public static Loan fromMap(Map<String, Object> record) {
        try {
            return new Loan((int) record.get("id"),
                    new URL((String) record.get("url")),
                    (String) record.get("name"),
                    (String) record.get("story"),
                    OffsetDateTime.parse((String) record.get("datePublished")));
        } catch (Exception e) {
            throw new RuntimeException("Can't obtain Loan value from given map " + record, e);
        }
    }

    public static String getFields() {
        return Arrays.stream(Loan.class.getDeclaredFields())
                .filter(field -> Modifier.isPrivate(field.getModifiers()))
                .map(Field::getName)
                .collect(Collectors.joining(","));
    }

    public int getId() {
        return id;
    }

    public URL getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getStory() {
        return story;
    }

    public OffsetDateTime getDatePublished() {
        return datePublished;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Loan loan = (Loan) o;

        return id == loan.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Loan.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("url=" + url)
                .add("name='" + name + "'")
                .add("story=" + (story != null ? "'" + story.replace('\n', ' ').substring(0, Math.min(story.length(), 25)) + "...'" : null))
                .add("datePublished=" + datePublished)
                .toString();
    }
}
