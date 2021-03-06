import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineRunner;
import org.apache.beam.sdk.coders.DoubleCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.SchemaAndRecord;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.graalvm.compiler.lir.alloc.lsra.LinearScanResolveDataFlowPhase;
import org.joda.time.Duration;
import java.io.Serializable;

public class SmallTechBeamWatch implements Serializable {
    public interface PubSubToGcsOptions extends PipelineOptions {

    }
    public static class MeasureLength extends DoFn<String,String>
    {
        @ProcessElement
        public  void ProcessElement(@Element String input, OutputReceiver<String> out)
        {
            System.out.println(String.valueOf(input.length()));
            out.output( String.valueOf(input.length()));

        }

    }

    public static class Caster extends DoFn<Double,String>
    {
        @ProcessElement
        public  void ProcessElement(@Element Double input, OutputReceiver<String> out)
        {

            out.output( String.valueOf(input));

        }

    }







    public static void main(String[] args) {
        PubSubToGcsOptions options=PipelineOptionsFactory.fromArgs(args).withValidation().as(PubSubToGcsOptions.class);



        Pipeline p = Pipeline.create(options);


        PCollection<String> lines = p.apply(TextIO.read()
                .from("gs://smalltech//function/*.txt")
                .watchForNewFiles(
                        // Check for new files every 60 seconds
                        Duration.standardSeconds(30),
                        // Never stop checking for new files
                        Watch.Growth.<String>never()));


        if (lines != null) {
            PCollection<String> totalsales =
                    p.apply(
                            BigQueryIO.read(
                                    (SchemaAndRecord elem) ->  elem.getRecord().get("totalsales").toString())
                                    .fromQuery(
                                            "SELECT cast ( SUM(CAST(sellingPrice AS Numeric)) as String) AS totalsales FROM `lyrical-amulet-308012.Sample_Tech.pos`")
                                    .usingStandardSql().withCoder(StringUtf8Coder.of())
                                    .withoutValidation());


            totalsales.apply(TextIO.write().to("gs://smalltech//Sales/Results").withSuffix(".csv"));
        }

        p.run().waitUntilFinish();
    }
}
