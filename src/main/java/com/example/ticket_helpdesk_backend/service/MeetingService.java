package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.AttendeeStatus;
import com.example.ticket_helpdesk_backend.dto.AttendeeResponse;
import com.example.ticket_helpdesk_backend.dto.MeetingRequest;
import com.example.ticket_helpdesk_backend.dto.MeetingResponse;
import com.example.ticket_helpdesk_backend.dto.RoomRequirementDto;
import com.example.ticket_helpdesk_backend.entity.*;
import com.example.ticket_helpdesk_backend.exception.AuthException;
import com.example.ticket_helpdesk_backend.repository.*;

import com.example.ticket_helpdesk_backend.specification.MeetingSpecifications;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.ticket_helpdesk_backend.specification.MeetingSpecifications.*;

@Service
public class MeetingService {

    @Autowired
    ModelMapper modelMapper;

    private final MeetingRepository meetingRepository;
    private final AttendeeRepository attendeeRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoomRequirementService roomRequirementService;
    @Autowired
    private RoomRequirementRepository roomRequirementRepository;


    public MeetingService(MeetingRepository meetingRepository,
                          AttendeeRepository attendeeRepository, UserService userService, UserRepository userRepository, RoomRequirementService roomRequirementService) {
        this.meetingRepository = meetingRepository;
        this.attendeeRepository = attendeeRepository;
        this.userService = userService;
        this.userRepository = userRepository;

        this.roomRequirementService = roomRequirementService;
    }

    private MeetingResponse mapToMeetingResponse(Meeting meeting) {
        if (meeting == null) return null;

        // Dùng ModelMapper để map các trường cơ bản
        MeetingResponse dto = modelMapper.map(meeting, MeetingResponse.class);

        List<AttendeeResponse> attendees = (meeting.getAttendees() == null)
                ? Collections.emptyList()
                : meeting.getAttendees().stream()
                .filter(Objects::nonNull)
                .map(attendee -> {
                    AttendeeResponse ar = new AttendeeResponse();
                    ar.setEmail(attendee.getEmail());
                    ar.setStatus(attendee.getStatus());
                    return ar;
                })
                .collect(Collectors.toList());

        RoomRequirementDto roomRequirementDto = RoomRequirementDto.toRoomRequirementDto(roomRequirementService.getRoomRequirementMeetingId(meeting.getId()));

        dto.setRoomRequirement(roomRequirementDto);

        dto.setAttendees(attendees);
        return dto;
    }


    private List<MeetingResponse> mapMeetingsToResponse(List<Meeting> meetings) {
        return meetings.stream()
                .map(this::mapToMeetingResponse)
                .toList();
    }

    @Transactional
    public MeetingResponse saveMeeting(MeetingRequest req, String organizerEmail) {
        // 1. Lấy hoặc tạo meeting
        Meeting meeting;
        RoomRequirement roomRequirement;
        if (req.getId() == null) {
            meeting = new Meeting();
            meeting.setCreatedAt(LocalDateTime.now());
        } else {
            meeting = meetingRepository.findById(req.getId())
                    .orElseThrow(() -> new RuntimeException("Meeting not found"));

            if (meeting.isReady())
                throw new RuntimeException("Meeting is ready, cannot update meeting");

            if (!meeting.getOrganizerEmail().equals(organizerEmail)) {
                throw new AuthException("User does not have permission to update meeting");
            }
        }

        // 2. Cập nhật các trường
        meeting.setTitle(req.getTitle());
        meeting.setSubject(req.getSubject());
        meeting.setDescription(req.getDescription());
        meeting.setMeetingRoom(req.isMeetingRoom());
        meeting.setLocation(req.getLocation());
        meeting.setOnlineLink(req.getOnlineLink());
        meeting.setStartTime(req.getStart());
        meeting.setEndTime(req.getEnd());
        meeting.setOrganizerEmail(organizerEmail);
        meeting.setUpdatedAt(LocalDateTime.now());
        meeting.setReady(false);

        // 3. Lưu meeting trước để có ID
        Meeting saved = meetingRepository.save(meeting);

        if (req.getRoomRequirement() != null)
            roomRequirementService.saveRoomRequirement(req.getRoomRequirement(), saved);
        else {
            System.out.println("Hello");
            roomRequirementService.deleteRoomRequirementByMeetingId(saved.getId());
        }
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
            attendeeRepository.flush();
            System.out.println("đã xóa" + toRemove.stream().map(Attendee::getEmail).toList());
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


    public List<MeetingResponse> getMeetings(UUID userId,
                                             LocalDateTime startTime,
                                             LocalDateTime endTime,
                                             List<String> emails) {
        if (userId == null) return Collections.emptyList();

        if (userService.isAdmin(userId)) {
            return getAllMeetings(startTime, endTime, emails);
        }

        if (userService.isManager(userId)) {
            User manager = userRepository.findById(userId).orElse(null);
            if (manager == null) return Collections.emptyList();

            UUID departmentId = manager.getEmployeeProfile().getDepartment().getId();
            if (emails == null || emails.isEmpty()) {
                emails = userRepository.findByEmployeeProfile_Department_Id(departmentId)
                        .stream()
                        .map(User::getUsername)
                        .toList();
            }
            return getAllMeetingByEmails(startTime, endTime, emails);
        }

        if (userService.isUser(userId)) {
            User employee = userRepository.findById(userId).orElse(null);
            if (employee == null) return Collections.emptyList();
            return getAllMeetingsByEmail(employee.getUsername(), startTime, endTime);
        }

        return Collections.emptyList();
    }

    public List<MeetingResponse> getAllMeetings(LocalDateTime startTime,
                                                LocalDateTime endTime,
                                                List<String> emails) {
        Specification<Meeting> spec = startAfter(startTime)
                .and(endBefore(endTime))
                .and(hasEmails(emails));

        return mapMeetingsToResponse(meetingRepository.findAll(spec, Sort.by("startTime")));
    }

    public List<MeetingResponse> getAllMeetingsByEmail(String email,
                                                       LocalDateTime startTime,
                                                       LocalDateTime endTime) {
        Specification<Meeting> spec = organizedBy(email)
                .or(joinedBy(email))
                .and(startAfter(startTime))
                .and(endBefore(endTime))
                .and(isReady());

        return mapMeetingsToResponse(meetingRepository.findAll(spec, Sort.by("startTime")));
    }

    public List<MeetingResponse> getAllMeetingByEmails(LocalDateTime startTime,
                                                       LocalDateTime endTime,
                                                       List<String> emails) {
        if (emails == null || emails.isEmpty()) return Collections.emptyList();

        Specification<Meeting> spec = organizedOrJoinedBy(emails)
                .and(startAfter(startTime))
                .and(endBefore(endTime));

        return mapMeetingsToResponse(meetingRepository.findAll(spec, Sort.by("startTime")));
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

    public MeetingResponse setStatus(UUID id, boolean isAccepted, User user) {
        Attendee attendee = attendeeRepository.findByMeeting_IdAndEmail(id, user.getUsername());
        if (attendee == null)
            throw new IllegalArgumentException("Attendee not found: " + id);
        attendee.setStatus(isAccepted ? AttendeeStatus.ACCEPTED : AttendeeStatus.REJECTED);

        return mapToMeetingResponse(attendeeRepository.save(attendee).getMeeting());
    }

    public Boolean confirmMeeting(UUID meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId).orElse(null);
        if (meeting == null) return false;
        meeting.setReady(true);
        meetingRepository.save(meeting);
        return true;
    }
}
