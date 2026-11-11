package com.wedplan.wedplan.services.paymentMgt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;

@Component
public class ReceiptStorage {
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;
    private final Path storageDirectory;

    public ReceiptStorage(@Value("${wedplan.receipt.storage-dir:${user.home}/wedplan-uploads}") String directory) {
        storageDirectory = Path.of(directory).toAbsolutePath().normalize();
    }

    public StoredReceipt store(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE) {
            throw new PaymentWorkflowException("Choose a non-empty receipt file smaller than 5 MB.");
        }

        String extension = extension(file.getOriginalFilename());
        byte[] contents;
        try {
            contents = file.getBytes();
            if (!matchesFileSignature(extension, contents)) {
                throw new PaymentWorkflowException("Upload a valid PDF, JPEG, or PNG receipt.");
            }
            Files.createDirectories(storageDirectory);
            String key = UUID.randomUUID() + extension;
            Files.write(storageDirectory.resolve(key), contents, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return new StoredReceipt(key, cleanOriginalName(file.getOriginalFilename()), contentType(extension));
        } catch (IOException exception) {
            throw new PaymentWorkflowException("The receipt could not be stored. Please try again.", exception);
        }
    }

    public ReceiptFile load(String key, String originalName) {
        Path file = resolveKey(key);
        if (!Files.isRegularFile(file)) {
            throw new PaymentWorkflowException("The stored receipt file is unavailable.");
        }
        return new ReceiptFile(new FileSystemResource(file), contentType(extension(key)), originalName);
    }

    public void delete(String key) {
        if (key == null) {
            return;
        }
        try {
            Files.deleteIfExists(resolveKey(key));
        } catch (IOException exception) {
            throw new PaymentWorkflowException("The previous receipt file could not be removed.", exception);
        }
    }

    private Path resolveKey(String key) {
        if (key == null || !key.matches("[0-9a-fA-F-]{36}\\.(pdf|jpg|jpeg|png)")) {
            throw new PaymentWorkflowException("Invalid receipt reference.");
        }
        Path file = storageDirectory.resolve(key).normalize();
        if (!file.startsWith(storageDirectory)) {
            throw new PaymentWorkflowException("Invalid receipt reference.");
        }
        return file;
    }

    private String extension(String fileName) {
        if (fileName == null) {
            throw new PaymentWorkflowException("Receipt file name is missing.");
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            throw new PaymentWorkflowException("Use a PDF, JPEG, or PNG receipt.");
        }
        String extension = fileName.substring(dot).toLowerCase(Locale.ROOT);
        if (!extension.matches("\\.(pdf|jpg|jpeg|png)")) {
            throw new PaymentWorkflowException("Use a PDF, JPEG, or PNG receipt.");
        }
        return extension;
    }

    private boolean matchesFileSignature(String extension, byte[] bytes) {
        return switch (extension) {
            case ".pdf" -> startsWith(bytes, new byte[]{'%', 'P', 'D', 'F', '-'});
            case ".png" -> startsWith(bytes, new byte[]{(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10});
            case ".jpg", ".jpeg" -> bytes.length >= 3
                    && bytes[0] == (byte) 0xff && bytes[1] == (byte) 0xd8 && bytes[2] == (byte) 0xff;
            default -> false;
        };
    }

    private boolean startsWith(byte[] bytes, byte[] prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if (bytes[index] != prefix[index]) {
                return false;
            }
        }
        return true;
    }

    private String cleanOriginalName(String name) {
        String baseName = Path.of(name).getFileName().toString().replaceAll("[\\r\\n\\t\"]", "_");
        return baseName.length() > 255 ? baseName.substring(baseName.length() - 255) : baseName;
    }

    private String contentType(String extension) {
        return switch (extension.toLowerCase(Locale.ROOT)) {
            case ".pdf" -> "application/pdf";
            case ".png" -> "image/png";
            default -> "image/jpeg";
        };
    }

    public record StoredReceipt(String key, String originalName, String contentType) {
    }
}
