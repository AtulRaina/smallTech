



import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.WriteResult;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.beam.repackaged.core.org.apache.commons.lang3.ArrayUtils;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.DoFn;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

import com.smalltech.avro.pos;
public class BeamFirestore<In> extends DoFn<pos, Void>  {

   // private static final long serialVersionUID = 2L;
    private transient List<pos> mutations;
    private transient Firestore db;
    private  Map<String, Object> map = new HashMap<>();
    public BeamFirestore() {
    }

    @StartBundle
    public void setupBufferedMutator(StartBundleContext startBundleContext) throws IOException {
        this.mutations = new ArrayList<pos>();
    }

    @ProcessElement
    public void processElement(ProcessContext context) throws Exception {




        pos mutation = (pos)context.element();
        System.out.print(mutation.getClass().toString());







        mutations.add(mutation);
        // Batch size set to 200, could go up to 500
        if (mutations.size() >= 5) {
            flushBatch(context.getPipelineOptions());
        }
    }

    private void flushBatch(PipelineOptions pipelineOptions) throws Exception {
        List field_to_pick = new ArrayList<>(Arrays.asList("timeofsale","merchant","productid","sellingPrice","quantity"));


        // Create firestore instance
        FirestoreOptions firestoreOptions = FirestoreOptions
                .getDefaultInstance().toBuilder()
                .setProjectId("lyrical-amulet-308012")
                .build();

        db = firestoreOptions.getService();

        // Create batch to commit documents
        WriteBatch batch = db.batch();
        for (pos doc : mutations) {
            pos ps= JsonConvert.DeserializeObject<pos>(json)
            Map<String, Object> map = new HashMap<>();
// Use MyObject.class.getFields() instead of getDeclaredFields()
// If you are interested in public fields only
            for (Field field : pos.class.getDeclaredFields()) {
                // Skip this if you intend to access to public fields only
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
               if( field_to_pick.contains(field.getName()))
                {
                    map.put(field.getName(), field.get(doc));
                }


            }
            System.out.print("\n------------------------------------------------------------------------");
            System.out.print(map);

            String uuid = UUID.randomUUID().toString().replace("-", "");
            DocumentReference docRef = db.collection("Orders").document(uuid);
            batch.set(docRef, map);
        }

        ApiFuture<List<WriteResult>> wr = batch.commit();
        if (wr.isDone()) return;
    }
    @FinishBundle
    public synchronized void finishBundle(FinishBundleContext context) throws Exception {
        if (mutations.size() > 0) {
            flushBatch(context.getPipelineOptions());
        }
        // close connection
        db.close();
    }
}