/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package functions;

// [START functions_helloworld_storage]
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import functions.eventpojos.GcsEvent;
import java.util.logging.Logger;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import java.util.UUID;
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
import java.util.Arrays;
import java.util.List;
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
import java.util.ArrayList;

public class HelloGcs implements BackgroundFunction<GcsEvent> {
  private static final Logger logger = Logger.getLogger(HelloGcs.class.getName());



  @Override
  public void accept(GcsEvent event, Context context) throws Exception {
    logger.info("Event: " + context.eventId());
    logger.info("Event Type: " + context.eventType());
    logger.info("Bucket: " + event.getBucket());
    logger.info("File: " + event.getName());
    logger.info("Metageneration: " + event.getMetageneration());
    logger.info("Created: " + event.getTimeCreated());
    logger.info("Updated: " + event.getUpdated());
    read_execute_query();
  }

  public void read_execute_query() throws Exception
  {
    List<String> eventQueries=queries();
    for(String eventQuery:eventQueries)
    {
      logger.info("Executing Query: "+ eventQuery);
      run_query(eventQuery);
    }
  }

  public void run_query(String querytoRun)  throws Exception
  {
    BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
    QueryJobConfiguration queryConfig =
            QueryJobConfiguration.newBuilder(
                    querytoRun)
                    // Use standard SQL syntax for queries.
                    // See: https://cloud.google.com/bigquery/sql-reference/
                    .setUseLegacySql(false)
                    .build();

    // Create a job ID so that we can safely retry.
    JobId jobId = JobId.of(UUID.randomUUID().toString());
    Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());

    // Wait for the query to complete.
    queryJob = queryJob.waitFor();

    // Check for errors


    // Get the results.
    TableResult result = queryJob.getQueryResults();
    logger.info("Event: " + "Biq query is executed");
    // Print all pages of the results.
    for (FieldValueList row : result.iterateAll()) {
      // String type
      String commit = row.get("Totalsales").getStringValue();
      // Record type
      publishWithErrorHandlerExample("lyrical-amulet-308012","totalSales",commit);

    }


  }

  public static void publishWithErrorHandlerExample(String projectId, String topicId,String message)
          throws IOException, InterruptedException {
    TopicName topicName = TopicName.of(projectId, topicId);
    Publisher publisher = null;

    try {
      // Create a publisher instance with default settings bound to the topic
      publisher = Publisher.newBuilder(topicName).build();

      List<String> messages = Arrays.asList("first message", "second message");


        ByteString data = ByteString.copyFromUtf8(message);
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

        // Once published, returns a server-assigned message id (unique within the topic)
        ApiFuture<String> future = publisher.publish(pubsubMessage);

        // Add an asynchronous callback to handle success / failure
        ApiFutures.addCallback(
                future,
                new ApiFutureCallback<String>() {

                  @Override
                  public void onFailure(Throwable throwable) {
                    if (throwable instanceof ApiException) {
                      ApiException apiException = ((ApiException) throwable);
                      // details on the API exception
                      System.out.println(apiException.getStatusCode().getCode());
                      System.out.println(apiException.isRetryable());
                    }
                    System.out.println("Error publishing message : " + message);
                  }

                  @Override
                  public void onSuccess(String messageId) {
                    // Once published, returns server-assigned message ids (unique within the topic)
                    System.out.println("Published message ID: " + messageId);
                  }
                },
                MoreExecutors.directExecutor());

    } finally {
      if (publisher != null) {
        // When finished with the publisher, shutdown to free up resources.
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
      }
    }
  }



   public static List<String> queries()
   {
     List<String> queryList = new ArrayList<String>();
     Storage storage = StorageOptions.newBuilder().setProjectId("lyrical-amulet-308012").build().getService();
     Page<Blob> blobs = storage.list("cfquery");

     for (Blob blob : blobs.iterateAll()) {
       String fileContent = new String(blob.getContent());
       queryList.add(fileContent);
     }

     return queryList;

   }


}



// [END functions_helloworld_storage]
