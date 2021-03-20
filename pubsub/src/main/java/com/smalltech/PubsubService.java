package com.smalltech;

import org.springframework.stereotype.Service;
import com.smalltech.avro.pos;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.Encoding;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
@Service
public class PubsubService {

	public String publishMessage() throws Exception {
		String projectId = "lyrical-amulet-308012";
		// Use a topic created with an Avro schema.
		String topicId = "pos";

		return publishAvroRecordsExample(projectId, topicId);
		
	}


	public static String publishAvroRecordsExample(String projectId, String topicId) throws IOException, ExecutionException, InterruptedException {
		StringBuilder str
				= new StringBuilder();
		Encoding encoding = null;

		TopicName topicName = TopicName.of(projectId, topicId);

		// Get the topic encoding type.
		try (TopicAdminClient topicAdminClient = TopicAdminClient.create()) {
			encoding = topicAdminClient.getTopic(topicName).getSchemaSettings().getEncoding();
		}

		// Instantiate an avro-tools-generated class defined in `us-states.avsc`.
		//pos state = pos.newBuilder().setName("Alaska").setPostAbbr("AK").build();
		for(int i=0;i<5;i++)
		{
			PubsubDataGenerator inject = new PubsubDataGenerator();
			pos state = pos.newBuilder().setTimeofsale(inject.timeofsale).setMerchant(inject.merchant).setProductid(inject.productid).setSellingPrice(inject.sellingPrice).setQuantity(inject.quantity).build();
			Publisher publisher = null;

			block:
			try {
				publisher = Publisher.newBuilder(topicName).build();

				// Prepare to serialize the object to the output stream.
				ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

				Encoder encoder = null;

				// Prepare an appropriate encoder for publishing to the topic.
				switch (encoding) {
					case BINARY:
						System.out.println("Preparing a BINARY encoder...");
						encoder = EncoderFactory.get().directBinaryEncoder(byteStream, /*reuse=*/ null);
						break;

					case JSON:
						System.out.println("Preparing a JSON encoder...");
						encoder = EncoderFactory.get().jsonEncoder(pos.getClassSchema(), byteStream);
						break;

					default:
						break block;
				}

				// Encode the object and write it to the output stream.
				state.customEncode(encoder);
				encoder.flush();

				// Publish the encoded object as a Pub/Sub message.
				ByteString data = ByteString.copyFrom(byteStream.toByteArray());
				PubsubMessage message = PubsubMessage.newBuilder().setData(data).build();
				System.out.println("Publishing message: " + message);

				ApiFuture<String> future = publisher.publish(message);
				str.append(message+future.get());
				str.append("\n");
				System.out.println("Published message ID: " + future.get());

			} finally {
				if (publisher != null) {
					publisher.shutdown();
					publisher.awaitTermination(1, TimeUnit.MINUTES);
				}
			}
		}
		return str.toString();
	}




}
