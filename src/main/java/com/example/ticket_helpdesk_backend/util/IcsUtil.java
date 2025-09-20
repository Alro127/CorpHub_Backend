package com.example.ticket_helpdesk_backend.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class IcsUtil {
    public static String createMeetingIcs(
            String uid,
            String title,
            String description,
            String location,
            String onlineLink,
            ZonedDateTime start,
            ZonedDateTime end,
            String organizer,
            String[] attendees
    ) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String lineBreak = "\r\n";

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR").append(lineBreak)
                .append("PRODID:-//MyApp//Meeting Scheduler//EN").append(lineBreak)
                .append("VERSION:2.0").append(lineBreak)
                .append("CALSCALE:GREGORIAN").append(lineBreak)
                .append("METHOD:REQUEST").append(lineBreak)
                .append("BEGIN:VEVENT").append(lineBreak)
                .append("UID:").append(uid).append(lineBreak)
                .append("DTSTAMP:").append(ZonedDateTime.now(ZoneOffset.UTC).format(fmt)).append(lineBreak)
                .append("DTSTART:").append(start.withZoneSameInstant(ZoneOffset.UTC).format(fmt)).append(lineBreak)
                .append("DTEND:").append(end.withZoneSameInstant(ZoneOffset.UTC).format(fmt)).append(lineBreak)
                .append("SUMMARY:").append(title).append(lineBreak)
                .append("DESCRIPTION:").append(description).append(lineBreak);

        // Nếu có địa điểm offline
        if (location != null && !location.isBlank()) {
            sb.append("LOCATION:").append(location).append(lineBreak);
        }

        // Nếu có link online
        if (onlineLink != null && !onlineLink.isBlank()) {
            sb.append("URL:").append(onlineLink).append(lineBreak);
            sb.append("DESCRIPTION:").append(description).append("\\nOnline: ").append(onlineLink).append(lineBreak);
        }

        sb.append("ORGANIZER;CN=Organizer:MAILTO:").append(organizer).append(lineBreak);

        for (String att : attendees) {
            sb.append("ATTENDEE;CN=").append(att)
                    .append(";RSVP=TRUE:MAILTO:").append(att).append(lineBreak);
        }

        sb.append("END:VEVENT").append(lineBreak)
                .append("END:VCALENDAR");

        return sb.toString();
    }
}
