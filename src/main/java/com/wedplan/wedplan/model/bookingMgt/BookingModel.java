package com.wedplan.wedplan.model.bookingMgt;

public abstract class BookingModel extends BaseEntity {
    private String customerName;
    private String vendorName;
    private String eventDate;   // yyyy-MM-dd
    private String status;      // PENDING, CONFIRMED, CANCELLED

    protected BookingModel(String id, String createdAt, String customerName,
                           String vendorName, String eventDate, String status) {
        super(id, createdAt);
        this.customerName = customerName;
        this.vendorName = vendorName;
        this.eventDate = eventDate;
        this.status = status;
    }

    // Each subclass must implement these (polymorphism)
    public abstract double calculateTotal();
    public abstract String getType();
    public abstract double getQuantity();   // hours or guests
    public abstract double getRate();       // hourly rate or price per plate
    public double getExtra() { return 0; }  // optional override (album fee)

    public String toFileString() {
        return String.join("|", getType(), getId(), getCreatedAt(), customerName,
                vendorName, eventDate, status,
                String.valueOf(getQuantity()), String.valueOf(getRate()),
                String.valueOf(getExtra()));
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    public String getEventDate() { return eventDate; }
    public void setEventDate(String eventDate) { this.eventDate = eventDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // ================= SUBCLASSES =================

    public static class VenueBooking extends BookingModel {
        private double hours;
        private double hourlyRate;

        public VenueBooking(String id, String createdAt, String customer, String vendor,
                            String date, String status, double hours, double hourlyRate) {
            super(id, createdAt, customer, vendor, date, status);
            this.hours = hours;
            this.hourlyRate = hourlyRate;
        }

        @Override public double calculateTotal() { return hours * hourlyRate * 1.10; } // 10% service charge
        @Override public String getType() { return "VENUE"; }
        @Override public double getQuantity() { return hours; }
        @Override public double getRate() { return hourlyRate; }
    }

    public static class CateringBooking extends BookingModel {
        private int guests;
        private double pricePerPlate;

        public CateringBooking(String id, String createdAt, String customer, String vendor,
                               String date, String status, int guests, double pricePerPlate) {
            super(id, createdAt, customer, vendor, date, status);
            this.guests = guests;
            this.pricePerPlate = pricePerPlate;
        }

        @Override
        public double calculateTotal() {
            double total = guests * pricePerPlate;
            return guests >= 200 ? total * 0.95 : total;   // 5% bulk discount
        }
        @Override public String getType() { return "CATERING"; }
        @Override public double getQuantity() { return guests; }
        @Override public double getRate() { return pricePerPlate; }
    }

    public static class PhotographyBooking extends BookingModel {
        private double hours;
        private double hourlyRate;
        private double albumFee;

        public PhotographyBooking(String id, String createdAt, String customer, String vendor,
                                  String date, String status, double hours,
                                  double hourlyRate, double albumFee) {
            super(id, createdAt, customer, vendor, date, status);
            this.hours = hours;
            this.hourlyRate = hourlyRate;
            this.albumFee = albumFee;
        }

        @Override public double calculateTotal() { return hours * hourlyRate + albumFee; }
        @Override public String getType() { return "PHOTOGRAPHY"; }
        @Override public double getQuantity() { return hours; }
        @Override public double getRate() { return hourlyRate; }
        @Override public double getExtra() { return albumFee; }
    }
}