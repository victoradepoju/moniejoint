package com.victor.saving_group_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePayoutOrderRequest {
    @NotEmpty
    private List<@NotBlank String> memberIds;
}
