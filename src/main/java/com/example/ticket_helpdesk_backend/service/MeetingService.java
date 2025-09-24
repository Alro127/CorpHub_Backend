package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.AttendeeStatus;
import com.example.ticket_helpdesk_backend.dto.MeetingRequest;
import com.example.ticket_helpdesk_backend.dto.MeetingResponse;
import com.example.ticket_helpdesk_backend.entity.Attendee;
import com.example.ticket_helpdesk_backend.entity.Meeting;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.repository.AttendeeRepository;
import com.example.ticket_helpdesk_backend.repository.MeetingRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class MeetingService {

    @Autowired
    ModelMapper modelMapper;

    private final MeetingRepository meetingRepository;
    private final AttendeeRepository attendeeRepository;
    private final AccountService accountService;
    private final UserRepository userRepository;

    public MeetingService(MeetingRepository meetingRepository,
                          AttendeeRepository attendeeRepository, AccountService accountService, UserRepository userRepository) {
        this.meetingRepository = meetingRepository;
        this.attendeeRepository = attendeeRepository;
        this.accountService = accountService;
        this.userRepository = userRepository;
    }

    private List<MeetingResponse> mapToMeetingResponse(List<Meeting> meetings) {
        return meetings.stream()
                .map((meeting) -> {
                    MeetingResponse dto = modelMapper.map(meeting, MeetingResponse.class);

                    List<String> emails = meeting.getAttendees().stream()
                            .map(Attendee::getEmail)
                            .collect(Collectors.toList());

                    dto.setAttendeesEmails(emails);
                    return dto;

                })
                .collect(Collectors.toList());
    }

    /**
     * Tạo mới cuộc họp từ MeetingRequest:
     * - Lưu Meeting (title, description, location, onlineLink, start/end, organizer)
     * - Lưu danh sách Attendees (PENDING) gắn với Meeting
     */
    @Transactional
    public Meeting createMeeting(MeetingRequest req, String organizerEmail) {
        // Map request -> entity Meeting
        Meeting meeting = new Meeting();

        meeting.setTitle(req.getTitle());
        meeting.setDescription(req.getDescription());
        meeting.setLocation(req.getLocation());
        meeting.setOnlineLink(req.getOnlineLink());
        meeting.setStartTime(req.getStart());
        meeting.setEndTime(req.getEnd());
        meeting.setOrganizerEmail(organizerEmail);
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

    public List<MeetingResponse> getMeetings(UUID userId) {
        if (userId == null) {
            System.out.println("userId is null");
            return Collections.emptyList();
        }
        if (accountService.isAdmin(userId)) {
            System.out.println("account is admin");
            return getAllMeetings();
        }
        if (accountService.isManager(userId)) {
            System.out.println("account is manager");
            User manager = userRepository.findById(userId).orElse(null);
            if (manager == null) {
                return Collections.emptyList();
            }
            UUID departmentId = manager.getDepartment().getId();
            List<User> employees = userRepository.findByDepartment_Id(departmentId);
            List<String> emails = employees.stream()
                    .map(User::getEmail)
                    .toList();
            return getAllMeetingByEmails(emails);
        }
        if (accountService.isUser(userId)) {
            System.out.println("account is user");
            User employee = userRepository.findById(userId).orElse(null);
            if (employee == null) {
                return Collections.emptyList();
            }
            return getAllMeetingsByEmail(employee.getEmail());
        }
        System.out.println("nothing to show");
        return Collections.emptyList();
    }

    // Lấy danh sách tất cả cuộc họp
    public List<MeetingResponse> getAllMeetings() {
        List<Meeting> meetings = meetingRepository.findAll();
        return mapToMeetingResponse(meetings);
    }

    public List<MeetingResponse> getAllMeetingsByEmail(String email) {
        List<Meeting> meetings = meetingRepository.findAllByOrganizerEmail(email);
        List<Attendee> attendees = attendeeRepository.findByEmail(email);
        attendees.forEach(attendee -> {
            meetings.add(attendee.getMeeting());
        });
        return mapToMeetingResponse(meetings);
    }

    public List<MeetingResponse> getAllMeetingByEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Lấy meetings mà người tổ chức nằm trong danh sách email
        List<Meeting> organizerMeetings = meetingRepository.findAllByOrganizerEmailIn(emails);

        // 2. Lấy tất cả attendees thuộc danh sách email (tránh query từng email)
        List<Attendee> attendees = attendeeRepository.findByEmailIn(emails);
        List<Meeting> attendeeMeetings = attendees.stream()
                .map(Attendee::getMeeting)
                .toList();

        // 3. Gộp 2 danh sách và loại bỏ trùng bằng Set
        Set<UUID> uniqueMeetingIds = new HashSet<>();
        List<Meeting> combinedMeetings = Stream.concat(organizerMeetings.stream(), attendeeMeetings.stream())
                .filter(meeting -> uniqueMeetingIds.add(meeting.getId())) // chỉ giữ meeting chưa xuất hiện
                .toList();

        // 4. Map sang response
        return mapToMeetingResponse(combinedMeetings);
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
