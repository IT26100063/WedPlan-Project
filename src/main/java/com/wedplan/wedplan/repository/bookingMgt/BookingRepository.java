package com.wedplan.wedplan.repository.bookingMgt;

import com.wedplan.wedplan.model.bookingMgt.BookingModel.VenueBooking;
import com.wedplan.wedplan.model.bookingMgt.BookingModel.CateringBooking;
import com.wedplan.wedplan.model.bookingMgt.BookingModel.PhotographyBooking;
import com.wedplan.wedplan.model.bookingMgt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import com.wedplan.wedplan.model.bookingMgt.BookingModel;
import com.wedplan.wedplan.model.bookingMgt.BookingModel.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Repository
public class BookingRepository {
    private final Path filePath;

    public BookingRepository(@Value("${wedplan.data.bookings:data/bookings.txt}") String path) {
        this.filePath = Paths.get(path);
        try {
            if (filePath.getParent() != null) Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath)) Files.createFile(filePath);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot initialise " + path, e);
        }
    }

    // READ
    public synchronized List<BookingModel> findAll() {
        List<BookingModel> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                BookingModel b = parse(line);
                if (b != null) list.add(b);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return list;
    }

    public synchronized Optional<BookingModel> findById(String id) {
        return findAll().stream().filter(b -> b.getId().equals(id)).findFirst();
    }

    // CREATE
    public synchronized void save(BookingModel booking) {
        List<BookingModel> all = findAll();
        all.add(booking);
        writeAll(all);
    }

    // UPDATE
    public synchronized boolean update(BookingModel updated) {
        List<BookingModel> all = findAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(updated.getId())) {
                all.set(i, updated);
                writeAll(all);
                return true;
            }
        }
        return false;
    }

    // DELETE
    public synchronized boolean deleteById(String id) {
        List<BookingModel> all = findAll();
        boolean removed = all.removeIf(b -> b.getId().equals(id));
        if (removed) writeAll(all);
        return removed;
    }

    private void writeAll(List<BookingModel> list) {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            for (BookingModel b : list) {
                bw.write(b.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Converts one line back into the correct subclass (polymorphism)
    private BookingModel parse(String line) {
        String[] p = line.split("\\|");
        if (p.length < 10) return null;
        String id = p[1], created = p[2], cust = p[3], vendor = p[4], date = p[5], status = p[6];
        double qty = Double.parseDouble(p[7]);
        double rate = Double.parseDouble(p[8]);
        double extra = Double.parseDouble(p[9]);

        return switch (p[0]) {
            case "VENUE"       -> new VenueBooking(id, created, cust, vendor, date, status, qty, rate);
            case "CATERING"    -> new CateringBooking(id, created, cust, vendor, date, status, (int) qty, rate);
            case "PHOTOGRAPHY" -> new PhotographyBooking(id, created, cust, vendor, date, status, qty, rate, extra);
            default -> null;
        };
    }
}