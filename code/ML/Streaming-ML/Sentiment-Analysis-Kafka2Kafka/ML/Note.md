###  **Why Python ML Libraries Aren’t Async**

Most core ML libraries in Python (like `scikit-learn`, `pandas`, `joblib`, `transformers`, etc.) are:

1. **CPU-bound** (not I/O-bound):
   These libraries do intense numerical computations, matrix operations, and CPU-heavy tasks. They **block** the thread until the computation finishes.

2. **Not built with `async`/`await`:**
   Python’s async ecosystem (`asyncio`) is mainly meant for *I/O-bound* tasks — like HTTP calls, database access, file reads, etc. ML libraries don’t expose async APIs because their operations don't benefit from async—they burn CPU, not wait on I/O.

---

### ✅ **Our FastAPI Code is Async in Interface, But Not Truly Async Inside**

FastAPI allows us to declare routes as async:

```python
@app.post("/predict_sentiment/")
async def predict_sentiment(review: ReviewInput):
```

However, if the body of that function uses blocking operations like this:

```python
prediction = model.predict([review.review_text])  # sync + CPU-bound
```

Then it's **not truly async**. FastAPI will **block the thread** that handles that request until the prediction is done.

This causes:

* **Thread exhaustion** in heavy load
* **Poor concurrency**, even with multiple workers
* **Misleading perception of scalability**

---

### 🧵 **Fix Option: Offload to ThreadPool (but with limits)**

Yes, FastAPI internally lets us offload **blocking sync code** to a `ThreadPoolExecutor`:

```python
import asyncio
from concurrent.futures import ThreadPoolExecutor

executor = ThreadPoolExecutor(max_workers=3)

@app.post("/predict_sentiment/")
async def predict_sentiment(review: ReviewInput):
    prediction = await asyncio.get_event_loop().run_in_executor(
        executor,
        lambda: model.predict([review.review_text])
    )
    sentiment = label_encoder.inverse_transform(prediction)[0]
    return {"sentiment": sentiment}
```

This gives us:

* ✅ non-blocking `async` endpoint
* ❌ still CPU-bound, limited by thread pool size
* ⚠️ still not scalable to *thousands* of concurrent requests

---

### 🚀 **Real Scalability Options**

#### 1. **Use a Production WSGI Worker with Process Scaling**

Run the FastAPI using **Gunicorn** with **Uvicorn workers**:

```bash
gunicorn -k uvicorn.workers.UvicornWorker sentiment_api:app -w 4
```

* 4 workers = 4 separate processes
* Each handles requests in parallel
* Still not *truly async*, but better than single-thread

#### 2. **Deploy Model as a Batchable or Parallel Service**

Use **dedicated model servers** like:

* **TorchServe** / **TF Serving** for deep learning
* **Ray Serve** (supports async + scaling)
* **BentoML** (scalable inference APIs)
* **ONNX Runtime + FastAPI** (ultra-fast C-level backend)

These can batch requests, auto-scale, and handle GPU loads.

#### 3. **Async Queue Pattern**

Instead of calling the model in the API:

* API puts request into **Kafka** or **Redis queue**
* Worker processes it and sends result back via another Kafka topic
* Consumer listens and responds

it’s a *true decoupled*, async-ready system.

---

###  Summary

| Technique                   | True Async | Scalable                 | Recommended |
| --------------------------- | ---------- | ------------------------ | ----------- |
| FastAPI + Sync ML           | ❌          | 🚫                       | ❌           |
| FastAPI + `run_in_executor` | ✅ (sorta)  | ⚠️ (3-10 threads)        | Not sure          |
| Gunicorn + Workers          | ❌          | ✅ (limited by processes) | ✅           |
| Ray Serve / BentoML         | ✅          | ✅✅✅                      | ✅✅         |
| Kafka Queue + Worker        | ✅✅         | ✅✅✅                      | 🔥      |

---