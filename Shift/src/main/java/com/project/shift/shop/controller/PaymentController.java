package com.project.shift.shop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.shift.shop.dto.PaymentRequestDTO;
import com.project.shift.shop.dto.PaymentResponseDTO;
import com.project.shift.shop.dto.PaymentResultDTO;
import com.project.shift.shop.service.IOrderService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final IOrderService orderService;
	
    // SHOP-009 결제 요청
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> requestPayment(@RequestBody PaymentRequestDTO request) {
        PaymentResponseDTO response = orderService.requestPayment(request);
        return ResponseEntity.ok(response);
    }
    
    // SHOP-010 결제 결과 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<PaymentResultDTO> getPaymentResult(@PathVariable Long orderId) {
        PaymentResultDTO response = orderService.getPaymentResult(orderId);
        return ResponseEntity.ok(response);
    }
    
	// SHOP-018 선물 결제 및 채팅 전송
    // 채팅방 존재 여부와 관계 없이 호추 가능
    @PostMapping("/gift")
    public ResponseEntity<PaymentResponseDTO> requestGiftPayment(HttpServletRequest request, @RequestBody PaymentRequestDTO dto) {    		
		log.info("[Data] receiverId {}", dto.getReceiverId());
		log.info("[Data] receiverName {}", dto.getReceiverName());
    	PaymentResponseDTO response = orderService.requestGiftPayment(dto);
        return ResponseEntity.ok(response);
    }

}
