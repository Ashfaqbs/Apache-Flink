from minio import Minio

client = Minio(
    "localhost:9000",
    access_key="admin",
    secret_key="admin123",
    secure=False
)

# List buckets to test credentials
buckets = client.list_buckets()
for bucket in buckets:
    print(f"Bucket: {bucket.name}")