package com.hrms.actionreason.specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hrms.actionreason.dto.SearchRequest;
import com.hrms.actionreason.entity.ActionReason;
import com.hrms.actionreason.exception.ResourceException;
import org.springframework.data.jpa.domain.Specification;

public final class ActionReasonSpecification {

    private static final Map<String, String> FIELD_MAPPING = Map.of(
            "actionReasonName", "actionReasonName",
            "actionReasonCode", "actionReasonCode",
            "description", "description"
    );

    private ActionReasonSpecification() {
    }

    public static Specification<ActionReason> search(SearchRequest request) {

        return (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (request.getValue() != null && !request.getValue().isBlank()) {
                String field = FIELD_MAPPING.get(request.getField());
                if (field == null) {
                    throw new ResourceException("Unsupported search field");
                }

                String value = request.getValue().trim().toLowerCase();
                String operator = request.getOperator() == null
                        ? "CONTAINS"
                        : request.getOperator().trim().toUpperCase();

                switch (operator) {
                    case "CONTAINS" -> predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get(field)),
                            "%" + value + "%"));
                    case "STARTS_WITH" -> predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get(field)),
                            value + "%"));
                    case "EQUAL_TO" -> predicates.add(criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get(field)),
                            value));
                    case "DOES_NOT_CONTAIN" -> predicates.add(criteriaBuilder.notLike(
                            criteriaBuilder.lower(root.get(field)),
                            "%" + value + "%"));
                    case "NOT_EQUALS" -> predicates.add(criteriaBuilder.notEqual(
                            criteriaBuilder.lower(root.get(field)),
                            value));
                    default -> throw new ResourceException("Unsupported search operator");
                }
            }

            if (request.getStatus() != null && !request.getStatus().isBlank()) {
                predicates.add(criteriaBuilder.equal(
                        root.get("status"),
                        Enum.valueOf(com.hrms.actionreason.enums.Status.class,
                                request.getStatus().trim().toUpperCase())));
            }

            if (request.getModuleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("module").get("id"), request.getModuleId()));
            }

            if (request.getModuleMasterId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("moduleMaster").get("id"), request.getModuleMasterId()));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

}
