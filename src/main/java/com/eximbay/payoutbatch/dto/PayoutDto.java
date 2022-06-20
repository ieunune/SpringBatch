package com.eximbay.payoutbatch.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
public class PayoutDto {
    
    private String payoutDate;
    private String transactionType;
    private String status;

}
