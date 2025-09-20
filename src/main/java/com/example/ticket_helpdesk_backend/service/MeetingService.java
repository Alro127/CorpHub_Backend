package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.AttendeeStatus;
import com.example.ticket_helpdesk_backend.dto.MeetingRequest;
import com.example.ticket_helpdesk_backend.entity.Attendee;
import com.example.ticket_helpdesk_backend.entity.Meeting;
import com.example.ticket_helpdesk_backend.repository.AttendeeRepository;
import com.example.ticket_helpdesk_backend.repository.MeetingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final AttendeeRepository attendeeRepository;

    public MeetingService(MeetingRepository meetingRepository,
                          AttendeeRepository attendeeRepository) {
        this.meetingRepository = meetingRepository;
        this.attendeeRepository = attendeeRepository;
    }

    /**
     * Tạo mới cuộc họp từ MeetingRequest:
     * - Lưu Meeting (title, description, location, onlineLink, start/end, organizer)
     * - Lưu danh sách Attendees (PENDING) gắn với Meeting
     */
    @Transactional
    public Meeting createMeeting(MeetingRequest req) {
        // Map request -> entity Meeting
        Meeting meeting = new Meeting();
        meeting.setTitle(req.getTitle());
        meeting.setDescription(req.getDescription());
        meeting.setLocation(req.getLocation());
        meeting.setOnlineLink(req.getOnlineLink());
        meeting.setStartTime(req.getStart());
        meeting.setEndTime(req.getEnd());
        meeting.setOrganizerEmail(req.getOrganizer());
        meeting.setCreatedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());

        // Lưu meeting trước để có meetingId
        Meeting saved = meetingRepository.save(meeting);

        // Lưu attendees
        if (req.getTo() != null && !req.getTo().isEmpty()) {
            List<Attendee> list = new ArrayList<>();
            for (String email : req.getTo()) {
                Attendee a = new Attendee();
                a.setEmail(email);
                a.setStatus(AttendeeStatus.PENDING); // mặc định pending
                a.setMeeting(saved);
                list.add(a);
            }
            attendeeRepository.saveAll(list);
        }

        return saved;
    }

    // Lấy danh sách tất cả cuộc họp
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }

    // Lấy chi tiết 1 cuộc họp
    public Meeting getMeetingOrThrow(UUID meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("Meeting not found: " + meetingId));
    }

    // Lấy attendees theo meeting
    public List<Attendee> getAttendeesByMeeting(UUID meetingId) {
        return attendeeRepository.findByMeeting_Id(meetingId);
    }

    /**
     * Cập nhật thông tin meeting (KHÔNG đụng tới attendees ở đây).
     */
    @Transactional
    public Meeting updateMeeting(UUID meetingId, MeetingRequest req) {
        Meeting meeting = getMeetingOrThrow(meetingId);

        // Update các trường; chỉ update nếu request có (tuỳ nhu cầu, ở đây update thẳng)
        meeting.setTitle(req.getTitle());
        meeting.setDescription(req.getDescription());
        meeting.setLocation(req.getLocation());
        meeting.setOnlineLink(req.getOnlineLink());
        meeting.setStartTime(req.getStart());
        meeting.setEndTime(req.getEnd());
        meeting.setOrganizerEmail(req.getOrganizer());
        meeting.setUpdatedAt(LocalDateTime.now());

        return meetingRepository.save(meeting);
    }

    @Transactional
    public boolean deleteMeeting(UUID meetingId) {
        if (meetingRepository.existsById(meetingId)) {
            meetingRepository.deleteById(meetingId); // FK ON DELETE CASCADE hoặc orphanRemoval sẽ xử lý attendees
            return true;
        }
        return false;
    }
}
