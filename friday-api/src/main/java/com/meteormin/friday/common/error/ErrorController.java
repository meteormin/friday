package com.meteormin.friday.common.error;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import com.meteormin.friday.common.hexagon.annotation.RestAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Error Controller
 * </p>
 * <p>
 * just show error codes
 * </p>
 *
 * @author seongminyoo
 * @since 2023/11/20
 */
@Tag(name = "Errors")
@RestAdapter(path = "/errors")
@Profile("!prod || !stage")
public class ErrorController {
    /**
     * ErrorDetails - About error code's details
     *
     * @param code error code
     * @param httpStatusCode http status code
     * @param title error title
     * @param description error description
     */
    public record ErrorDetail(
            int code,
            int httpStatusCode,
            String title,
            String description) {
    }

    /**
     * Retrieves the list of error codes.
     *
     * @return A list of ErrorDetail objects representing the error codes.
     */
    @Operation(summary = "error code list",
            description = "error code list")
    @GetMapping("")
    public ResponseEntity<List<ErrorDetail>> errors() {
        List<ErrorDetail> errorDetails = new ArrayList<>();
        for (RestErrorCode errorCode : RestErrorCode.values()) {
            errorDetails.add(new ErrorDetail(
                    errorCode.getErrorCode(),
                    errorCode.getStatusCode(),
                    errorCode.getTitle(),
                    errorCode.getDescription()));
        }

        return ResponseEntity.ok(errorDetails);
    }
}
