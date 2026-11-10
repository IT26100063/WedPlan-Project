package com.wedplan.wedplan.services.bookingMgt;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class BookingRequest {
    @NotBlank
    @Size(max = 120)
    private String customerName;

    @NotBlank
    @Email
    @Size(max = 180)
    private String customerEmail;

    @NotBlank
    @Pattern(regexp = "\\+?[0-9][0-9\\s().-]{6,38}", message = "Enter a valid phone number.")
    @Size(max = 40)
    private String customerPhone;

    @NotBlank
    @Size(max = 255)
    private String customerAddress;

    @NotBlank
    @Size(max = 100)
    private String customerCity;

    @Size(max = 80)
    private String customerCountry = "Sri Lanka";

    @NotBlank
    @Size(max = 150)
    private String vendorName;

    private LocalDate eventDate;

    @Size(max = 40)
    private String guestCount;

    @Size(max = 1500)
    private String notes;

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public String getCustomerCity() { return customerCity; }
    public void setCustomerCity(String customerCity) { this.customerCity = customerCity; }
    public String getCustomerCountry() { return customerCountry; }
    public void setCustomerCountry(String customerCountry) { this.customerCountry = customerCountry; }
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    public String getGuestCount() { return guestCount; }
    public void setGuestCount(String guestCount) { this.guestCount = guestCount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
