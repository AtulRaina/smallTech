package functions;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import org.threeten.bp.Duration;
import functions.eventpojos.GcsEvent;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import com.google.api.gax.batching.BatchingSettings;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.nio.file.Paths;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.util.Arrays;
import java.util.List;

import java.util.concurrent.TimeUnit;

public class PubSubHelper {
    private static final Logger logger = Logger.getLogger(PubSubHelper.class.getName());
    private String projectId;
    private String topicId;
    private TopicName topicName = null;
    private Publisher  publisher = null;
    private PropertiesLoader propertiesLoader= new PropertiesLoader();
    public PubSubHelper() throws IOException {


        this.topicName = TopicName.of(propertiesLoader.prop.getProperty("project"), propertiesLoader.prop.getProperty("topic"));
        long requestBytesThreshold = 5000L; // default : 1 byte
        long messageCountBatchSize = 100; // default : 1 message

        Duration publishDelayThreshold = Duration.ofSeconds(1); // default : 1 ms

        // Publish request get triggered based on request size, messages count & time since last
        // publish, whichever condition is met first.
        BatchingSettings batchingSettings =
                BatchingSettings.newBuilder()
                        .setElementCountThreshold(messageCountBatchSize)
                        .setRequestByteThreshold(requestBytesThreshold)
                        .setDelayThreshold(publishDelayThreshold)
                        .build();
        publisher = Publisher.newBuilder(topicName).setBatchingSettings(batchingSettings).build();
    }

    public Boolean writeMessagePubSub(String message) throws IOException, InterruptedException {

        // publish message which is the result of the query
        // TO DO make it class specific
        // publish the result of the query to the pubsub topic


        try {
            // Create a publisher instance with default settings bound to the topic


            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            // Once published, returns a server-assigned message id (unique within the topic)
            ApiFuture<String> future = publisher.publish(pubsubMessage);




        } finally {
        }
            // Wait on any pending publish requests.




            return true;


        }




     public void closePublisher() throws InterruptedException {
         if (publisher != null) {
             // When finished with the publisher, shutdown to free up resources.
             logger.info("Shutting Down Pubsub");
             publisher.shutdown();
             publisher.awaitTermination(1, TimeUnit.MINUTES);
         }
     }


}
