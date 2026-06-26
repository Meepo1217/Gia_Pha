package model;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Singleton Service giao tiếp Cloudflare R2 Object Storage chuẩn S3.
 * Hỗ trợ lưu trữ đa phương tiện: Ảnh chân dung, Video gia đình, Tài liệu PDF/Word.
 */
public class CloudflareStorageService {
    private static CloudflareStorageService instance;
    private final Properties props = new Properties();

    private CloudflareStorageService() {
        try {
            File localConfig = new File("src/config.properties");
            if (localConfig.exists()) {
                try (InputStream fis = new java.io.FileInputStream(localConfig)) {
                    props.load(fis);
                }
            } else {
                try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
                    if (input != null) props.load(input);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải config.properties: " + e.getMessage());
        }
    }

    public static synchronized CloudflareStorageService getInstance() {
        if (instance == null) instance = new CloudflareStorageService();
        return instance;
    }

    public String uploadImage(File fileAnh) {
        return uploadFile(fileAnh, "avatars");
    }

    /**
     * Tải file đa phương tiện bất kỳ (Ảnh, Video, PDF, DOCX) lên Cloudflare R2.
     * @param file File từ máy tính cục bộ
     * @param thưMục Prefix thư mục phân loại trên Cloud (ví dụ: "videos", "docs", "avatars")
     * @return Link URL công khai CDN
     */
    public String uploadFile(File file, String thưMục) {
        String accessKey = props.getProperty("r2.access_key", "").trim();
        String secretKey = props.getProperty("r2.secret_key", "").trim();
        String endpoint = props.getProperty("r2.endpoint", "").trim();
        String publicUrl = props.getProperty("r2.public_url", "").trim();
        String bucket = props.getProperty("r2.bucket", "giapha").trim();

        if (accessKey.isEmpty() || accessKey.contains("DIEN_")) {
            System.err.println("❌ Lỗi: Chưa cấu hình Access Key Cloudflare R2 trong config.properties");
            return null;
        }

        try {
            byte[] bodyBytes = Files.readAllBytes(file.toPath());
            String ext = file.getName().contains(".") ? file.getName().substring(file.getName().lastIndexOf(".")) : ".dat";
            String prefix = (thưMục != null && !thưMục.isEmpty()) ? thưMục + "/" : "";
            String objectKey = prefix + "res_" + UUID.randomUUID().toString().replace("-", "") + ext;

            URI endpointUri = URI.create(endpoint);
            String host = endpointUri.getHost();
            String path = "/" + bucket + "/" + objectKey;
            String fullUri = endpoint + path;

            SimpleDateFormat amzDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
            amzDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String xAmzDate = amzDateFormat.format(new Date());
            String dateStamp = xAmzDate.substring(0, 8);

            String contentSha256 = sha256Hex(bodyBytes);
            String region = "auto";
            String service = "s3";

            String canonicalHeaders = "host:" + host + "\n" + "x-amz-content-sha256:" + contentSha256 + "\n" + "x-amz-date:" + xAmzDate + "\n";
            String signedHeaders = "host;x-amz-content-sha256;x-amz-date";
            String canonicalRequest = "PUT\n" + path + "\n\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + contentSha256;

            String credentialScope = dateStamp + "/" + region + "/" + service + "/aws4_request";
            String stringToSign = "AWS4-HMAC-SHA256\n" + xAmzDate + "\n" + credentialScope + "\n" + sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));

            byte[] kSecret = ("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8);
            byte[] kDate = hmacSha256(kSecret, dateStamp);
            byte[] kRegion = hmacSha256(kDate, region);
            byte[] kService = hmacSha256(kRegion, service);
            byte[] kSigning = hmacSha256(kService, "aws4_request");
            String signature = hexEncode(hmacSha256(kSigning, stringToSign));

            String authorizationHeader = "AWS4-HMAC-SHA256 Credential=" + accessKey + "/" + credentialScope + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUri))
                    .header("x-amz-date", xAmzDate)
                    .header("x-amz-content-sha256", contentSha256)
                    .header("Authorization", authorizationHeader)
                    .header("Content-Type", guessContentType(ext))
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(bodyBytes))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String cdnUrl = publicUrl.endsWith("/") ? publicUrl + objectKey : publicUrl + "/" + objectKey;
                System.out.println("✔ Upload thành công file lên R2: " + cdnUrl);
                return cdnUrl;
            } else {
                System.err.println("❌ Lỗi upload R2 HTTP " + response.statusCode() + ": " + response.body());
                return null;
            }

        } catch (Exception e) {
            System.err.println("❌ Ngoại lệ upload R2: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static String sha256Hex(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return hexEncode(digest.digest(data));
    }

    private static byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String hexEncode(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String guessContentType(String ext) {
        if (".png".equalsIgnoreCase(ext)) return "image/png";
        if (".gif".equalsIgnoreCase(ext)) return "image/gif";
        if (".webp".equalsIgnoreCase(ext)) return "image/webp";
        if (".mp4".equalsIgnoreCase(ext)) return "video/mp4";
        if (".mov".equalsIgnoreCase(ext)) return "video/quicktime";
        if (".avi".equalsIgnoreCase(ext)) return "video/x-msvideo";
        if (".pdf".equalsIgnoreCase(ext)) return "application/pdf";
        if (".doc".equalsIgnoreCase(ext)) return "application/msword";
        if (".docx".equalsIgnoreCase(ext)) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return "application/octet-stream";
    }
}
