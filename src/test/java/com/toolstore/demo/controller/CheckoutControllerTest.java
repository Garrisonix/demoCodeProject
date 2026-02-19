package com.toolstore.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.toolstore.demo.dto.CheckoutRequest;
import com.toolstore.demo.exception.GlobalExceptionHandler;
import com.toolstore.demo.model.RentalAgreement;
import com.toolstore.demo.service.CheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Import(GlobalExceptionHandler.class)
class CheckoutControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CheckoutService checkoutService;

    @InjectMocks
    private CheckoutController checkoutController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(checkoutController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // -------------------------------------------------------
    // Happy Path Tests
    // -------------------------------------------------------

    @Test
    void checkout_ValidRequest_Returns200WithRentalAgreement() throws Exception {
        // Specification Test 3
        CheckoutRequest request = new CheckoutRequest("LADW", 3, 10, LocalDate.of(2020, 7, 2));
        RentalAgreement mockAgreement = buildMockAgreement("LADW", "LADDER", "Werner", 3,
                LocalDate.of(2020, 7, 2), LocalDate.of(2020, 7, 5),
                new BigDecimal("1.99"), 2, new BigDecimal("3.98"),
                10, new BigDecimal("0.40"), new BigDecimal("3.58"));

        when(checkoutService.checkout(anyString(), anyInt(), anyInt(), any(LocalDate.class)))
                .thenReturn(mockAgreement);

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toolCode").value("LADW"))
                .andExpect(jsonPath("$.toolType").value("LADDER"))
                .andExpect(jsonPath("$.toolBrand").value("Werner"))
                .andExpect(jsonPath("$.rentalDays").value(3))
                .andExpect(jsonPath("$.chargeDays").value(2))
                .andExpect(jsonPath("$.preDiscountCharge").value(3.98))
                .andExpect(jsonPath("$.discountPercent").value(10))
                .andExpect(jsonPath("$.discountAmount").value(0.40))
                .andExpect(jsonPath("$.finalCharge").value(3.58));

        verify(checkoutService, times(1))
                .checkout("LADW", 3, 10, LocalDate.of(2020, 7, 2));
    }

    @Test
    void checkout_ZeroDiscount_Returns200() throws Exception {
        // Specification test 4
        CheckoutRequest request = new CheckoutRequest("JAKD", 6, 0, LocalDate.of(2015, 9, 3));
        RentalAgreement mockAgreement = buildMockAgreement("JAKD", "JACKHAMMER", "DeWalt", 6,
                LocalDate.of(2015, 9, 3), LocalDate.of(2015, 9, 9),
                new BigDecimal("2.99"), 3, new BigDecimal("8.97"),
                0, new BigDecimal("0.00"), new BigDecimal("8.97"));

        when(checkoutService.checkout(anyString(), anyInt(), anyInt(), any(LocalDate.class)))
                .thenReturn(mockAgreement);

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.toolCode").value("JAKD"))
                .andExpect(jsonPath("$.discountPercent").value(0))
                .andExpect(jsonPath("$.discountAmount").value(0.00))
                .andExpect(jsonPath("$.finalCharge").value(8.97));
    }

    @Test
    void checkout_100PercentDiscount_Returns200() throws Exception {
        // Specification test 2
        CheckoutRequest request = new CheckoutRequest("LADW", 3, 100, LocalDate.of(2020, 7, 2));
        RentalAgreement mockAgreement = buildMockAgreement("LADW", "LADDER", "Werner", 3,
                LocalDate.of(2020, 7, 2), LocalDate.of(2020, 7, 5),
                new BigDecimal("1.99"), 2, new BigDecimal("3.98"),
                100, new BigDecimal("3.98"), new BigDecimal("0.00"));

        when(checkoutService.checkout(anyString(), anyInt(), anyInt(), any(LocalDate.class)))
                .thenReturn(mockAgreement);

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discountPercent").value(100))
                .andExpect(jsonPath("$.finalCharge").value(0.00));
    }

    // -------------------------------------------------------
    // Validation Tests - Service Layer Exceptions
    // -------------------------------------------------------

    @Test
    void checkout_DiscountOver100_Returns400WithMessage() throws Exception {
        // Specification test 1
        CheckoutRequest request = new CheckoutRequest("JAKR", 5, 101, LocalDate.of(2015, 9, 3));

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Discount percent must be between 0 and 100"));
    }

    @Test
    void checkout_NegativeDiscount_Returns400WithMessage() throws Exception {
        // Specification test 3 - modified to be invalid
        CheckoutRequest request = new CheckoutRequest("LADW", 3, -1, LocalDate.of(2020, 7, 2));

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Discount percent must be between 0 and 100"));
    }

    @Test
    void checkout_RentalDaysLessThanOne_Returns400WithMessage() throws Exception {
        // Specification test 2 - modified to be invalid
        CheckoutRequest request = new CheckoutRequest("LADW", 0, 10, LocalDate.of(2020, 7, 2));

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rental day count must be 1 or greater"));
    }

    @Test
    void checkout_InvalidToolCode_Returns400WithMessage() throws Exception {
        // Specification test 2 - modified to be invalid
        CheckoutRequest request = new CheckoutRequest("INVALID", 3, 10, LocalDate.of(2020, 7, 2));

        when(checkoutService.checkout(anyString(), anyInt(), anyInt(), any(LocalDate.class)))
                .thenThrow(new IllegalArgumentException("Tool not found: INVALID"));

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Tool not found: INVALID"));
    }

    // -------------------------------------------------------
    // Malformed Request Tests
    // -------------------------------------------------------

    @Test
    void checkout_MissingToolCode_Returns400() throws Exception {
        String requestJson = """
                {
                    "rentalDayCount": 3,
                    "discountPercent": 10,
                    "checkoutDate": "07/02/20"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("Status: " + response.getStatus());
        System.out.println("Error message: " + response.getErrorMessage());
        System.out.println("Response body: " + response.getContentAsString());
    }

    @Test
    void checkout_MissingCheckoutDate_Returns400() throws Exception {
        String requestJson = """
                {
                    "toolCode": "LADW",
                    "rentalDayCount": 3,
                    "discountPercent": 10
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("Status: " + response.getStatus());
        System.out.println("Error message: " + response.getErrorMessage());
        System.out.println("Response body: " + response.getContentAsString());
    }

    @Test
    void checkout_EmptyBody_Returns400() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andReturn();

        MockHttpServletResponse response = result.getResponse();
        System.out.println("Status: " + response.getStatus());
        System.out.println("Error message: " + response.getErrorMessage());
        System.out.println("Response body: " + response.getContentAsString());
    }

    // -------------------------------------------------------
    // Specification Tests
    // -------------------------------------------------------

    @Test
    void specification_test_2() throws Exception {
        CheckoutRequest request = new CheckoutRequest("LADW", 3, 10, LocalDate.of(2020, 7, 2));

        when(checkoutService.checkout(anyString(), anyInt(), anyInt(), any(LocalDate.class)))
                .thenReturn(buildMockAgreement("LADW", "LADDER", "Werner", 3,
                        LocalDate.of(2020, 7, 2), LocalDate.of(2020, 7, 5),
                        new BigDecimal("1.99"), 2, new BigDecimal("3.98"),
                        10, new BigDecimal("0.40"), new BigDecimal("3.58")));

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(checkoutService).checkout("LADW", 3, 10, LocalDate.of(2020, 7, 2));
    }

    @Test
    void specification_test_3() throws Exception {
        CheckoutRequest request = new CheckoutRequest("CHNS", 5, 25, LocalDate.of(2015, 7, 2));

        when(checkoutService.checkout(anyString(), anyInt(), anyInt(), any(LocalDate.class)))
                .thenReturn(buildMockAgreement("CHNS", "CHAINSAW", "Stihl", 5,
                        LocalDate.of(2015, 7, 2), LocalDate.of(2015, 7, 7),
                        new BigDecimal("1.49"), 4, new BigDecimal("5.96"),
                        25, new BigDecimal("1.49"), new BigDecimal("4.47")));

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(checkoutService).checkout("CHNS", 5, 25, LocalDate.of(2015, 7, 2));
    }

    @Test
    void specification_test_4() throws Exception {
        CheckoutRequest request = new CheckoutRequest("JAKD", 6, 0, LocalDate.of(2015, 9, 3));

        when(checkoutService.checkout(anyString(), anyInt(), anyInt(), any(LocalDate.class)))
                .thenReturn(buildMockAgreement("JAKD", "JACKHAMMER", "DeWalt", 6,
                        LocalDate.of(2015, 9, 3), LocalDate.of(2015, 9, 9),
                        new BigDecimal("2.99"), 3, new BigDecimal("8.97"),
                        0, new BigDecimal("0.00"), new BigDecimal("8.97")));

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(checkoutService).checkout("JAKD", 6, 0, LocalDate.of(2015, 9, 3));
    }

    @Test
    void specification_test_5() throws Exception {
        CheckoutRequest request = new CheckoutRequest("JAKR", 9, 0, LocalDate.of(2015, 7, 2));

        when(checkoutService.checkout(anyString(), anyInt(), anyInt(), any(LocalDate.class)))
                .thenReturn(buildMockAgreement("JAKR", "JACKHAMMER", "Ridgid", 9,
                        LocalDate.of(2015, 7, 2), LocalDate.of(2015, 7, 11),
                        new BigDecimal("2.99"), 6, new BigDecimal("17.94"),
                        0, new BigDecimal("0.00"), new BigDecimal("17.94")));

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(checkoutService).checkout("JAKR", 9, 0, LocalDate.of(2015, 7, 2));
    }

    @Test
    void specification_test_6() throws Exception {
        CheckoutRequest request = new CheckoutRequest("JAKR", 4, 50, LocalDate.of(2020, 7, 2));

        when(checkoutService.checkout(anyString(), anyInt(), anyInt(), any(LocalDate.class)))
                .thenReturn(buildMockAgreement("JAKR", "JACKHAMMER", "Ridgid", 4,
                        LocalDate.of(2020, 7, 2), LocalDate.of(2020, 7, 6),
                        new BigDecimal("2.99"), 1, new BigDecimal("2.99"),
                        50, new BigDecimal("1.50"), new BigDecimal("1.49")));

        mockMvc.perform(post("/api/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(checkoutService).checkout("JAKR", 4, 50, LocalDate.of(2020, 7, 2));
    }

    // -------------------------------------------------------
    // Helper
    // -------------------------------------------------------

    private RentalAgreement buildMockAgreement(
            String toolCode, String toolType, String toolBrand, int rentalDays,
            LocalDate checkoutDate, LocalDate dueDate, BigDecimal dailyCharge,
            int chargeDays, BigDecimal preDiscountCharge, int discountPercent,
            BigDecimal discountAmount, BigDecimal finalCharge) {

        return RentalAgreement.builder()
                .toolCode(toolCode)
                .toolType(toolType)
                .toolBrand(toolBrand)
                .rentalDays(rentalDays)
                .checkoutDate(checkoutDate)
                .dueDate(dueDate)
                .dailyRentalCharge(dailyCharge)
                .chargeDays(chargeDays)
                .preDiscountCharge(preDiscountCharge)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .finalCharge(finalCharge)
                .build();
    }
}