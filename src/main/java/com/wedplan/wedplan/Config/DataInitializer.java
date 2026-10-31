package com.wedplan.wedplan.Config;

import com.wedplan.wedplan.bookingMgt.model.BookingModel;
import com.wedplan.wedplan.bookingMgt.model.BookingStatus;
import com.wedplan.wedplan.bookingMgt.repository.BookingRepository;
import com.wedplan.wedplan.packageAndServiceMgt.model.PackageandServiceModel;
import com.wedplan.wedplan.packageAndServiceMgt.repository.PackageandServiceRepository;
import com.wedplan.wedplan.paymentMgt.model.PaymentMethod;
import com.wedplan.wedplan.paymentMgt.model.PaymentModel;
import com.wedplan.wedplan.paymentMgt.model.PaymentStatus;
import com.wedplan.wedplan.paymentMgt.repository.PaymentRepository;
import com.wedplan.wedplan.reviewAndAlertMgt.model.ReviewAndAllertModel;
import com.wedplan.wedplan.reviewAndAlertMgt.repository.ReviewAndAlertRepository;
import com.wedplan.wedplan.userandAdminMgt.model.UserAndAdminModel;
import com.wedplan.wedplan.userandAdminMgt.repository.UserAndAdminRepository;
import com.wedplan.wedplan.vendorMgt.model.VendorModel;
import com.wedplan.wedplan.vendorMgt.repository.VendorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(
            UserAndAdminRepository userRepository,
            VendorRepository vendorRepository,
            PackageandServiceRepository packageRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            ReviewAndAlertRepository reviewRepository) {

        return args -> {
            // 1. Seed Users (Admin & Couple)
            if (userRepository.count() == 0) {
                userRepository.save(new UserAndAdminModel(
                        null, "Amaya Silva (Admin)", "admin@wedplan.com", "admin123", "ADMIN"
                ));
                userRepository.save(new UserAndAdminModel(
                        null, "Ishara Fernando (Staff)", "staff@wedplan.com", "staff123", "STAFF"
                ));
                userRepository.save(new UserAndAdminModel(
                        null, "Nethmi & Kavin", "nethmi@example.com", "couple123", "USER"
                ));
                userRepository.save(new UserAndAdminModel(
                        null, "Dilshan & Tharushi", "dilshan@example.com", "couple123", "USER"
                ));
            }

            // 2. Seed Vendors
            if (vendorRepository.count() == 0) {
                vendorRepository.save(new VendorModel(
                        null, "The Garden House", "gardenhouse@wedplan.lk", "Venues", "Gampaha",
                        "A leafy colonial bungalow with a lawn for the ceremony and a glass pavilion for dinner and dancing.",
                        4.8, "/img/reception.jpg"
                ));
                vendorRepository.save(new VendorModel(
                        null, "Lanka Weddings Photography", "lanka.photo@wedplan.lk", "Photography", "Colombo",
                        "Award-winning wedding photographers capturing candid moments, pre-shoots and aerial drone coverage.",
                        4.9, "/img/photography.jpg"
                ));
                vendorRepository.save(new VendorModel(
                        null, "Petals & Blooms", "petals@wedplan.lk", "Florals", "Colombo",
                        "Bespoke floral arches, elegant bouquets, poruwa decoration and floral styling across Sri Lanka.",
                        4.9, "/img/florals.jpg"
                ));
                vendorRepository.save(new VendorModel(
                        null, "Spice Symphony Catering", "spicesymphony@wedplan.lk", "Catering", "Kandy",
                        "Authentic Sri Lankan buffets and multi-cuisine wedding banquets with premium live cooking stations.",
                        4.7, "/img/catering.jpg"
                ));
                vendorRepository.save(new VendorModel(
                        null, "Ceylon Strings & Beats", "ceylonstrings@wedplan.lk", "Music", "Galle",
                        "Acoustic duos, full wedding live bands and classical string quartets for your church and reception.",
                        4.8, "/img/music.jpg"
                ));
                vendorRepository.save(new VendorModel(
                        null, "Glow Bridal Studio", "glowbridal@wedplan.lk", "Beauty", "Colombo",
                        "Bridal hair styling, traditional Kandyan drape, western veil dressing and professional makeup.",
                        4.9, "/img/beauty.jpg"
                ));
            }

            // 3. Seed Packages
            if (packageRepository.count() == 0) {
                packageRepository.save(new PackageandServiceModel(
                        null, 1L, "The Garden House", "Classic Venue Package", "Venues",
                        "Includes private lawn garden access, marquee banquet setup, lighting, and tables for up to 200 guests.",
                        new BigDecimal("400000.00"), "LKR", 200, 8, "Tables, Lighting, Bridal Room, Parking", true,
                        null, null
                ));
                packageRepository.save(new PackageandServiceModel(
                        null, 1L, "The Garden House", "Grand Pavilion Package", "Venues",
                        "Exclusive full-day access to both the glass pavilion and the garden lawns with luxury setups.",
                        new BigDecimal("650000.00"), "LKR", 300, 12, "Full Pavilion, DJ stage, Generator, Suite", true,
                        null, null
                ));
                packageRepository.save(new PackageandServiceModel(
                        null, 2L, "Lanka Weddings Photography", "Diamond Photo & Video", "Photography",
                        "2 Senior photographers, 2 cinematographers, drone coverage, 100-page photobook and teaser video.",
                        new BigDecimal("280000.00"), "LKR", null, 10, "Drone, 2 Photographers, Video teaser, Album", true,
                        null, null
                ));
            }

            // 4. Seed Bookings
            if (bookingRepository.count() == 0) {
                BookingModel booking1 = new BookingModel();
                booking1.setBookingReference("WP-2026-001");
                booking1.setCustomerName("Nethmi & Kavin");
                booking1.setCustomerEmail("nethmi@example.com");
                booking1.setCustomerPhone("+94 77 123 4567");
                booking1.setPartnerName("Kavin Jayawardena");
                booking1.setWeddingDate(LocalDate.now().plusMonths(4));
                booking1.setLocation("Gampaha");
                booking1.setVendorId(1L);
                booking1.setVendorName("The Garden House");
                booking1.setPackageId(1L);
                booking1.setPackageName("Classic Venue Package");
                booking1.setGuestCount(150);
                booking1.setTotalAmount(new BigDecimal("400000.00"));
                booking1.setDepositAmount(new BigDecimal("120000.00"));
                booking1.setIsPlusMember(true);
                booking1.setStatus(BookingStatus.CONFIRMED);
                booking1.setSpecialNotes("Afternoon ceremony with garden poruwa setup.");
                bookingRepository.save(booking1);

                BookingModel booking2 = new BookingModel();
                booking2.setBookingReference("WP-2026-002");
                booking2.setCustomerName("Dilshan & Tharushi");
                booking2.setCustomerEmail("dilshan@example.com");
                booking2.setCustomerPhone("+94 71 987 6543");
                booking2.setPartnerName("Tharushi Mendis");
                booking2.setWeddingDate(LocalDate.now().plusMonths(6));
                booking2.setLocation("Colombo");
                booking2.setVendorId(2L);
                booking2.setVendorName("Lanka Weddings Photography");
                booking2.setPackageId(3L);
                booking2.setPackageName("Diamond Photo & Video");
                booking2.setGuestCount(200);
                booking2.setTotalAmount(new BigDecimal("280000.00"));
                booking2.setDepositAmount(new BigDecimal("84000.00"));
                booking2.setIsPlusMember(false);
                booking2.setStatus(BookingStatus.PENDING);
                booking2.setSpecialNotes("Pre-shoot in Galle fort requested.");
                bookingRepository.save(booking2);
            }

            // 5. Seed Payments
            if (paymentRepository.count() == 0) {
                PaymentModel payment1 = new PaymentModel();
                payment1.setBookingReference("WP-2026-001");
                payment1.setCustomerName("Nethmi & Kavin");
                payment1.setCustomerEmail("nethmi@example.com");
                payment1.setDescription("Classic Venue Package - 30% Advance Deposit");
                payment1.setAmount(new BigDecimal("120000.00"));
                payment1.setOriginalAmount(new BigDecimal("120000.00"));
                payment1.setCurrency("LKR");
                payment1.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
                payment1.setPaymentStatus(PaymentStatus.COMPLETED);
                payment1.setPaymentType("DEPOSIT");
                payment1.setPaymentDate(LocalDate.now().minusDays(5));
                payment1.setTransactionReference("BOC-TRX-882910");
                payment1.setReceiptNumber("INV-2026-1001");
                payment1.setBankName("Bank of Ceylon");
                payment1.setNotes("Bank transfer verified by accounts.");
                paymentRepository.save(payment1);

                PaymentModel payment2 = new PaymentModel();
                payment2.setBookingReference("BK2041");
                payment2.setCustomerName("Nethmi & Kavin");
                payment2.setCustomerEmail("nethmi@example.com");
                payment2.setDescription("The Garden House - Classic Venue Package (30% Deposit)");
                payment2.setAmount(new BigDecimal("235710.00"));
                payment2.setOriginalAmount(new BigDecimal("261900.00"));
                payment2.setDiscountAmount(new BigDecimal("26190.00"));
                payment2.setCurrency("LKR");
                payment2.setPaymentMethod(PaymentMethod.CARD);
                payment2.setPaymentStatus(PaymentStatus.COMPLETED);
                payment2.setPaymentType("DEPOSIT");
                payment2.setCardLast4("4242");
                payment2.setPaymentDate(LocalDate.now().minusDays(2));
                payment2.setTransactionReference("TXN-773918");
                payment2.setReceiptNumber("INV-2026-1002");
                payment2.setNotes("Plus VIP 10% discount applied. Instant card confirmation.");
                paymentRepository.save(payment2);

                PaymentModel payment3 = new PaymentModel();
                payment3.setBookingReference("BK2039");
                payment3.setCustomerName("Sarah & Ashan");
                payment3.setCustomerEmail("sarah@example.com");
                payment3.setDescription("Table for Two Catering - Deposit (30%)");
                payment3.setAmount(new BigDecimal("63954.00"));
                payment3.setOriginalAmount(new BigDecimal("67320.00"));
                payment3.setDiscountAmount(new BigDecimal("3366.00"));
                payment3.setCurrency("LKR");
                payment3.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
                payment3.setPaymentStatus(PaymentStatus.PENDING);
                payment3.setPaymentType("DEPOSIT");
                payment3.setBankName("Commercial Bank");
                payment3.setPaymentDate(LocalDate.now());
                payment3.setTransactionReference("COMB-20261031-419");
                payment3.setReceiptNumber("INV-2026-1003");
                payment3.setNotes("Bank transfer slip submitted by couple. Awaiting admin approval.");
                paymentRepository.save(payment3);
            }

            // 6. Seed Reviews
            if (reviewRepository.count() == 0) {
                ReviewAndAllertModel review1 = new ReviewAndAllertModel();
                review1.setCoupleName("Kasun & Shenali");
                review1.setCoupleEmail("kasun@example.com");
                review1.setVendorId(1L);
                review1.setVendorName("The Garden House");
                review1.setTitle("Our dream garden wedding came to life!");
                review1.setStory("The team at The Garden House went above and beyond. From the lawn poruwa to the evening banquet pavilion, every single guest was stunned by the beauty of the venue.");
                review1.setRating(5);
                review1.setStatus("APPROVED");
                review1.setAlertType("REVIEW");
                reviewRepository.save(review1);

                ReviewAndAllertModel review2 = new ReviewAndAllertModel();
                review2.setCoupleName("Ruwan & Nimasha");
                review2.setCoupleEmail("ruwan@example.com");
                review2.setVendorId(2L);
                review2.setVendorName("Lanka Weddings Photography");
                review2.setTitle("Incredible photos, felt so natural");
                review2.setStory("They made us feel so comfortable in front of the lens. The candid shots from our church ceremony and reception are breathtaking.");
                review2.setRating(5);
                review2.setStatus("APPROVED");
                review2.setAlertType("REVIEW");
                reviewRepository.save(review2);
            }
        };
    }
}
