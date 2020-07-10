package ca.gc.aafc.objectstore.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.xmlpull.v1.XmlPullParserException;

import ca.gc.aafc.objectstore.api.file.FileMetaEntry;
import ca.gc.aafc.objectstore.api.file.FolderStructureStrategy;
import ca.gc.aafc.objectstore.api.minio.MinioFileService;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.XmlParserException;
import io.minio.messages.Item;
import okhttp3.Headers;

/**
 * 
 * Configuration used to override bean in the context of Integration testing.
 * A MinioClient stub with 1 entry will be created for testing purpose (see {@link #setupFile(MinioClient)})
 *
 */
@Configuration
public class TestConfiguration {

  private final FolderStructureStrategy folderStructureStrategy = new FolderStructureStrategy();
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final String TEST_BUCKET = "test";
  public static final String TEST_USAGE_TERMS = "test usage terms";
  public static final UUID TEST_FILE_IDENTIFIER = UUID.randomUUID();
  public static final UUID TEST_THUMBNAIL_IDENTIFIER = UUID.randomUUID();
  public static final String TEST_FILE_EXT = ".txt";
  public static final String TEST_ORIGINAL_FILENAME = "myfile" + TEST_FILE_EXT;
  public static final String ILLEGAL_BUCKET_CHAR = "~";
  
  @Primary
  @Bean
  public MinioClient initMinioClient() {
    try {
      MinioClient minioClient = new MinioClientStub();
      setupFile(minioClient);
      return minioClient;
    } catch (InvalidKeyException | InvalidBucketNameException | NoSuchAlgorithmException
        | ErrorResponseException | InternalException | InsufficientDataException
        | InvalidResponseException | IOException | XmlPullParserException | InvalidEndpointException
        | InvalidPortException | IllegalArgumentException | XmlParserException e) {
      throw new RuntimeException("Can't setup Minio client for testing", e);
    }
  }
  
  private void setupFile(MinioClient minioClient) throws InvalidKeyException,
      InvalidBucketNameException, NoSuchAlgorithmException, ErrorResponseException,
      InternalException, InsufficientDataException, InvalidResponseException, IOException,
      XmlPullParserException, IllegalArgumentException, XmlParserException {

    String testFile = "This is a test\n";
    InputStream is = new ByteArrayInputStream(
        testFile.getBytes(StandardCharsets.UTF_8));
    
    storeTestObject(minioClient, TEST_FILE_IDENTIFIER, TEST_FILE_EXT, is, MediaType.TEXT_PLAIN_VALUE);    
  }
  
  /**
   * Store a test object using the provided minio client.
   * 
   * @param minioClient
   * @param id
   * @param objExt
   * @param objStream
   * @param mediaType
   * @throws InvalidKeyException
   * @throws InvalidBucketNameException
   * @throws NoSuchAlgorithmException
   * @throws NoResponseException
   * @throws ErrorResponseException
   * @throws InternalException
   * @throws InvalidArgumentException
   * @throws InsufficientDataException
   * @throws InvalidResponseException
   * @throws IOException
   * @throws XmlPullParserException
   * @throws XmlParserException
   * @throws IllegalArgumentException
   */
  private void storeTestObject(MinioClient minioClient, UUID id, String objExt,
      InputStream objStream, String mediaType) throws InvalidKeyException,
      InvalidBucketNameException, NoSuchAlgorithmException, ErrorResponseException,
      InternalException, InsufficientDataException, InvalidResponseException, IOException,
      XmlPullParserException, IllegalArgumentException, XmlParserException {
    minioClient.putObject(
      TEST_BUCKET,
      MinioFileService.toMinioObjectName(folderStructureStrategy.getPathFor(id + objExt)),
      objStream,
      null);

    FileMetaEntry fme = new FileMetaEntry(id);
    fme.setEvaluatedFileExtension(objExt);
    fme.setReceivedMediaType(mediaType);
    fme.setDetectedMediaType(mediaType);
    fme.setThumbnailIdentifier(TEST_THUMBNAIL_IDENTIFIER);
    fme.setSha1Hex("123");
    String jsonContent = OBJECT_MAPPER.writeValueAsString(fme);
    InputStream metaStream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
    minioClient.putObject(
      TEST_BUCKET,
      MinioFileService.toMinioObjectName(
        folderStructureStrategy.getPathFor(id + FileMetaEntry.SUFFIX)),
      metaStream,
      null);
  }
  
  /**
   * Stub used to replace MinioClient for testing.
   *
   */
  public static class MinioClientStub extends MinioClient {
    
    private final Map<String, byte[]> INTERNAL_OBJECTS = new HashMap<>();

    public MinioClientStub() throws InvalidEndpointException, InvalidPortException {
      super("localhost");
    }
    
    @Override
    public boolean bucketExists(String bucketName){
      return true;
    }
    
    @Override
    public void putObject(String bucketName, String objectName, InputStream stream, PutObjectOptions options) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
        IOUtils.copy(stream, baos);
      } catch (IOException e) {
        e.printStackTrace();
      }
      INTERNAL_OBJECTS.put(bucketName + objectName, baos.toByteArray());
    }
    
    @Override
    public InputStream getObject(String bucketName, String objectName) {
      return new ByteArrayInputStream(INTERNAL_OBJECTS.get(bucketName + objectName));
    }
    
    /**
     * If {@link TestConfiguration#ILLEGAL_BUCKET_CHAR} is present in the bucket name, {@link InvalidBucketNameException}
     * will be thrown. Otherwise, the item for {@link TestConfiguration#TEST_FILE_IDENTIFIER} will be returned.
     */
    @Override
    public Iterable<Result<Item>> listObjects(String bucketName, String prefix) {
      // Trying to mimic what Minio Java SDK will do.
      Iterator<Result<Item>> iterator = null;
        Result<Item> result;
        if(bucketName.contains(ILLEGAL_BUCKET_CHAR)) {
          result = new Result<Item>(new InvalidBucketNameException(bucketName, "generated for testing purpose"));
          iterator = new Iterator<Result<Item>>() {

            @Override
            public boolean hasNext() {
              return false;
            }

            @Override
            public Result<Item> next() {
              return result;
            }
          };
        }
        else {
          Optional<String> potentialKey = INTERNAL_OBJECTS.keySet().stream()
              .filter(key -> key.startsWith(prefix)).findFirst();
          if (potentialKey.isPresent()) {
            Item item = new Item(potentialKey.get());
            result = new Result<Item>(item);
            iterator = Collections.singletonList(result).iterator();
          } else {
            iterator = Collections.emptyIterator();
          }  
        }
        
        final Iterator<Result<Item>> finalIterator = iterator;
        return () -> finalIterator;
      }

      @Override
      public ObjectStat statObject(String bucketName, String objectName) {
        Headers head = Headers.of(
          ImmutableMap.of(
            "Content-Type", MediaType.TEXT_PLAIN_VALUE,
            "Last-Modified", "Tue, 15 Nov 1994 12:45:26 GMT",
            "Content-Length","1234"));
        return new ObjectStat(bucketName, objectName, head);
      }
    }

}
