package com.project.shift.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDTO {

    private Long orderId;      // 주문 번호
    private Integer amount;    // 결제 총액 (주문 totalPrice와 같아야 함)
    private Integer pointUsed; // 사용 포인트 (null 또는 0이면 포인트 미사용)
    private Long receiverId;   // 선물 받는 친구의 사용자 ID
    private Long receiverName;   // 선물 받는 친구의 이름
    private String senderName; // 로그인한 사용자 이름 (메시지/로그용)
}
