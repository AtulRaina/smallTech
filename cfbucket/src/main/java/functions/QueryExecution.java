package functions;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Job;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.TableResult;
import java.util.UUID;
import java.util.logging.Logger;


public class QueryExecution {

    BigQuery  bigquery=null;
    private static final Logger logger = Logger.getLogger(QueryExecution.class.getName());
    public QueryExecution()
    {
        bigquery = BigQueryOptions.getDefaultInstance().getService();
    }

    public  TableResult execute(String queryToRun) throws InterruptedException {
        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();
        QueryJobConfiguration queryConfig =
                QueryJobConfiguration.newBuilder(
                        queryToRun)
                        // Use standard SQL syntax for queries.
                        // See: https://cloud.google.com/bigquery/sql-reference/
                        .setUseLegacySql(false)
                        .build();

        // Create a job ID so that we can safely retry.
        JobId jobId = JobId.of(UUID.randomUUID().toString());
        Job queryJob = bigquery.create(JobInfo.newBuilder(queryConfig).setJobId(jobId).build());
        //logger.info("Executing query"+ queryToRun);
        // Wait for the query to complete.
        queryJob = queryJob.waitFor();
        // Get the results.
        return  queryJob.getQueryResults();
    }


}
