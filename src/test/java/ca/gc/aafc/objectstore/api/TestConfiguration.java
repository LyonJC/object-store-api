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

import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.xmlpull.v1.XmlPullParserException;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.objectstore.api.file.FileMetaEntry;
import ca.gc.aafc.objectstore.api.file.FolderStructureStrategy;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.ResponseHeader;
import io.minio.Result;
import io.minio.ServerSideEncryption;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import io.minio.messages.Item;

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
  public static final UUID TEST_FILE_IDENTIFIER = UUID.randomUUID();
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
    } catch (InvalidEndpointException | InvalidPortException | InvalidKeyException
        | InvalidBucketNameException | NoSuchAlgorithmException | NoResponseException
        | ErrorResponseException | InternalException | InvalidArgumentException
        | InsufficientDataException | InvalidResponseException | IOException
        | XmlPullParserException e) {
      throw new RuntimeException("Can't setup Minio client for testing", e);
    }
  }
  
  private void setupFile(MinioClient minioClient)
      throws InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException,
      NoResponseException, ErrorResponseException, InternalException, InvalidArgumentException,
      InsufficientDataException, InvalidResponseException, IOException, XmlPullParserException {
    
    String testFile = "This is a test\n";
    InputStream is = new ByteArrayInputStream(
        testFile.getBytes(StandardCharsets.UTF_8));
    // since we are storing files directly (without the FRileController) we need to apply the folderStructureStrategy
    minioClient.putObject(TEST_BUCKET, folderStructureStrategy.getPathFor(TEST_FILE_IDENTIFIER + TEST_FILE_EXT).toString(), is, null,
        null, null, MediaType.TEXT_PLAIN_VALUE);
    
    FileMetaEntry fme = new FileMetaEntry(TEST_FILE_IDENTIFIER);
    fme.setEvaluatedFileExtension(TEST_FILE_EXT);
    fme.setReceivedMediaType(MediaType.TEXT_PLAIN_VALUE);
    fme.setDetectedMediaType(MediaType.TEXT_PLAIN_VALUE);
    fme.setSha1Hex("123");
    String jsonContent = OBJECT_MAPPER.writeValueAsString(fme);
    is = new ByteArrayInputStream(
        jsonContent.getBytes(StandardCharsets.UTF_8));
    minioClient.putObject(TEST_BUCKET, folderStructureStrategy.getPathFor(TEST_FILE_IDENTIFIER + FileMetaEntry.SUFFIX).toString(), is, null,
        null, null, MediaType.TEXT_PLAIN_VALUE);
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
    public void putObject(String bucketName, String objectName, InputStream stream, Long size,
        Map<String, String> headerMap, ServerSideEncryption sse, String contentType) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
        IOUtils.copy(stream, baos);
      } catch (IOException e) {
        e.printStackTrace();
      }
      INTERNAL_OBJECTS.put(bucketName + objectName, baos.toByteArray());
    }
    
    @Override
    public InputStream getObject(String bucketName, String objectName)
        throws InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException,
        IOException, InvalidKeyException, NoResponseException, XmlPullParserException,
        ErrorResponseException, InternalException, InvalidArgumentException,
        InvalidResponseException {
      
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
      try {
        Result<Item> result;
        if(bucketName.contains(ILLEGAL_BUCKET_CHAR)) {
          result = new Result<Item>(null, new InvalidBucketNameException(bucketName, "generated for testing purpose"));
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
            Item item = new Item(potentialKey.get(), false);
            result = new Result<Item>(item, null);
            iterator = Collections.singletonList(result).iterator();
          } else {
            iterator = Collections.emptyIterator();
          }  
        }
        
        final Iterator<Result<Item>> finalIterator = iterator;
        return () -> finalIterator;
      } catch (XmlPullParserException e) {
        throw new RuntimeException(e);
      }
    }
    
    @Override
    public ObjectStat statObject (String bucketName, String fileName) {
      ResponseHeader rh = new ResponseHeader();
      rh.setContentType(MediaType.TEXT_PLAIN_VALUE);
      rh.setLastModified("Tue, 15 Nov 1994 12:45:26 GMT");      
      return new ObjectStat(bucketName, fileName, rh, null);
    }
    
  }

}
