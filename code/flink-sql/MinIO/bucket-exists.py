from minio import Minio

client = Minio(
    "localhost:9000",
    access_key="admin",
    secret_key="admin123",
    secure=False
)

# Ensure bucket exists
if client.bucket_exists("my-bucket"):
    print("Bucket 'my-bucket' found. Listing objects:\n")
    for obj in client.list_objects("my-bucket", recursive=True):
        print(f"- {obj.object_name} ({obj.size} bytes)")
else:
    print("Bucket 'my-bucket' does NOT exist")
