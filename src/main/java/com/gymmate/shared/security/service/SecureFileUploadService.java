package com.gymmate.shared.security.service;

import com.gymmate.shared.constants.FileUploadContext;
import com.gymmate.shared.dto.FileValidationResult;
import com.gymmate.shared.dto.SecureFileResult;
import com.gymmate.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SecureFileUploadService {

  private final InputSanitizationService sanitizationService;
  private final SecurityAuditService auditService;

  // Allowed file types
  private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
    "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
  );

  private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of(
    "application/pdf", "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
  );

  private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
    ".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf", ".doc", ".docx", ".xls", ".xlsx"
  );

  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
  private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
    ".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".vbs", ".js", ".jar", ".php", ".asp", ".jsp"
  );

  public FileValidationResult validateFile(MultipartFile file, FileUploadContext context) {
    List<String> errors = new ArrayList<>();

    // Check file is not empty
    if (file.isEmpty()) {
      errors.add("File is empty");
      return new FileValidationResult(false, errors);
    }

    // Sanitize filename
    String originalFilename = file.getOriginalFilename();
    String sanitizedFilename = sanitizationService.sanitizeFilename(originalFilename);

    if (sanitizedFilename == null || sanitizedFilename.isEmpty()) {
      errors.add("Invalid filename");
    }

    // Check file extension
    String fileExtension = getFileExtension(sanitizedFilename).toLowerCase();
    if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
      errors.add("File type not allowed: " + fileExtension);
    }

    // Check for dangerous extensions
    if (DANGEROUS_EXTENSIONS.contains(fileExtension)) {
      errors.add("Dangerous file type detected");
    }

    // Check file size
    if (file.getSize() > MAX_FILE_SIZE) {
      errors.add("File size exceeds maximum allowed size");
    }

    // Check MIME type
    String mimeType = file.getContentType();
    if (!isAllowedMimeType(mimeType, context)) {
      errors.add("MIME type not allowed: " + mimeType);
    }

    // Validate file content (magic bytes)
    try {
      if (!validateFileContent(file.getBytes(), mimeType)) {
        errors.add("File content does not match declared type");
      }
    } catch (IOException e) {
      errors.add("Failed to read file content");
    }

    // Scan for malware (simplified - integrate with actual scanner in production)
    if (containsMalwareSignatures(file)) {
      errors.add("File contains potentially malicious content");
    }

    boolean isValid = errors.isEmpty();
    if (!isValid) {
      log.warn("File upload validation failed: {}", String.join(", ", errors));
    }

    return new FileValidationResult(isValid, errors);
  }

  private String getFileExtension(String filename) {
    if (filename == null) return "";
    int lastDot = filename.lastIndexOf('.');
    return lastDot > 0 ? filename.substring(lastDot) : "";
  }

  private boolean isAllowedMimeType(String mimeType, FileUploadContext context) {
    switch (context) {
      case PROFILE_IMAGE:
        return ALLOWED_IMAGE_TYPES.contains(mimeType);
      case DOCUMENT:
        return ALLOWED_DOCUMENT_TYPES.contains(mimeType);
      default:
        return ALLOWED_IMAGE_TYPES.contains(mimeType) ||
          ALLOWED_DOCUMENT_TYPES.contains(mimeType);
    }
  }

  private boolean validateFileContent(byte[] content, String declaredMimeType) {
    // Magic byte validation
    if (content.length < 4) return false;

    String actualMimeType = detectMimeTypeFromBytes(content);
    return actualMimeType.equals(declaredMimeType);
  }

  private String detectMimeTypeFromBytes(byte[] content) {
    // Simplified magic byte detection
    if (content.length >= 4) {
      // JPEG
      if (content[0] == (byte)0xFF && content[1] == (byte)0xD8 && content[2] == (byte)0xFF) {
        return "image/jpeg";
      }
      // PNG
      if (content[0] == (byte)0x89 && content[1] == 0x50 && content[2] == 0x4E && content[3] == 0x47) {
        return "image/png";
      }
      // GIF
      if (content[0] == 0x47 && content[1] == 0x49 && content[2] == 0x46) {
        return "image/gif";
      }
      // PDF
      if (content.length >= 5 &&
        new String(content, 0, 5).equals("%PDF-")) {
        return "application/pdf";
      }
    }

    return "application/octet-stream";
  }

  private boolean containsMalwareSignatures(MultipartFile file) {
    // Simplified malware detection
    // In production, integrate with actual antivirus scanner like ClamAV
    try {
      String content = new String(file.getBytes(), StandardCharsets.UTF_8);

      // Check for common malware patterns
      return content.toLowerCase().contains("<script") ||
        content.toLowerCase().contains("eval(") ||
        content.toLowerCase().contains("document.cookie") ||
        content.toLowerCase().contains("system(") ||
        content.toLowerCase().contains("exec(");
    } catch (Exception e) {
      log.debug("Failed to scan file for malware", e);
      return false;
    }
  }

  public SecureFileResult saveFileSecurely(MultipartFile file, FileUploadContext context, UUID userId) {
    // Validate file first
    FileValidationResult validation = validateFile(file, context);
    if (!validation.valid()) {
      throw new BadRequestException("File validation failed: " + String.join(", ", validation.errors()));
    }

    try {
      // Generate secure filename
      String originalFilename = sanitizationService.sanitizeFilename(file.getOriginalFilename());
      String fileExtension = getFileExtension(originalFilename);
      String secureFilename = UUID.randomUUID().toString() + fileExtension;

      // Create upload directory with proper permissions
      Path uploadDir = Paths.get("./uploads", context.name().toLowerCase());
      Files.createDirectories(uploadDir);

      // Save file with restricted permissions
      Path filePath = uploadDir.resolve(secureFilename);
      Files.write(filePath, file.getBytes());

      // Set file permissions (owner read/write only)
      Files.setPosixFilePermissions(filePath,
        PosixFilePermissions.fromString("rw-------"));

      // Log successful upload
      auditService.logDataAccess(userId, null, null,
        "FILE_UPLOAD", "Uploaded file: " + originalFilename);

      return new SecureFileResult(secureFilename, filePath.toString(), file.getSize());

    } catch (IOException e) {
      log.error("Failed to save file securely", e);
      throw new RuntimeException("Failed to save file", e);
    }
  }
}
