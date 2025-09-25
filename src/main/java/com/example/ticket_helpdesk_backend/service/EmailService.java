package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.MeetingRequest;
import com.example.ticket_helpdesk_backend.util.IcsUtil;
import jakarta.activation.DataSource;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.UUID;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSimpleMail(String to, String subject, String text) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        mailSender.send(message);
    }

    public void sendMeetingInvite(MeetingRequest req, String organizerEmail) throws Exception {
        String uid = UUID.randomUUID().toString();

        String[] attendees = req.getTo().toArray(new String[0]);

        String icsContent = IcsUtil.createMeetingIcs(
                uid,
                req.getTitle(),
                req.getDescription(),
                req.getLocation(),
                req.getOnlineLink(),
                req.getStart().atZone(ZoneId.systemDefault()),
                req.getEnd().atZone(ZoneId.systemDefault()),
                organizerEmail,
                attendees
        );

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(attendees);
        helper.setSubject(req.getSubject());
        helper.setText("Bạn được mời tham dự cuộc họp. Vui lòng xác nhận trong file đính kèm.", true);

        DataSource dataSource = new ByteArrayDataSource(icsContent, "text/calendar; charset=UTF-8; method=REQUEST");
        helper.addAttachment("invite.ics", dataSource);

        mailSender.send(message);
    }
}
