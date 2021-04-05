cd ..\apachebeam
mvn compile exec:java -Dexec.mainClass=BeamPubsub -Dexec.args="--runner=DataflowRunner --gcpTempLocation=gs://smalltech/tmp --project=lyrical-amulet-308012 --region=us-central1" -Pdataflow-runner

