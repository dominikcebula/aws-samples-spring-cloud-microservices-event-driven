package com.dominikcebula.aws.samples.spring.cloud.shipment.utils;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

import static java.util.Optional.ofNullable;

public class PredicateUtils {
    private PredicateUtils() {
    }

    public static Predicate condition(String value, Function<String, BooleanExpression> condition) {
        return ofNullable(value)
                .filter(StringUtils::isNotBlank)
                .map(condition)
                .orElse(null);

    }
}
