package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.AbsenceRequestStatus;
import com.example.ticket_helpdesk_backend.consts.WorkflowActionType;
import com.example.ticket_helpdesk_backend.consts.WorkflowStatus;
import com.example.ticket_helpdesk_backend.context.AbsenceWorkflowContextProvider;
import com.example.ticket_helpdesk_backend.dto.AbsenceReqRequest;
import com.example.ticket_helpdesk_backend.dto.AbsenceReqResponse;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.dto.WorkflowStepActionDto;
import com.example.ticket_helpdesk_backend.entity.*;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.AbsenceBalanceRepository;
import com.example.ticket_helpdesk_backend.repository.AbsenceRequestRepository;
import com.example.ticket_helpdesk_backend.repository.AbsenceTypeRepository;
import com.example.ticket_helpdesk_backend.repository.WorkflowInstanceRepository;
import com.example.ticket_helpdesk_backend.specification.AbsenceBalanceSpecifications;
import com.example.ticket_helpdesk_backend.specification.WorkflowSpecifications;
import jakarta.security.auth.message.AuthException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.ticket_helpdesk_backend.specification.AbsenceRequestSpecifications.hasUserId;
import static com.example.ticket_helpdesk_backend.specification.WorkflowSpecifications.*;

@AllArgsConstructor
@Service
public class AbsenceRequestService {

    private final UserService userService;
    private final AbsenceTypeRepository absenceTypeRepository;
    private final WorkflowEngineService workflowEngineService;
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final AbsenceBalanceRepository absenceBalanceRepository;
    private AbsenceRequestRepository absenceRequestRepository;
    private final AbsenceAttachmentService absenceAttachmentService;
    private final ModelMapper modelMapper;
    private final AbsenceWorkflowContextProvider absenceContext;

    public AbsenceReqResponse mapToDto(AbsenceRequest request) {
        AbsenceReqResponse response = modelMapper.map(request, AbsenceReqResponse.class);
        response.setUser(UserDto.toUserDto(request.getUser()));

//        response.setAttachmentUrl(
//                absenceAttachmentService.generatePresignedUrl(request.getAttachmentUrl())
//        );

        Specification<WorkflowInstance> spec = Specification
                .where(byEntityId(request.getId()))
                .and(instanceByTargetEntity(absenceContext.getTargetEntity()));

        WorkflowInstance instance = workflowInstanceRepository.findOne(spec).orElse(null);

        if (instance != null) {
            response.setWorkflowInstanceId(instance.getId());
            response.setWorkflowStatus(instance.getStatus());
            response.setCurrentStepOrder(instance.getCurrentStepOrder());
        }

        return response;
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

    public AbsenceRequest getEntityById(UUID id) {
        return absenceRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Absence request not found"));
    }

    public void updateAttachment(UUID id, String newObjectKey) {
        AbsenceRequest request = getEntityById(id);
        request.setAttachmentUrl(newObjectKey);
        absenceRequestRepository.save(request);
    }

//    @Transactional
//    public AbsenceReqResponse create(UUID userId, AbsenceReqRequest request) throws ResourceNotFoundException {
//        AbsenceRequest absenceRequest = modelMapper.map(request, AbsenceRequest.class);
//        User user = userService.getUserById(userId);
//        AbsenceType absenceType = absenceTypeRepository.findById(request.getAbsenceTypeId())
//                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại nghỉ phép"));
//
//        // ✅ Validate logic ngày
//        if (request.getEndDate().isBefore(request.getStartDate())) {
//            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
//        }
//
//        // ✅ Tính số ngày nghỉ
//        BigDecimal duration = BigDecimal.valueOf(
//                ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1
//        );
//        absenceRequest.setDurationDays(duration);
//
//        absenceRequest.setUser(user);
//        absenceRequest.setAbsenceType(absenceType);
//        absenceRequest.setStatus(AbsenceRequestStatus.PENDING);
//        absenceRequest.setCreatedAt(LocalDateTime.now());
//        absenceRequest.setUpdatedAt(LocalDateTime.now());
//
//        try {
//            AbsenceRequest saved = absenceRequestRepository.save(absenceRequest);
//
//            WorkflowTemplate template = absenceType.getWorkflowTemplate();
//
//            if (template != null) {
//                workflowEngineService.startWorkflow(
//                        absenceContext.getTargetEntity(),
//                        template.getName(),
//                        saved.getId(),
//                        userId
//                );
//            }
//
//            return this.mapToDto(saved);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            // ÉP rollback của transaction
//            throw new RuntimeException("Không thể tạo quy trình phê duyệt. Vui lòng thử lại.");
//        }
//    }

    @Transactional
    public AbsenceReqResponse create(UUID userId, AbsenceReqRequest request) throws ResourceNotFoundException {

        User user = userService.getUserById(userId);

        AbsenceType absenceType = absenceTypeRepository.findById(request.getAbsenceTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy loại nghỉ phép"));

        // 🔥 1. Kiểm tra nếu loại nghỉ phép yêu cầu minh chứng
        if (Boolean.TRUE.equals(absenceType.getRequireProof())) {
            if (request.getAttachmentUrl() == null || request.getAttachmentUrl().isBlank()) {
                throw new IllegalArgumentException("Loại nghỉ phép này yêu cầu phải có file minh chứng.");
            }
        }

        // 🔥 2. Validate ngày
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }

        // 🔥 3. Map entity
        AbsenceRequest absenceRequest = new AbsenceRequest();
        absenceRequest.setUser(user);
        absenceRequest.setAbsenceType(absenceType);
        absenceRequest.setStartDate(request.getStartDate());
        absenceRequest.setEndDate(request.getEndDate());
        absenceRequest.setReason(request.getReason());
        absenceRequest.setAttachmentUrl(request.getAttachmentUrl());
        absenceRequest.setStatus(AbsenceRequestStatus.PENDING);
        absenceRequest.setCreatedAt(LocalDateTime.now());
        absenceRequest.setUpdatedAt(LocalDateTime.now());

        // 🔥 4. Tính số ngày
        BigDecimal duration = BigDecimal.valueOf(
                ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1
        );
        absenceRequest.setDurationDays(duration);

        // 🔥 5. Lưu DB + tạo workflow
        try {
            AbsenceRequest saved = absenceRequestRepository.save(absenceRequest);

            WorkflowTemplate template = absenceType.getWorkflowTemplate();

            if (template != null) {
                workflowEngineService.startWorkflow(
                        absenceContext.getTargetEntity(),
                        template.getName(),
                        saved.getId(),
                        userId
                );
            }

            return this.mapToDto(saved);

        } catch (Exception ex) {
            ex.printStackTrace();
            // ÉP rollback của transaction
            throw new RuntimeException("Không thể tạo quy trình phê duyệt. Vui lòng thử lại.");
        }
    }


    public AbsenceReqResponse getById(UUID id) {
        return absenceRequestRepository.findById(id).map(this::mapToDto).orElse(null);
    }

    public Page<AbsenceReqResponse> getPendingForApprover(UUID userId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        // Lấy instance mà user là người duyệt hiện tại
        Page<WorkflowInstance> instances = workflowInstanceRepository.findAll(
                Specification.where(WorkflowSpecifications.byCurrentApprover(userId))
                        .and(WorkflowSpecifications.instanceByTargetEntity("ABSENCE_REQUEST")),
                pageable
        );


        List<UUID> requestIds = instances.getContent().stream()
                .map(WorkflowInstance::getEntityId)
                .toList();

        List<AbsenceRequest> reqs = absenceRequestRepository.findAllById(requestIds);

        List<AbsenceReqResponse> dtos = reqs.stream()
                .map(this::mapToDto)
                .toList();

        return new PageImpl<>(dtos, pageable, instances.getTotalElements());
    }

    @Transactional
    public Page<AbsenceReqResponse> getAllApprovals(UUID userId, int page, int size, WorkflowActionType action) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<WorkflowInstance> instancePage =
                workflowInstanceRepository.findAll(
                        WorkflowSpecifications.byActionOfUser(action, userId),
                        pageable
                );

        return instancePage.map(instance -> {

            // Chỉ lấy Absence Request
            if (!"ABSENCE_REQUEST".equals(instance.getTemplate().getTargetEntity())) {
                return null;
            }

            AbsenceRequest req = absenceRequestRepository.findById(instance.getEntityId())
                    .orElse(null);

            if (req == null) return null;

            AbsenceReqResponse dto = mapToDto(req);

            // status workflow
            dto.setWorkflowStatus(instance.getStatus());
            dto.setCurrentStepOrder(instance.getCurrentStepOrder());
            dto.setCurrentApproverId(instance.getCurrentApproverId());

            // lịch sử duyệt
            List<WorkflowStepActionDto> actions =
                    WorkflowStepActionDto.toDtoList(instance.getActions());
            dto.setWorkflowActions(actions);

            return dto;
        });
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

        absenceAttachmentService.deleteAttachment(id);
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

        // Nếu có file mới → xóa file cũ + ghi đè
        if (request.getAttachmentUrl() != null && !request.getAttachmentUrl().equals(absenceRequest.getAttachmentUrl())) {
            absenceRequest.setAttachmentUrl(request.getAttachmentUrl());
        }

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

    @Transactional
    public AbsenceReqResponse approveOrReject(UUID instanceId,
                                              UUID actorId,
                                              boolean approve,
                                              String comment) {

        workflowEngineService.handleAction(
                instanceId,
                actorId,
                approve ? WorkflowActionType.APPROVE : WorkflowActionType.REJECT,
                comment
        );

        // Lấy lại request sau khi workflow cập nhật trạng thái
        WorkflowInstance instance = workflowInstanceRepository.findById(instanceId).orElseThrow();

        AbsenceRequest request = absenceRequestRepository.findById(instance.getEntityId())
                .orElseThrow();

        // Cập nhật trạng thái đơn theo workflow
        if (instance.getStatus() == WorkflowStatus.APPROVED) {
            request.setStatus(AbsenceRequestStatus.APPROVED);
            if (request.getAbsenceType().getAffectQuota())
            {
                UUID userId = request.getUser().getId();
                UUID typeId = request.getAbsenceType().getId();
                Integer year = request.getCreatedAt().getYear();

                AbsenceBalance balance = absenceBalanceRepository.findOne(
                        Specification.where(AbsenceBalanceSpecifications.hasUserId(userId))
                                .and(AbsenceBalanceSpecifications.hasAbsenceTypeId(typeId))
                                .and(AbsenceBalanceSpecifications.hasYear(year))
                ).orElseThrow(() -> new RuntimeException(
                        "AbsenceBalance not found for user=" + userId +
                                " type=" + typeId +
                                " year=" + year
                ));

                balance.setUsedDays(balance.getUsedDays().add(request.getDurationDays()));

                absenceBalanceRepository.save(balance);
            }
        }
        if (instance.getStatus() == WorkflowStatus.REJECTED) {
            request.setStatus(AbsenceRequestStatus.REJECTED);
        }

        absenceRequestRepository.save(request);
        return mapToDto(request);
    }

}
