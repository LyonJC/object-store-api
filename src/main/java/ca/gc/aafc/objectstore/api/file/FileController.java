package ca.gc.aafc.objectstore.api.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.mime.MimeTypeException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.xmlpull.v1.XmlPullParserException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import ca.gc.aafc.objectstore.api.service.ObjectStoreMetadataReadService;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import io.minio.errors.RegionConflictException;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class FileController {

  public static final String DIGEST_ALGORITHM = "SHA-1";
  private static final int MAX_NUMBER_OF_ATTEMPT_RANDOM_UUID = 5;

  private final MinioFileService minioService;
  private final ObjectStoreMetadataReadService objectStoreMetadataReadService;
  private final MediaTypeDetectionStrategy mediaTypeDetectionStrategy;
  private final ObjectMapper objectMapper;

  @Inject
  public FileController(MinioFileService minioService, ObjectStoreMetadataReadService objectStoreMetadataReadService, 
      MediaTypeDetectionStrategy mediaTypeDetectionStrategy,
      Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder) {
    this.minioService = minioService;
    this.objectStoreMetadataReadService = objectStoreMetadataReadService;
    this.mediaTypeDetectionStrategy = mediaTypeDetectionStrategy;
    this.objectMapper = jackson2ObjectMapperBuilder.build();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @PostMapping("/file/{bucket}")
  public FileUploadResponse handleFileUpload(@RequestParam("file") MultipartFile file,
      @PathVariable String bucket) throws InvalidKeyException, NoSuchAlgorithmException,
      InvalidBucketNameException, NoResponseException, ErrorResponseException, InternalException,
      InvalidArgumentException, InsufficientDataException, InvalidResponseException,
      RegionConflictException, InvalidEndpointException, InvalidPortException, IOException,
      XmlPullParserException, URISyntaxException, MimeTypeException {
    
    // Temporary, we will need to check if the user is an admin
    minioService.ensureBucketExists(bucket);
    
    MediaTypeDetectionStrategy.MediaTypeDetectionResult mtdr = mediaTypeDetectionStrategy
        .detectMediaType(file.getInputStream(), file.getContentType(), file.getOriginalFilename());
    
    // Check that the UUID is not already assigned. It is very unlikely but not impossible
    UUID uuid = getNewUUID(bucket);

    FileMetaEntry fileMetaEntry = new FileMetaEntry(uuid);
    fileMetaEntry.setOriginalFilename(file.getOriginalFilename());
    fileMetaEntry.setReceivedMediaType(file.getContentType());
    
    fileMetaEntry.setDetectedMediaType(Objects.toString(mtdr.getDetectedMediaType()));
    fileMetaEntry.setDetectedFileExtension(mtdr.getDetectedMimeType().getExtension());
    
    fileMetaEntry.setEvaluatedMediaType(mtdr.getEvaluatedMediatype());
    fileMetaEntry.setEvaluatedFileExtension(mtdr.getEvaluatedExtension());
    
    // Decorate the InputStream in order to compute the hash
    MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM);
    DigestInputStream dis = new DigestInputStream(mtdr.getInputStream(), md);
    
    minioService.storeFile(uuid.toString() + mtdr.getEvaluatedExtension(), dis,
        mtdr.getEvaluatedMediatype(), bucket, null);
    
    String sha1Hex = DigestUtils.sha1Hex(md.digest());
    fileMetaEntry.setSha1Hex(sha1Hex);
    
    storeFileMetaEntry(fileMetaEntry, bucket);

    return new FileUploadResponse(uuid.toString(), mtdr.getEvaluatedMediatype().toString(),
        mtdr.getEvaluatedExtension(), file.getSize());
  }
  
  /**
   * Store a {@link FileMetaEntry} in Minio as a json file.
   * 
   * @param fileMetaEntry
   * @param bucket
   * @throws InvalidKeyException
   * @throws NoSuchAlgorithmException
   * @throws InvalidBucketNameException
   * @throws NoResponseException
   * @throws ErrorResponseException
   * @throws InternalException
   * @throws InvalidArgumentException
   * @throws InsufficientDataException
   * @throws InvalidResponseException
   * @throws RegionConflictException
   * @throws InvalidEndpointException
   * @throws InvalidPortException
   * @throws IOException
   * @throws XmlPullParserException
   * @throws URISyntaxException
   */
  private void storeFileMetaEntry(FileMetaEntry fileMetaEntry, String bucket)
      throws InvalidKeyException, NoSuchAlgorithmException, InvalidBucketNameException,
      NoResponseException, ErrorResponseException, InternalException, InvalidArgumentException,
      InsufficientDataException, InvalidResponseException, RegionConflictException,
      InvalidEndpointException, InvalidPortException, IOException, XmlPullParserException,
      URISyntaxException {
    
    String jsonContent = objectMapper.writeValueAsString(fileMetaEntry);
    InputStream inputStream = new ByteArrayInputStream(
        jsonContent.getBytes(StandardCharsets.UTF_8));
    minioService.storeFile(fileMetaEntry.getFileMetaEntryFilename().toString(), 
        inputStream, fileMetaEntry.getDetectedMediaType(), bucket, null);
  }
  
  /**
   * Triggers a download of a file. Note that the file requires a metadata entry in the database to
   * be available for download.
   * 
   * @param bucket
   * @param fileId
   * @return
   * @throws IOException
   */
  @GetMapping("/file/{bucket}/{fileId}")
  public ResponseEntity<InputStreamResource> downloadObject(@PathVariable String bucket,
      @PathVariable UUID fileId) throws IOException {
    
    try {
      Optional<ObjectStoreMetadata> loadedMetadata = objectStoreMetadataReadService
          .loadObjectStoreMetadataByFileId(fileId);
      ObjectStoreMetadata metadata = loadedMetadata
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
              "No metadata found for FileIdentifier " + fileId + " or bucket " + bucket, null));

      FileObjectInfo foi = minioService.getFileInfo(metadata.getFilename(), bucket)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
              fileId + " or bucket " + bucket + " Not Found", null));
      
      HttpHeaders respHeaders = new HttpHeaders();
      respHeaders.setContentType(org.springframework.http.MediaType.parseMediaType(metadata.getDcFormat()));
      respHeaders.setContentLength(foi.getLength());
      respHeaders.setContentDispositionFormData("attachment", metadata.getFilename());

      InputStream is = minioService.getFile(metadata.getFilename(), bucket).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
          "FileIdentifier " + fileId + " or bucket " + bucket + " Not Found", null));

      InputStreamResource isr = new InputStreamResource(is);
      return new ResponseEntity<InputStreamResource>(isr, respHeaders, HttpStatus.OK);
    } catch (IOException e) {
      log.warn("Can't download object", e);
    }
   
    throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR, null);
  }
  
  private UUID getNewUUID(String bucketName) throws IllegalStateException, IOException {
    int numberOfAttempt = 0;
    while (numberOfAttempt < MAX_NUMBER_OF_ATTEMPT_RANDOM_UUID) {
      UUID uuid = UUID.randomUUID();
      if(!minioService.isFileWithPrefixExists(bucketName, uuid.toString())) {
        return uuid;
      }
      numberOfAttempt++;
    }
    throw new IllegalStateException("Can't assign unique UUID. Giving up.");
  }

}
