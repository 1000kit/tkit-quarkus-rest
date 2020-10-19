package org.tkit.quarkus.rs.mappers;

import org.eclipse.microprofile.config.ConfigProvider;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Mapper
public abstract class OffsetDateTimeMapper {

    static final ZoneOffset ZONE_OFFSET = ZoneId.of(
            ConfigProvider.getConfig()
                    .getOptionalValue("quarkus.hibernate-orm.jdbc.timezone", String.class)
                    .orElse(ZoneOffset.UTC.getId())
    ).getRules().getOffset(Instant.now());

    public OffsetDateTime map(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return OffsetDateTime.of(dateTime,ZONE_OFFSET);
    }

    public LocalDateTime map(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return LocalDateTime.ofInstant(offsetDateTime.toInstant(), ZONE_OFFSET);
    }

}
