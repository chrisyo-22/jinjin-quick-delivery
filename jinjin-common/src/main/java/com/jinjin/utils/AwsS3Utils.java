package com.jinjin.utils;

import com.jinjin.properties.AwsS3Properties;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.FileUpload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
public class AwsS3Utils {

//    public static final String BUCKET_NAME = "amz-chris-yo-22-wode-mingzi-hello-world";
    private static final AwsS3Properties awsS3Properties;
    static{
        awsS3Properties = new AwsS3Properties();
    }
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    public static final S3AsyncClient s3AsyncClient = S3AsyncClient.builder().multipartEnabled(true).build();
    static final S3TransferManager transferManager = S3TransferManager.builder()
            .s3Client(s3AsyncClient)
            .build();
    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();



    public String upload(byte[] bytes, String objectName) {
        if (bytes == null) throw new IllegalArgumentException("bytes must not be null");
        if (objectName == null || objectName.isEmpty()) throw new IllegalArgumentException("objectName must not be null/empty");

        try {
            // Build async request body from bytes
            AsyncRequestBody body = AsyncRequestBody.fromBytes(bytes);

            // Put object (async) and block for completion to keep behavior consistent with other methods
            CompletableFuture<PutObjectResponse> responseFuture = s3AsyncClient.putObject(
                    r -> r.bucket(awsS3Properties.getBucketName())
                            .key(objectName)
                            .contentLength((long) bytes.length),
                    body
            ).exceptionally(e -> {
                log.error("S3 putObject failed for key [{}]: {}", objectName, e == null ? "null" : e.getMessage(), e);
                return null;
            });

            PutObjectResponse putResponse = responseFuture.join(); // block here

            if (putResponse == null) {
                throw new RuntimeException("S3 upload failed for key: " + objectName);
            }

            log.info("Uploaded object [{}] to bucket [{}] (ETag={})",
                    objectName, awsS3Properties.getBucketName(), putResponse.eTag());

            // Build a durable public URL for the object.
            // We URL-encode each path segment to handle spaces/special chars but preserve slashes.
            String bucket = awsS3Properties.getBucketName();
            String region = awsS3Properties.getRegion();

            // Safe encode path segments while preserving '/'
            String[] segments = objectName.split("/");
            StringBuilder encodedKey = new StringBuilder();
            for (int i = 0; i < segments.length; i++) {
                if (i > 0) encodedKey.append('/');
                String seg = segments[i];
                // URLEncoder encodes spaces to '+'; replace '+' with %20 which is preferable in URLs.
                String enc = java.net.URLEncoder.encode(seg, StandardCharsets.UTF_8)
                        .replace("+", "%20");
                // URLEncoder also encodes '*' etc; that's okay for S3 object keys in URL form.
                encodedKey.append(enc);
            }

            // Construct URL. For standard S3 virtual-hosted–style endpoint:
            // https://{bucket}.s3.{region}.amazonaws.com/{key}
            String url;
            if (region == null || region.isBlank() || region.equals("us-east-1")) {
                // us-east-1 historically can omit region in the endpoint, but the regional form is fine too.
                url = String.format("https://%s.s3.amazonaws.com/%s", bucket, encodedKey.toString());
            } else {
                url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, encodedKey.toString());
            }

            return url;

        } catch (CompletionException ce) {
            log.error("Upload interrupted or failed for [{}]: {}", objectName, ce.getMessage(), ce);
            throw new RuntimeException("Failed to upload " + objectName, ce);
        } catch (RuntimeException re) {
            log.error("Runtime exception while uploading [{}]: {}", objectName, re.getMessage(), re);
            throw re;
        } catch (Exception ex) {
            // URLEncoder throws UnsupportedEncodingException in some older APIs; catch general Exception to be safe
            log.error("Unexpected error uploading [{}]: {}", objectName, ex.getMessage(), ex);
            throw new RuntimeException("Unexpected error uploading " + objectName, ex);
        }
    }

    public String asyncUpload_multipart_stream(String key, InputStream inputStream) {

        AsyncRequestBody body = AsyncRequestBody.fromInputStream(inputStream, null, executor); // 'null' indicates that the
        // content length is unknown.
        CompletableFuture<PutObjectResponse> responseFuture =
                s3AsyncClient.putObject(r -> r.bucket(awsS3Properties.getBucketName()).key(key), body)
                        .exceptionally(e -> {
                            if (e != null) {
                                log.error(e.getMessage(), e);
                            }
                            return null;
                        });

        PutObjectResponse response = responseFuture.join(); // Wait for the response.
        System.out.println("Response: " + response);
        executor.shutdown();
        return key;
    }

    public String UploadFile(String key, URI filePathURI) {

        UploadFileRequest uploadFileRequest = UploadFileRequest.builder()
                .putObjectRequest(b -> b.bucket(awsS3Properties.getBucketName()).key(key))
                .addTransferListener(LoggingTransferListener.create())  // Add listener.
                .source(Paths.get(filePathURI))
                .build();

        FileUpload fileUpload = transferManager.uploadFile(uploadFileRequest);

        fileUpload.completionFuture().join();
        return key;

    }

    /* Create a pre-signed URL to download an object in a subsequent GET request. */
    public String createPresignedGetUrl(String keyName) {
        try (S3Presigner presigner = S3Presigner.create()) {

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(awsS3Properties.getBucketName())
                    .key(keyName)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))  // The URL will expire in 10 minutes.
                    .getObjectRequest(objectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            log.info("Presigned URL: [{}]", presignedRequest.url().toString());
            log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

            return presignedRequest.url().toExternalForm();
        }
    }


    /* Create a presigned URL to use in a subsequent PUT request */
    public String createPresignedGetUrl(String keyName, Map<String, String> metadata) {
        try (S3Presigner presigner = S3Presigner.create()) {

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(awsS3Properties.getBucketName())
                    .key(keyName)
                    .metadata(metadata)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))  // The URL expires in 10 minutes.
                    .putObjectRequest(objectRequest)
                    .build();


            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            String myURL = presignedRequest.url().toString();
            log.info("Presigned URL to upload a file to: [{}]", myURL);
            log.info("HTTP method: [{}]", presignedRequest.httpRequest().method());

            return presignedRequest.url().toExternalForm();
        }
    }


}
