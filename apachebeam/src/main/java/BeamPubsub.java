import com.google.protobuf.ByteString;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.*;
import com.smalltech.avro.pos;
import org.apache.beam.sdk.state.State;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;


public class BeamPubsub {

    public interface PubSubToGcsOptions extends PipelineOptions, StreamingOptions {

    }




    // The DoFn to perform on each element in the input PCollection.
    static class ComputeWordLengthFn extends DoFn<String, String> {
        @DoFn.ProcessElement
        public  void ProcessElement(@Element String input, OutputReceiver<String> out)
        {   System.out.print("\n----------------------------------");
            System.out.print(input.toString()+'\n');
            out.output(input.toString());
        }
    }
          // The DoFn to perform on each element, which

    public static class ExtractCountry extends DoFn<pos,String>
    {
        @DoFn.ProcessElement
        public  void ProcessElement(@Element pos input, OutputReceiver<String> out)
        {

            String test =input.getQuantity().toString();
            System.out.print(test);
            out.output(test);

        }

    }

    public static class pubsub_to_class extends DoFn<PubsubMessage,pos>
    {
        @DoFn.ProcessElement
        public  void ProcessElement(@Element PubsubMessage message, OutputReceiver<pos> out)  {

            SpecificDatumReader<pos> reader = new SpecificDatumReader<>(pos.getClassSchema());
            try {
            // Instantiate an asynchronous message receiver.



                        // Get the schema encoding type.


                // Send the message data to a byte[] input stream.
            InputStream inputStream = new ByteArrayInputStream(message.getPayload());

            Decoder decoder = null;



            decoder = DecoderFactory.get().jsonDecoder(pos.getClassSchema(), inputStream);



            pos state=  reader.read(null,decoder);
            System.out.print(state.getMerchant().toString());
            out.output(state);
            }
            catch (IOException e) {
                System.err.println(e);
            }




                        }

    }





    public static void main(String[] args) {

        pos p = new pos();
        PubSubToGcsOptions options=PipelineOptionsFactory.fromArgs(args).withValidation().as(PubSubToGcsOptions.class);
        options.setStreaming(true);
        Pipeline pipeline = Pipeline.create(options);
        PCollection<PubsubMessage> lines = pipeline.apply(PubsubIO.readMessages().fromSubscription("projects/lyrical-amulet-308012/subscriptions/pos_apache"));
        PCollection<pos> ps= lines.apply(ParDo.of(new pubsub_to_class()));
        ps.apply(ParDo.of(new BeamFirestore()));
        pipeline.run().waitUntilFinish();

    }
}
