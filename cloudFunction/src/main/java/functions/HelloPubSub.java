package functions;

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Logger;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import static java.nio.charset.StandardCharsets.UTF_8;
//

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HelloPubSub implements BackgroundFunction<Message> {
    private static final Logger logger = Logger.getLogger(HelloPubSub.class.getName());

    @Override
    public void accept(Message message, Context context) {
        String name = "world";
        String set = "dummy";
        if (message != null && message.getData() != null) {
            name = new String(
                    Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
                    StandardCharsets.UTF_8);
           // write_to_bucket(name);
        }

        logger.info(String.format("MessageArrived= %s!", name));

        return;
    }


//    private void write_to_bucket(String record) {
//        Storage storage = StorageOptions.newBuilder().setProjectId("lyrical-amulet-308012").build().getService();
//        BlobId blobId = BlobId.of("smalltech", "text");
//        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
//        storage.create(blobInfo, record.getBytes());
//
//
//
//        }


    }

