from minio import Minio

client = Minio(
    "localhost:9000",
    access_key="admin",
    secret_key="admin123",
    secure=False
)

objects = client.list_objects("my-bucket", recursive=True)

for obj in objects:
    print(obj.object_name)
