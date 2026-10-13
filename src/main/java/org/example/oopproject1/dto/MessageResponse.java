package org.example.oopproject1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Data Transfer Object for standardized API responses containing a message.
 * <p>
 * Commonly used to return success or error messages in REST controllers.
 * </p>
 *
 * @since 1.0
 */
@Data
@AllArgsConstructor
public class MessageResponse {

    /**
     * The response message detailing the result of an operation.
     */
    private String message;

}