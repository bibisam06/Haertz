package com.haertz.be.payment.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class KakaoPayApproveRequestDto {
    private String tid;
    //private String partner_order_id;
    private String designerScheduleId;
    private String pg_token;
}
