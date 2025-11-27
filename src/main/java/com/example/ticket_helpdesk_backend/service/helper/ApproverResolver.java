package com.example.ticket_helpdesk_backend.service.helper;

import com.example.ticket_helpdesk_backend.consts.ApproverType;
import com.example.ticket_helpdesk_backend.consts.UserRelationKey;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.model.ApproverDefinition;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.example.ticket_helpdesk_backend.specification.UserSpecifications.*;

@Component
@RequiredArgsConstructor
public class ApproverResolver {

    private final UserRepository userRepository;

    /**
     * ⭐ Resolve chung cho mọi loại ApproverType
     */
    public UUID resolve(ApproverDefinition def, Map<String, Object> ctx) {
        if (def == null) return null;

        ApproverType type = def.getType();
        Map<String, Object> params = def.getParams();

        return switch (type) {
            case USER -> resolveUser(params);
            case USER_RELATION -> resolveByUserRelation(params, ctx);
            case POSITION -> resolveByPosition(params, ctx);
            case POSITION_LEVEL -> resolveByPositionLevel(params, ctx);
            case DEPARTMENT -> resolveByDepartment(params, ctx);
        };
    }

    // ============================================================
    // USER — chỉ trả về userId đã cấu hình
    // ============================================================
    private UUID resolveUser(Map<String, Object> params) {
        return UUID.fromString(params.get("userId").toString());
    }

    // ============================================================
    // USER_RELATION — tìm người liên quan đến user
    // ============================================================
    private UUID resolveByUserRelation(Map<String, Object> params, Map<String, Object> ctx) {

        UserRelationKey key = UserRelationKey.valueOf(params.get("key").toString());
        UUID requesterId = (UUID) ctx.get("requesterId");

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        var profile = requester.getEmployeeProfile();
        if (profile == null) return null;

        return switch (key) {

            case DIRECT_MANAGER -> {
                if (profile.getManager() != null && profile.getManager().getUser() != null)
                    yield profile.getManager().getUser().getId();
                yield null;
            }

            case DEPARTMENT_MANAGER -> {
                var dept = profile.getDepartment();
                if (dept != null && dept.getManager() != null && dept.getManager().getUser() != null)
                    yield dept.getManager().getUser().getId();
                yield null;
            }
        };
    }


    // ============================================================
    // 3️⃣ POSITION — tìm User theo Position.code + cùng phòng ban
    // ============================================================
    private UUID resolveByPosition(Map<String, Object> params, Map<String, Object> ctx) {

        String code = params.get("code").toString();
        UUID requesterId = (UUID) ctx.get("requesterId");

        Specification<User> spec = Specification
                .where(hasPositionCode(code))
                .and(inSameDepartmentAsUser(requesterId))
                .and(isActive(true))
                .and(withEmployeeJoins());

        Optional<User> found = userRepository.findOne(spec);
        return found.map(User::getId).orElse(null);
    }

    // ============================================================
    // 4️⃣ POSITION_LEVEL — tìm User theo cấp bậc trong Position
    // ============================================================
    private UUID resolveByPositionLevel(Map<String, Object> params, Map<String, Object> ctx) {

        Integer level = (Integer) params.get("levelOrder");
        UUID requesterId = (UUID) ctx.get("requesterId");

        Specification<User> spec = Specification
                .where(hasPositionLevel(level))
                .and(inSameDepartmentAsUser(requesterId))
                .and(isActive(true));

        Optional<User> found = userRepository.findOne(spec);
        return found.map(User::getId).orElse(null);
    }

    // ============================================================
    // 5️⃣ DEPARTMENT — HEAD / DIRECTOR / MANAGER → dùng từ context
    // ============================================================
    private UUID resolveByDepartment(Map<String, Object> params, Map<String, Object> ctx) {

        String deptRole = params.get("role").toString().toUpperCase();

        return switch (deptRole) {
            case "HEAD" -> (UUID) ctx.get("departmentHeadId");
            case "DIRECTOR" -> (UUID) ctx.get("departmentDirectorId");
            case "MANAGER" -> (UUID) ctx.get("managerId");
            default -> null;
        };
    }
}
