import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.*;
import com.smalltech.avro.pos;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.joda.time.Duration;


public class BeamPubsub {

    public interface PubSubToGcsOptions extends PipelineOptions, StreamingOptions {

    }

    public static class ExtractCountry extends DoFn<pos,String>
    {
        @DoFn.ProcessElement
        public  void ProcessElement(@Element pos input, OutputReceiver<String> out)
        {
            out.output(input.getClass().toString());

        }

    }
    public static void main(String[] args) {
        PubSubToGcsOptions options=PipelineOptionsFactory.fromArgs(args).withValidation().as(PubSubToGcsOptions.class);
        options.setStreaming(true);
        Pipeline pipeline = Pipeline.create(options);
        pipeline.apply(PubsubIO.readAvros(pos.class).fromSubscription("projects/lyrical-amulet-308012/subscriptions/pos_apache"))
                .apply(ParDo.of(new ExtractCountry()));

        pipeline.run().waitUntilFinish();

    }
}
