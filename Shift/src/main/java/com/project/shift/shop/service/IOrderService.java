package com.project.shift.shop.service;

import com.project.shift.shop.dto.*;




public interface IOrderService {
    OrderDTO createOrder(OrderDTO orderDTO);
    OrderListResponseDTO getOrdersByUser(Long userId);
    
    // SHOP-008
    OrderDetailResponseDTO getOrderDetail(Long orderId);
    
    // SHOP-009 결제 요청
    PaymentResponseDTO requestPayment(PaymentRequestDTO requestDTO);

    // ######## 선물 결제 및 메시지 전송 ##############
    PaymentResponseDTO requestGiftPayment(PaymentRequestDTO requestDTO);
    
    // ######## 선물 메시지 세팅 및 반환 ##############
    String setGiftMessage(PaymentRequestDTO requestDTO);
  
    // SHOP-010 결제 결과 조회
    PaymentResultDTO getPaymentResult(Long orderId);

    // SHOP-011 포인트 사용/적립 내역 조회
    PointHistoryResponseDTO getPointHistory(Long userId);
    
    // SHOP-012 주문 취소
    OrderCancelResponseDTO cancelOrder(Long orderId);

    // SHOP-013 환불 요청
    RefundResponseDTO requestRefund(RefundRequestDTO requestDTO);

    // SHOP-016 금액권 주문 생성
    PointOrderResponseDTO createPointOrder(PointOrderRequestDTO dto);
    
    // SHOP-017 금액권 결제 완료 (포인트 적립)
    PointOrderCompleteDTO completePointPayment(Long orderId);
	
}
