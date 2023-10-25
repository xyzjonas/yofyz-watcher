package com.brauer.android.yofyz;

import android.support.annotation.NonNull;

import org.jsoup.nodes.Element;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Table object holding semi-transformed data
 *
 * Used as the firs parser of the raw HTML
 * ...many ugly things might be hidden here, BEWARE!
 */
public class YogaTable {

    // expected HTML attributes of the table (3 more tables expected, class is used to identify the proper one)
    public static String TABLE_TAG = "table";
    public static String YOGA_TABLE_CSS_CLASS = "jsRozvrh";

    private TimesHeader times;
    private List<Row> rows;

    private YogaTable(TimesHeader times, List<Row> rows) {
        this.times = times;
        this.rows = rows;
    }

    /**
     * @return Get Yoga items in proper format, each object containing date, time and data
     */
    public List<YogaClass> getYogaItems() {
        return rows.stream()
                .filter(Objects::nonNull)
                .map(row -> tableRowToYogaClasses(row, times))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
    private List<YogaClass> tableRowToYogaClasses(Row tableRow, TimesHeader times) {
        List<YogaClass> items = new ArrayList<>();

        for (int i = 0; i < tableRow.getElements().size(); i++) {
            Row.Item rowItem = tableRow.getElements().get(i);
            if (rowItem == null) {
                continue;
            }
            LocalDate date = tableRow.date;
            LocalTime time = times.getTimes().get(i);
            if (date == null || time == null) {
                continue;
            }
            String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "-" + time.format(DateTimeFormatter.ofPattern("HH:mm"));

            try {
                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd-HH:mm", Locale.GERMANY);
                Date properDate = parser.parse(dateString);
                items.add(YogaClass.fromRowItem(properDate, rowItem));
            } catch (ParseException e) {

            }

        }
        return items;
    }

    /**  Public static builder  */
    public static YogaTable fromTable(Element table) {
        List<Element> trs = new ArrayList<Element>(table.getElementsByTag("tr"));
        TimesHeader times = trs.stream()
                .map(TimesHeader::fromTr)
                .findFirst()
                .orElse(null);
        List<Row> rows = trs.stream()
                .map(Row::fromTr)
                .collect(Collectors.toList());
        return new YogaTable(times, rows);
    }

    @NonNull
    @Override
    public String toString() {
        return "TABLE, rows: " + rows.size() + ", columns: " + rows.get(0).getElements().size();
    }


    // YogaTable.Row
    public static class Row {

        private LocalDate date;
        private List<Item> elements;

        private Row(LocalDate date, List<Item> elements) {
            this.date = date;
            this.elements = elements;
        }

        public List<Item> getElements() {
            return elements;
        }

        /**  Static builder method  */
        public static Row fromTr(Element row) {
            List<Element> items = new ArrayList<>(row.children());
            LocalDate parsedDate;
            try {
                String dateString = items.get(0).text();
                Matcher m = Pattern.compile("(\\d+\\.\\d+\\.)").matcher(dateString);
                if (m.find()) {
                    dateString = m.group(0) + LocalDate.now().getYear();
                } else {
                    return null;
                }
                //todo: parse date!
                SimpleDateFormat parser = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
                Date date = parser.parse(dateString);
                parsedDate = Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (DateTimeParseException | ParseException e) {
                parsedDate = LocalDate.MAX;
            }

            List<Item> yogaItems = new ArrayList<>();
            for (int i = 1; i < items.size(); i++) {
                yogaItems.add(Item.fromHtmlElement(items.get(i)));
            }

            return new Row(parsedDate, yogaItems);
        }

        @NonNull
        @Override
        public String toString() {
            return "[" + date + "]: " + elements;
        }

        // YogaTable.Row.Item
        public static class Item {
            private String instructor;
            private String name;
            private int freeSlots;
            private int substitutes;

            private Item(String instructor, String name, int freeSlots, int substitutes) {
                this.instructor = instructor;
                this.name = name;
                this.freeSlots = freeSlots;
                this.substitutes = substitutes;
            }

            /**
             * UGLY REGEX, BEWARE!
             * @return hopefully the right thing
             */
            static Item fromHtmlElement(Element element) {
                String instructor = element.getElementsByClass("motiv-kontra").text();
                String name = element.getElementsByClass("motiv-hlavni").text();

                Matcher substitutes = Pattern.compile("(Náhradník:\\s*\\d+)").matcher(element.text());
                Matcher free = Pattern.compile("(Volné:\\s*\\d+)").matcher(element.text());
                Pattern fullPatter = Pattern.compile(".*Obsazeno.*");

                boolean full = fullPatter.matcher(element.text()).matches();

                if (instructor != null && !instructor.isEmpty() && name != null && !name.isEmpty()) {

                    if (!full && free.find()) {
                        Matcher m = Pattern.compile("(\\d+)").matcher(free.group(0));
                        if (m.find()) {
                            int freeSlots = Integer.parseInt(m.group(0));
                            return new Item(instructor, name, freeSlots, 0);
                        }
                    } else if (!full && substitutes.find()) {
                        Matcher m = Pattern.compile("(\\d+)").matcher(substitutes.group(0));
                        if (m.find()) {
                            int substituteSlots = Integer.parseInt(m.group(0));
                            return new Item(instructor, name, 0, substituteSlots);
                        }
                    } else {
                        return new Item(instructor, name, 0, 0);
                    }
                }
                return null;
            }

            public String getInstructor() {
                return instructor;
            }

            public String getName() {
                return name;
            }

            public int getFreeSlots() {
                return freeSlots;
            }

            public int getSubstitutes() {
                return substitutes;
            }
        }
    }

    // Table.TimesHeader
    public static class TimesHeader {

        private final List<LocalTime> times;

        private TimesHeader(List<LocalTime> times) {
            this.times = times;
        }

        public List<LocalTime> getTimes() {
            return times;
        }


        /**  Static builder method  */
        public static TimesHeader fromTr(Element row) {
            List<LocalTime> times = row.children().stream()
                    .map(Element::text)
                    .map(str -> {
                        if (str.isEmpty()) {
                            return null;
                        }
                        try {
                            return LocalTime.parse(str);
                        } catch (DateTimeParseException e) {
                            return null;
                        }
                    })
                    .collect(Collectors.toList());
            if (times.stream().filter(Objects::isNull).count() > 1) {
                return null;
            } else {
                // first column is empty, remove it
                return new TimesHeader(times.stream().filter(Objects::nonNull).collect(Collectors.toList()));
            }

        }

        @NonNull
        @Override
        public String toString() {
            return times.toString();
        }
    }

}
