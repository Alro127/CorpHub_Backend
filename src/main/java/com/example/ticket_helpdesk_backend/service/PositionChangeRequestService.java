package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.consts.UserRole;
import com.example.ticket_helpdesk_backend.dto.PositionChangeAttachmentDto;
import com.example.ticket_helpdesk_backend.dto.PositionChangeRequestCreateDto;
import com.example.ticket_helpdesk_backend.dto.PositionChangeRequestDetailDto;
import com.example.ticket_helpdesk_backend.entity.*;
import com.example.ticket_helpdesk_backend.repository.*;
import com.example.ticket_helpdesk_backend.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PositionChangeRequestService {


    private final PositionChangeRequestRepository requestRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeProfileRepository employeeRepository;
    private final PositionChangeAttachmentService positionChangeAttachmentService;
    private final PositionChangeApprovalRepository approvalRepository;
    private final EmployeeDocumentService employeeDocumentService; // dùng khi upload quyết định
    private final InternalWorkHistoryRepository internalWorkHistoryRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final FileStorageService fileStorageService;
    private final PositionChangeAttachmentRepository positionChangeAttachmentRepository;
    private final JwtUtil jwtUtil;

    // ================= CREATE =================

    @Transactional
    public PositionChangeRequestDetailDto createRequest(PositionChangeRequestCreateDto dto, String token) {
        UUID createdById = jwtUtil.getUserId(token);

        EmployeeProfile employee = employeeProfileRepository.findById(UUID.fromString(dto.getEmployeeId()))
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        EmployeeProfile createdBy = employeeProfileRepository.findById(createdById)
                .orElseThrow(() -> new RuntimeException("CreatedBy employee not found"));

        Position newPosition = positionRepository.findById(UUID.fromString(dto.getNewPositionId()))
                .orElseThrow(() -> new RuntimeException("New position not found"));

        Department newDepartment = departmentRepository.findById(UUID.fromString(dto.getNewDepartmentId()))
                .orElseThrow(() -> new RuntimeException("New department not found"));

        Position oldPosition = null;
        if (dto.getOldPositionId() != null) {
            oldPosition = positionRepository.findById(UUID.fromString(dto.getOldPositionId()))
                    .orElseThrow(() -> new RuntimeException("Old position not found"));
        }

        Department oldDepartment = null;
        if (dto.getOldDepartmentId() != null) {
            oldDepartment = departmentRepository.findById(UUID.fromString(dto.getOldDepartmentId()))
                    .orElseThrow(() -> new RuntimeException("Old department not found"));
        }

        PositionChangeRequest request = new PositionChangeRequest();
        request.setEmployee(employee);
        request.setOldPosition(oldPosition);
        request.setOldDepartment(oldDepartment);
        request.setNewPosition(newPosition);
        request.setNewDepartment(newDepartment);
        request.setType(dto.getType());
        request.setEffectDate(dto.getEffectDate() != null ? dto.getEffectDate() : LocalDate.now());
        request.setReason(dto.getReason());
        request.setCreatedBy(createdBy);
        request.setCreatedAt(LocalDateTime.now());

        boolean isAdminCreator = createdBy.getUser().getRole().getName().equals(UserRole.ROLE_ADMIN.toString());

        request.setStatus(isAdminCreator
                ? PositionChangeRequest.STATUS_DONE
                : PositionChangeRequest.STATUS_PENDING);

        // Attachments
        if (dto.getAttachments() != null) {
            var savedAttachments = dto.getAttachments()
                    .stream()
                    .map(a -> positionChangeAttachmentService.saveAttachment(a, request))
                    .toList();
            request.setAttachments(savedAttachments);
        }

        // Lưu request trước để có ID
        PositionChangeRequest saved = requestRepository.save(request);

        // Tạo các bước phê duyệt theo workflow
        generateApprovalSteps(saved, isAdminCreator);

        return PositionChangeRequestDetailDto.mapEntityToDetailDto(saved);
    }
//
//    // ================= WORKFLOW GENERATION =================
//
    @Transactional
    protected void generateApprovalSteps(PositionChangeRequest request, boolean isAdminCreator) {

        if (isAdminCreator) {
            // ADMIN tự duyệt luôn
            EmployeeProfile admin = request.getCreatedBy();

            PositionChangeApproval adminStep = new PositionChangeApproval();
            adminStep.setRequest(request);
            adminStep.setStepOrder(1);
            adminStep.setApprover(admin);
            adminStep.setRole(PositionChangeApproval.ROLE_ADMIN);
            adminStep.setDecision(PositionChangeApproval.DECISION_PENDING);


            approvalRepository.save(adminStep);
            return;
        }

        int step = 1;

        // Step 1: Manager của employee.department
        Department dept = request.getEmployee().getDepartment();
        if (dept == null || dept.getManager() == null) {
            throw new RuntimeException("Department or department manager not found for employee");
        }

        PositionChangeApproval managerStep = new PositionChangeApproval();
        managerStep.setRequest(request);
        managerStep.setStepOrder(step++);
        managerStep.setApprover(dept.getManager());
        managerStep.setRole(PositionChangeApproval.ROLE_MANAGER);
        managerStep.setDecision(PositionChangeApproval.DECISION_PENDING);

        // Step 2: HR_MANAGER
        EmployeeProfile hrManager = employeeProfileRepository.findFirstByUserRoleName(UserRole.ROLE_HR.toString())
                .orElseThrow(() -> new RuntimeException("HR Manager not found"));
        PositionChangeApproval hrStep = new PositionChangeApproval();
        hrStep.setRequest(request);
        hrStep.setStepOrder(step++);
        hrStep.setApprover(hrManager);
        hrStep.setRole(PositionChangeApproval.ROLE_HR);
        hrStep.setDecision(PositionChangeApproval.DECISION_PENDING);

        // Step 3: ADMIN (chọn 1 admin đại diện, hoặc để nhiều admin share 1 account)
        EmployeeProfile admin = employeeProfileRepository.findFirstByUserRoleName(UserRole.ROLE_ADMIN.toString())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        PositionChangeApproval adminStep = new PositionChangeApproval();
        adminStep.setRequest(request);
        adminStep.setStepOrder(step);
        adminStep.setApprover(admin);
        adminStep.setRole(PositionChangeApproval.ROLE_ADMIN);
        adminStep.setDecision(PositionChangeApproval.DECISION_PENDING);

        approvalRepository.save(managerStep);
        approvalRepository.save(hrStep);
        approvalRepository.save(adminStep);
    }

    // ================= GET =================

    @Transactional
    public PositionChangeRequestDetailDto getRequest(UUID id) {
        PositionChangeRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        return PositionChangeRequestDetailDto.mapEntityToDetailDto(request);
    }

    public List<PositionChangeRequestDetailDto> getRequestsByEmployee(UUID employeeId) {
        return requestRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId)
                .stream()
                .map(PositionChangeRequestDetailDto::mapEntityToDetailDto)
                .collect(Collectors.toList());
    }

    public List<PositionChangeRequestDetailDto> getAll(String status) {
        List<PositionChangeRequest> list;
        if (status != null) {
            list = requestRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            list = requestRepository.findAll();
        }
        return list.stream()
                .map(PositionChangeRequestDetailDto::mapEntityToDetailDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public void updateStatus(UUID requestId, String status) {
        PositionChangeRequest request = requestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(status);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
    }

    @Transactional
    public void finalizeRequest(UUID requestId) {
        PositionChangeRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        // cập nhật thông tin nhân viên
        EmployeeProfile employee = request.getEmployee();
        employee.setPosition(request.getNewPosition());
        employee.setDepartment(request.getNewDepartment());
        employeeProfileRepository.save(employee);

        request.setStatus(PositionChangeRequest.STATUS_FINALIZED);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
    }

    @Transactional
    public void uploadDecisionFile(UUID requestId, MultipartFile file, String token) {
        PositionChangeRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.isFinalized()) {
            throw new RuntimeException("Request is not finalized yet");
        }

        UUID hrId = jwtUtil.getUserId(token);
        EmployeeProfile hr = employeeRepository.findById(hrId)
                .orElseThrow(() -> new RuntimeException("HR not found"));

        // 1. Upload file lên MinIO
        String bucket = "employee-documents";
        String prefix =  "decisions_" + request.getEmployee().getFullName();
        String fileUrl = fileStorageService.uploadFile(bucket, file, prefix);

        // -----------------------------
        // 2. Lưu file vào EmployeeDocument
        // -----------------------------
        EmployeeDocument doc = new EmployeeDocument();
        doc.setEmployeeProfile(request.getEmployee());
        doc.setDocumentType(documentTypeRepository.findByCode("DECISION"));
        doc.setTitle("Decision for position change");
        doc.setDescription("Uploaded by HR after final approval");
        doc.setFileName(file.getOriginalFilename());
        doc.setFileType(file.getContentType());
        doc.setFileUrl(fileUrl);
        doc.setActive(true);
        employeeDocumentRepository.save(doc);

        // -----------------------------
        // 3. Lưu vào PositionChangeAttachment
        // -----------------------------
        PositionChangeAttachment positionChangeAttachment = new PositionChangeAttachment();
        positionChangeAttachment.setRequest(request);
        positionChangeAttachment.setFileName(file.getOriginalFilename());
        positionChangeAttachment.setFileUrl(fileUrl);
        positionChangeAttachment.setUploadedBy(hr);
        positionChangeAttachment.setUploadedAt(LocalDateTime.now());
        positionChangeAttachmentRepository.save(positionChangeAttachment);

        // Gắn vào request (nếu bạn muốn hiển thị khi load request detail)
        request.getAttachments().add(positionChangeAttachment);

        // -----------------------------
        // 4. Lưu Internal Work History
        // -----------------------------
        InternalWorkHistory history = new InternalWorkHistory();
        history.setEmployeeProfile(request.getEmployee());
        history.setDepartment(request.getNewDepartment());
        history.setPosition(request.getNewPosition());
        history.setEffectiveDate(request.getEffectDate());
        history.setChangeType(request.getType().toUpperCase());
        history.setReason(request.getReason());
        history.setRequest(request);
        internalWorkHistoryRepository.save(history);

        // -----------------------------
        // 5. Cập nhật thông tin nhân viên
        // -----------------------------
        EmployeeProfile emp = request.getEmployee();
        emp.setDepartment(request.getNewDepartment());
        emp.setPosition(request.getNewPosition());
        employeeRepository.save(emp);

        // -----------------------------
        // 6. Cập nhật trạng thái request → DONE
        // -----------------------------
        request.setStatus("DONE");
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);
    }
}
