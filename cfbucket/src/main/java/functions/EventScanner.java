package functions;
import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.FieldValueList;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class EventScanner {

    private static final Logger logger = Logger.getLogger(EventScanner.class.getName());
    private final String receivedTriggerFile;
    PubSubHelper pubSubHelper=null;
    Storage storage;
    Page<Blob> blobs;



    public EventScanner(String triggerFile) throws IOException, InterruptedException {
        this.storage = StorageOptions.newBuilder().setProjectId(new PropertiesLoader().prop.getProperty("project")).build().getService();
        this.blobs = storage.list(new PropertiesLoader().prop.getProperty("configStorageBucket"));
        this.receivedTriggerFile=triggerFile;
        if(this.scanEvent())
        {
        logger.info("Expected event executed ");
        }

    }


    private boolean scanEvent() throws IOException, InterruptedException {
        logger.info("Scanning events....");
        List<String> expectedTriggers;
        List<String>receivedTriggerList= Arrays.asList(receivedTriggerFile.split("/"));


        for(String config:this.loadConfig()) {
            //logger.info("Scanning Configuration........"+config);

            if (config.contains(",")) {
                //logger.info("Configuration Contains ,");
                expectedTriggers=  Arrays.asList(config.split("\\|")[0].split(","));

            } else {
               // logger.info("Configuration does not contain , ");
                expectedTriggers = Collections.singletonList(config.split("\\|")[0]);
            }
           //logger.info("Received Trigger List: "+receivedTriggerList.toString());
           // logger.info("Expected Trigger List: "+ expectedTriggers.toString());

            // if the event is found then only set the folder path
            if (receivedTriggerList.containsAll(expectedTriggers)) {
              //  logger.info("=========Trigger match===========");
                String queryFolder = config.split("\\|")[1];
               // logger.info("Folder to pick queries is :"+ queryfolder);

                List<String> queriesToExecute = this.getQueries(queryFolder);
                logger.info("Total Query files for execution: "+ queriesToExecute.size());
                //logger.info("Trigger File found: "+ receivedTriggerFile);
             if(queriesToExecute.size()>0) {
                 this.ExecuteQueries(queriesToExecute);
             }
             return true;
            }
        }
        return false;
    }

    private void ExecuteQueries(List<String> queriesToExecute) throws IOException, InterruptedException {
        QueryExecution queryExecution= new QueryExecution();
                            pubSubHelper= new PubSubHelper();

        for(String eventQuery:queriesToExecute)
        {
           //logger.info("Executing Query: "+ eventQuery);
            TableResult result=queryExecution.execute(eventQuery);
            logger.info("Query returned rows:"+result.getTotalRows());
            if(result.getTotalRows()>0) {
                for (FieldValueList row : result.iterateAll()) {
                    // String type
                    String commit = row.get("Totalsales").getStringValue();
                    // Record type
                    pubSubHelper.writeMessagePubSub(commit);

                }
            }
        }
        pubSubHelper.closePublisher();

    }

    private List<String> getQueries(String targetPattern) {
        List<String> expectedEventQueryList = new ArrayList<>();

        for (Blob blob : this.blobs.iterateAll()) {
            //logger.info("Scanning blob............"+blob.getName());
            if (blob.getName().toLowerCase().contains(targetPattern.trim().toLowerCase())) {
               // logger.info("Reading query as it match pattern : "+blob.getName());
                String fileContent = new String(blob.getContent());
                if (fileContent.length() > 0) {
                    expectedEventQueryList.add(fileContent);
                }
            }

        }
        return expectedEventQueryList;
    }

    private List<String> loadConfig()
    {
        logger.info("Loding config......");
        List<String> expectedEventConfigList = new ArrayList<>();

        try {

            for (Blob blob : blobs.iterateAll()) {
                // logger.info(blob.getName());
                if(blob.getName().contains(new PropertiesLoader().prop.getProperty("configFilePath"))) {
                    String fileContent = new String(blob.getContent());
                    expectedEventConfigList = Arrays.asList(fileContent.split("\n"));
                    //   logger.info("Total expected trigger found"+ queryList.size());
                }
            }
        } catch (Exception e) {
            logger.info(e.toString());
        }

        return expectedEventConfigList;
    }




}
