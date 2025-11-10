package com.example.ticket_helpdesk_backend.service;

import com.example.ticket_helpdesk_backend.dto.AbsenceBalanceResponse;
import com.example.ticket_helpdesk_backend.dto.UserDto;
import com.example.ticket_helpdesk_backend.entity.AbsenceBalance;
import com.example.ticket_helpdesk_backend.entity.AbsenceType;
import com.example.ticket_helpdesk_backend.entity.User;
import com.example.ticket_helpdesk_backend.exception.ResourceNotFoundException;
import com.example.ticket_helpdesk_backend.repository.AbsenceBalanceRepository;
import com.example.ticket_helpdesk_backend.repository.AbsenceTypeRepository;
import com.example.ticket_helpdesk_backend.repository.UserRepository;
import com.example.ticket_helpdesk_backend.specification.AbsenceBalanceSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbsenceBalanceService {

    private final AbsenceBalanceRepository absenceBalanceRepository;
    private final AbsenceTypeRepository absenceTypeRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    /* -------------------- LẤY DỮ LIỆU -------------------- */
    public List<AbsenceBalanceResponse> getAll(UUID userId, UUID absenceTypeId, Integer year) {
        Specification<AbsenceBalance> spec = Specification
                .where(AbsenceBalanceSpecifications.hasUserId(userId))
                .and(AbsenceBalanceSpecifications.hasAbsenceTypeId(absenceTypeId))
                .and(AbsenceBalanceSpecifications.hasYear(year));

        return absenceBalanceRepository.findAll(spec)
                .stream()
                .map(entity -> {
                    AbsenceBalanceResponse dto = modelMapper.map(entity, AbsenceBalanceResponse.class);
                    dto.setUser(UserDto.toUserDto(entity.getUser()));
                    return dto;
                })
                .toList();
    }

    public AbsenceBalance getByKeys(UUID userId, UUID absenceTypeId, Integer year)
            throws ResourceNotFoundException {

        Specification<AbsenceBalance> spec = Specification
                .where(AbsenceBalanceSpecifications.hasUserId(userId))
                .and(AbsenceBalanceSpecifications.hasAbsenceTypeId(absenceTypeId))
                .and(AbsenceBalanceSpecifications.hasYear(year));

        return absenceBalanceRepository.findOne(spec)
                .orElseThrow(() -> new ResourceNotFoundException("Absence balance not found"));
    }

    /* -------------------- TẠO LEAVE BALANCE -------------------- */
    @Transactional
    public void generateForYear(Integer year) {
        log.info("Generating absence balances for year {}", year);
        List<User> users = userRepository.findAll();
        List<AbsenceType> absenceTypes = absenceTypeRepository.findAll()
                .stream()
                .filter(lt -> Boolean.TRUE.equals(lt.getAffectQuota()))
                .toList();

        for (User user : users) {
            for (AbsenceType type : absenceTypes) {
                Specification<AbsenceBalance> spec = Specification
                        .where(AbsenceBalanceSpecifications.hasUserId(user.getId()))
                        .and(AbsenceBalanceSpecifications.hasAbsenceTypeId(type.getId()))
                        .and(AbsenceBalanceSpecifications.hasYear(year));

                boolean exists = absenceBalanceRepository.exists(spec);
                if (exists) continue;

                AbsenceBalance balance = new AbsenceBalance();
                balance.setUser(user);
                balance.setAbsenceType(type);
                balance.setYear(year);
                balance.setTotalDays(type.getMaxPerRequest() != null ? type.getMaxPerRequest() : BigDecimal.valueOf(12));
                balance.setUsedDays(BigDecimal.ZERO);
                balance.setCarriedOver(BigDecimal.ZERO);
                balance.setLastUpdated(LocalDateTime.now());
                absenceBalanceRepository.save(balance);
            }
        }
        log.info("✅ Generated absence balances for year {}", year);
    }

    /* -------------------- CẬP NHẬT SAU KHI DUYỆT -------------------- */
    @Transactional
    public void deductDays(UUID userId, UUID absenceTypeId, int year, BigDecimal days) throws ResourceNotFoundException {
        AbsenceBalance balance = getByKeys(userId, absenceTypeId, year);

        BigDecimal used = balance.getUsedDays().add(days);
        BigDecimal remaining = balance.getTotalDays().subtract(used);

        balance.setUsedDays(used);
        balance.setLastUpdated(LocalDateTime.now());

        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("⚠️ User {} exceeded quota for absenceType {}", userId, absenceTypeId);
        }

        absenceBalanceRepository.save(balance);
    }

    @Transactional
    public void restoreDays(UUID userId, UUID absenceTypeId, int year, BigDecimal days) {
        Specification<AbsenceBalance> spec = Specification
                .where(AbsenceBalanceSpecifications.hasUserId(userId))
                .and(AbsenceBalanceSpecifications.hasAbsenceTypeId(absenceTypeId))
                .and(AbsenceBalanceSpecifications.hasYear(year));

        absenceBalanceRepository.findOne(spec).ifPresent(balance -> {
            balance.setUsedDays(balance.getUsedDays().subtract(days));
            balance.setLastUpdated(LocalDateTime.now());
            absenceBalanceRepository.save(balance);
        });
    }

    /* -------------------- AUTO SCHEDULER -------------------- */
    @Scheduled(cron = "0 0 1 1 1 *")
    @Transactional
    public void autoGenerateYearlyBalances() {
        int year = LocalDateTime.now().getYear();
        generateForYear(year);
    }
}
