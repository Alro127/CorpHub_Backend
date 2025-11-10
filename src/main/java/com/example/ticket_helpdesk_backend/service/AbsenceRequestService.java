package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.AbsenceRequestStatus;
import com.example.ticket_helpdesk_backend.dto.AbsenceReqRequest;
import com.example.ticket_helpdesk_backend.dto.AbsenceReqResponse;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.entity.AbsenceRequest;
import com.example.ticket_helpdesk_backend.entity.AbsenceType;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.AbsenceRequestRepository;
import com.example.ticket_helpdesk_backend.repository.AbsenceTypeRepository;
import jakarta.security.auth.message.AuthException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.example.ticket_helpdesk_backend.specification.AbsenceRequestSpecifications.hasUserId;

@AllArgsConstructor
@Service
public class AbsenceRequestService {

    private final UserService userService;
    private final AbsenceTypeRepository absenceTypeRepository;
    private AbsenceRequestRepository absenceRequestRepository;
    private final ModelMapper modelMapper;

    public AbsenceReqResponse mapToDto(AbsenceRequest absenceRequest) {
        AbsenceReqResponse absenceReqResponse = modelMapper.map(absenceRequest, AbsenceReqResponse.class);
        absenceReqResponse.setUser(UserDto.toUserDto(absenceRequest.getUser()));
        return absenceReqResponse;
    }

    public Page<AbsenceReqResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return absenceRequestRepository.findAll(pageable).map(this::mapToDto);
    }

    public Page<AbsenceReqResponse> getByUser(UUID userId, int page, int size) {
        Specification<AbsenceRequest> spec = Specification.where(hasUserId(userId));
        Pageable pageable = PageRequest.of(page, size);
        return absenceRequestRepository.findAll(spec, pageable).map(this::mapToDto);
    }

    @Transactional
    public AbsenceReqResponse create(UUID userId, AbsenceReqRequest request) throws ResourceNotFoundException {
        AbsenceRequest absenceRequest = modelMapper.map(request, AbsenceRequest.class);
        User user = userService.getUserById(userId);
        AbsenceType absenceType = absenceTypeRepository.findById(request.getAbsenceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại nghỉ phép"));

        // ✅ Validate logic ngày
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }

        // ✅ Tính số ngày nghỉ
        BigDecimal duration = BigDecimal.valueOf(
                ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1
        );
        absenceRequest.setDurationDays(duration);

        absenceRequest.setUser(user);
        absenceRequest.setAbsenceType(absenceType);
        absenceRequest.setStatus(AbsenceRequestStatus.PENDING);
        absenceRequest.setCreatedAt(LocalDateTime.now());
        absenceRequest.setUpdatedAt(LocalDateTime.now());

        AbsenceRequest savedAbsenceRequest = absenceRequestRepository.save(absenceRequest);
        return this.mapToDto(savedAbsenceRequest);
    }

    public AbsenceReqResponse getById(UUID id) {
        return absenceRequestRepository.findById(id).map(this::mapToDto).orElse(null);
    }

    @Transactional
    public void deleteByUser(UUID id, UUID userId) throws ResourceNotFoundException, AuthException {
        AbsenceRequest absenceRequest = absenceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn nghỉ phép"));
        if (!absenceRequest.getUser().getId().equals(userId)) {
            throw new AuthException("Bạn không có quyền xóa request này");
        }

        if (!AbsenceRequestStatus.PENDING.equals(absenceRequest.getStatus())) {
            throw new IllegalStateException("Chỉ có thể xóa đơn đang chờ duyệt");
        }

        absenceRequestRepository.delete(absenceRequest);
    }

    @Transactional
    public AbsenceReqResponse update(UUID userId, UUID id, AbsenceReqRequest request)
            throws ResourceNotFoundException, AuthException {

        AbsenceRequest absenceRequest = absenceRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn nghỉ phép"));

        if (!absenceRequest.getUser().getId().equals(userId)) {
            throw new AuthException("Bạn không có quyền sửa đơn nghỉ phép này");
        }

        if (!AbsenceRequestStatus.PENDING.equals(absenceRequest.getStatus())) {
            throw new IllegalStateException("Chỉ có thể chỉnh sửa đơn nghỉ phép đang chờ duyệt");
        }

        AbsenceType absenceType = absenceTypeRepository.findById(request.getAbsenceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại nghỉ phép"));

        absenceRequest.setAbsenceType(absenceType);
        absenceRequest.setStartDate(request.getStartDate());
        absenceRequest.setEndDate(request.getEndDate());
        absenceRequest.setReason(request.getReason());

        BigDecimal duration = BigDecimal.valueOf(
                ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1
        );
        absenceRequest.setDurationDays(duration);

        absenceRequest.setUpdatedAt(LocalDateTime.now());

        AbsenceRequest saved = absenceRequestRepository.save(absenceRequest);
        return mapToDto(saved);
    }

}
