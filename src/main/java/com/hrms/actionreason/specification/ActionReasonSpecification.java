package com.hrms.actionreason.specification;

import com.hrms.actionreason.entity.ActionReason;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

public class ActionReasonSpecification {

    public static Specification<ActionReason> search(
            String field,
            String value,
            String operator
    ) {

        return (root, query, criteriaBuilder) -> {

            if (value == null || value.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            switch (operator) {

                case "CONTAINS":
                    return criteriaBuilder.like(
                            criteriaBuilder.lower(root.get(field)),
                            "%" + value.toLowerCase() + "%"
                    );

                case "STARTS_WITH":
                    return criteriaBuilder.like(
                            criteriaBuilder.lower(root.get(field)),
                            value.toLowerCase() + "%"
                    );

                case "EQUAL_TO":
                    return criteriaBuilder.equal(
                            criteriaBuilder.lower(root.get(field)),
                            value.toLowerCase()
                    );

                case "DOES_NOT_CONTAIN":
                    return criteriaBuilder.notLike(
                            criteriaBuilder.lower(root.get(field)),
                            "%" + value.toLowerCase() + "%"
                    );

                case "NOT_EQUALS":
                    return criteriaBuilder.notEqual(
                            criteriaBuilder.lower(root.get(field)),
                            value.toLowerCase()
                    );

                default:
                    return criteriaBuilder.conjunction();
            }

        };

    }

}