package ru.practicum.telemetry.enums;

import java.util.Arrays;
import java.util.Optional;

public class EnumMapper {
    public static <T extends Enum<T>> Optional<T> toAppEnum(T[] enumValues, String protoName) {
        if (protoName == null) {
            return Optional.empty();
        }

        return Arrays.stream(enumValues)
                .filter(type -> type.name().equalsIgnoreCase(protoName))
                .findFirst();
    }
}
