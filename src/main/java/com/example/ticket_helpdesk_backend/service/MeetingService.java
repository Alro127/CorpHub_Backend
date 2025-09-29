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
    private final UserService userService;
    private final UserRepository userRepository;

    public MeetingService(MeetingRepository meetingRepository,
                          AttendeeRepository attendeeRepository, UserService userService, UserRepository userRepository) {
        this.meetingRepository = meetingRepository;
        this.attendeeRepository = attendeeRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    private MeetingResponse mapToMeetingResponse(Meeting meeting) {
        if (meeting == null) return null;

        MeetingResponse dto = modelMapper.map(meeting, MeetingResponse.class);

        List<String> emails = (meeting.getAttendees() == null)
                ? Collections.emptyList()
                : meeting.getAttendees().stream()
                .map(Attendee::getEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        dto.setAttendeesEmails(emails);
        return dto;
    }

    /**
     * Tạo mới cuộc họp từ MeetingRequest:
     * - Lưu Meeting (title, description, location, onlineLink, start/end, organizer)
     * - Lưu danh sách Attendees (PENDING) gắn với Meeting
     */
    @Transactional
    public MeetingResponse saveMeeting(MeetingRequest req, String organizerEmail) {
        // 1. Lấy hoặc tạo meeting
        Meeting meeting;
        if (req.getId() == null) {
            meeting = new Meeting();
            meeting.setCreatedAt(LocalDateTime.now());
        } else {
            meeting = meetingRepository.findById(req.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy meeting"));
        }

        // 2. Cập nhật các trường
        meeting.setTitle(req.getTitle());
        meeting.setSubject(req.getSubject());
        meeting.setDescription(req.getDescription());
        meeting.setLocation(req.getLocation());
        meeting.setOnlineLink(req.getOnlineLink());
        meeting.setStartTime(req.getStart());
        meeting.setEndTime(req.getEnd());
        meeting.setOrganizerEmail(organizerEmail);
        meeting.setUpdatedAt(LocalDateTime.now());

        // 3. Lưu meeting trước để có ID
        Meeting saved = meetingRepository.save(meeting);

        // 4. Đồng bộ attendees (nếu có truyền danh sách)
        List<String> newEmails = req.getTo() != null ? req.getTo() : Collections.emptyList();

        // Lấy danh sách cũ
        List<Attendee> oldAttendees = attendeeRepository.findByMeeting(saved);
        List<String> oldEmails = oldAttendees.stream()
                .map(Attendee::getEmail)
                .toList();

        // Tìm người cần xóa
        List<Attendee> toRemove = oldAttendees.stream()
                .filter(a -> !newEmails.contains(a.getEmail()))
                .toList();

        // Tìm người cần thêm
        List<String> toAdd = newEmails.stream()
                .filter(email -> !oldEmails.contains(email))
                .toList();

        // Xóa những người không còn trong danh sách
        if (!toRemove.isEmpty()) {
            attendeeRepository.deleteAll(toRemove);
        }

        // Thêm người mới
        if (!toAdd.isEmpty()) {
            List<Attendee> newAttendees = new ArrayList<>();
            for (String email : toAdd) {
                Attendee a = new Attendee();
                a.setEmail(email);
                a.setStatus(AttendeeStatus.PENDING);
                a.setMeeting(saved);
                newAttendees.add(a);
            }
            attendeeRepository.saveAll(newAttendees);
        }

        // 5. Trả về response
        return mapToMeetingResponse(saved);
    }


    public List<MeetingResponse> getMeetings(UUID userId) {
        if (userId == null) {
            System.out.println("userId is null");
            return Collections.emptyList();
        }
        if (userService.isAdmin(userId)) {
            System.out.println("account is admin");
            return getAllMeetings();
        }
        if (userService.isManager(userId)) {
            System.out.println("account is manager");
            User manager = userRepository.findById(userId).orElse(null);
            if (manager == null) {
                return Collections.emptyList();
            }
            UUID departmentId = manager.getEmployeeProfile().getDepartment().getId();
            List<User> employees = userRepository.findByEmployeeProfile_Department_Id(departmentId);
            List<String> emails = employees.stream()
                    .map(u -> u.getEmployeeProfile().getPersonalEmail())
                    .filter(Objects::nonNull) // tránh null email
                    .toList();
            return getAllMeetingByEmails(emails);
        }
        if (userService.isUser(userId)) {
            System.out.println("account is user");
            User employee = userRepository.findById(userId).orElse(null);
            if (employee == null) {
                return Collections.emptyList();
            }
            return getAllMeetingsByEmail(employee.getEmployeeProfile().getPersonalEmail());
        }
        System.out.println("nothing to show");
        return Collections.emptyList();
    }

    // Lấy danh sách tất cả cuộc họp
    public List<MeetingResponse> getAllMeetings() {
        List<Meeting> meetings = meetingRepository.findAll();
        return meetings.stream().map(this::mapToMeetingResponse).collect(Collectors.toList());
    }

    public List<MeetingResponse> getAllMeetingsByEmail(String email) {
        List<Meeting> meetings = meetingRepository.findAllByOrganizerEmail(email);
        List<Attendee> attendees = attendeeRepository.findByEmail(email);
        attendees.forEach(attendee -> {
            meetings.add(attendee.getMeeting());
        });
        return meetings.stream().map(this::mapToMeetingResponse).collect(Collectors.toList());
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
        return combinedMeetings.stream().map(this::mapToMeetingResponse).collect(Collectors.toList());
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

    @Transactional
    public boolean deleteMeeting(UUID meetingId) {
        if (meetingRepository.existsById(meetingId)) {
            meetingRepository.deleteById(meetingId); // FK ON DELETE CASCADE hoặc orphanRemoval sẽ xử lý attendees
            return true;
        }
        return false;
    }
}
