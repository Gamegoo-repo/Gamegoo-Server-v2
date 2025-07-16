package com.gamegoo.gamegoo_v2.core.config;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import jakarta.annotation.PostConstruct;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.ClassUtils;

import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@Profile("local")
public class P6SpyConfig implements MessageFormattingStrategy {

    @PostConstruct
    public void setLogMessageFormat() {
        P6SpyOptions.getActiveInstance().setLogMessageFormat(this.getClass().getName());
    }

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category,
                                String prepared, String sql, String url) {
        return String.format("[%s] | %d ms | %s", category, elapsed,
                formatSql(category, sql));
    }

    private String stackTrace() {
        return Stream.of(new Throwable().getStackTrace())
                .filter(t -> t.toString().startsWith("com.gamegoo") && !t.toString().contains(
                        ClassUtils.getUserClass(this).getName()))
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }

    private String formatSql(String category, String sql) {
        if (sql != null && !sql.trim().isEmpty() && Category.STATEMENT.getName().equals(category)) {
            String trimmedSql = sql.trim().toLowerCase(Locale.ROOT);
            return stackTrace() + (trimmedSql.startsWith("create") || trimmedSql.startsWith("alter")
                    || trimmedSql.startsWith("comment")
                    ? FormatStyle.DDL.getFormatter().format(sql)
                    : FormatStyle.BASIC.getFormatter().format(sql));
        }
        return sql;
    }

}

