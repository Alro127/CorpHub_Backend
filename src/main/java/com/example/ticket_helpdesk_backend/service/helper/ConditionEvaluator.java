package com.example.ticket_helpdesk_backend.service.helper;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

public class ConditionEvaluator {

    private static final ExpressionParser parser = new SpelExpressionParser();

    public static boolean evaluate(String expr, Map<String, Object> ctx) {
        if (expr == null || expr.isBlank()) return true; // không có điều kiện = luôn true

        StandardEvaluationContext context = new StandardEvaluationContext();
        ctx.forEach(context::setVariable); // map -> #var

        Boolean result = parser.parseExpression(expr).getValue(context, Boolean.class);
        return Boolean.TRUE.equals(result);
    }
}