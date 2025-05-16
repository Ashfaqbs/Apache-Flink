from minio import Minio
import json

client = Minio(
    "localhost:9000",
    access_key="admin",
    secret_key="admin123",
    secure=False
)

bucket = "my-bucket-2"
object_name = "initial.csv"
file_path = "C:/tmp/Apache-Flink/code/flink-sql/MinIO/initial.csv"

# Ensure bucket exists
if not client.bucket_exists(bucket):
    client.make_bucket(bucket)
    print(f"Created bucket: {bucket}")

# Set permissive policy
permissive_policy = {
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {"AWS": "*"},
            "Action": ["s3:PutObject", "s3:GetObject", "s3:ListBucket"],
            "Resource": [f"arn:aws:s3:::{bucket}/*", f"arn:aws:s3:::{bucket}"]
        }
    ]
}
try:
    client.set_bucket_policy(bucket, json.dumps(permissive_policy))
    print("Set permissive policy for bucket")
except Exception as e:
    print(f"Error setting policy: {str(e)}")

# Remove existing object (if any) to avoid conflicts
try:
    client.remove_object(bucket, object_name)
    print(f"Removed existing {object_name}")
except Exception as e:
    print(f"No existing {object_name} or error removing it: {str(e)}")

# Upload file
try:
    result = client.fput_object(bucket, object_name, file_path)
    print(f"Uploaded {object_name} successfully. ETag: {result.etag}")
except Exception as e:
    print(f"Upload failed: {str(e)}")
    
    
    
#     C:\tmp\Apache-Flink\code\flink-sql\MinIO>python createBucket.py
# Created bucket: my-bucket-2
# Set permissive policy for bucket
# Removed existing initial.csv
# Uploaded initial.csv successfully. ETag: 99bfd72945492d77f6aca2be25e6fe6f


# we had exisitng bucket my-bucket created from docker-compose but failed to upload due to policy issues
# so we created a new bucket my-bucket-2 and uploaded the file