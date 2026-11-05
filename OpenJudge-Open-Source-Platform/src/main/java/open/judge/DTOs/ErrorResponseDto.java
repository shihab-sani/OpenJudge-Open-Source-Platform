package open.judge.DTOs;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
public class ErrorResponseDto {
    private String code;
    private String message;
    private List<FieldErrorDto> errors;
    private Instant timestamp;

    public static ErrorResponseDto of(String code, String message) {
        return ErrorResponseDto.builder()
                .code(code).message(message)
                .timestamp(Instant.now()).build();
    }

    public static ErrorResponseDto validation(List<FieldErrorDto> errors) {
        return ErrorResponseDto.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .errors(errors)
                .timestamp(Instant.now()).build();
    }
}
