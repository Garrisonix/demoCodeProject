package com.toolstore.demo.controller;

import com.toolstore.demo.dto.CheckoutRequest;
import com.toolstore.demo.model.RentalAgreement;
import com.toolstore.demo.service.CheckoutService;
import com.toolstore.demo.view.RentalAgreementFormatter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    public ResponseEntity<RentalAgreement> checkout(@Valid @RequestBody CheckoutRequest request) {
        System.out.println("Received Checkout Request: " + request);
        RentalAgreement agreement = checkoutService.checkout(
                request.toolCode(),
                request.rentalDayCount(),
                request.discountPercent(),
                request.checkoutDate()
        );

        System.out.println("Checkout Successful. Generated Agreement:");
        System.out.println(RentalAgreementFormatter.format(agreement));

        return ResponseEntity.ok(agreement);
    }
}