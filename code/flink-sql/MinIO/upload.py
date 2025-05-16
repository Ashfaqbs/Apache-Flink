from minio import Minio
import json

client = Minio(
    "localhost:9000",
    access_key="admin",
    secret_key="admin123",
    secure=False
)

bucket = "my-bucket-2"

# Checking bucket policy
try:
    policy = client.get_bucket_policy(bucket)
    print("Current bucket policy:", json.loads(policy))
except Exception as e:
    print("Error fetching policy or no policy set:", str(e))

# Setting a permissive policy (optional, for testing)
permissive_policy = {
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {"AWS": "*"},
            "Action": ["s3:*"],
            "Resource": [f"arn:aws:s3:::{bucket}/*"]
        }
    ]
}
client.set_bucket_policy(bucket, json.dumps(permissive_policy))
print("Set permissive policy for testing")

# Proceed with upload
object_name = "initial.csv"
file_path = "C:/tmp/Apache-Flink/code/flink-sql/MinIO/initial.csv"
if not client.bucket_exists(bucket):
    client.make_bucket(bucket)
result = client.fput_object(bucket, object_name, file_path)
print(f"Uploaded {object_name} successfully. ETag: {result.etag}")