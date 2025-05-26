from fastapi import FastAPI
from pydantic import BaseModel
import joblib
import uvicorn
from fastapi import FastAPI
from pydantic import BaseModel
import joblib
import asyncio
from concurrent.futures import ThreadPoolExecutor

model = joblib.load("model/sentiment_model.pkl")
label_encoder = joblib.load("model/label_encoder.pkl")

app = FastAPI()
executor = ThreadPoolExecutor(max_workers=3)  # For running blocking tasks asynchronously


class ReviewInput(BaseModel):
    review_text: str
    
    
# An Synchronous prediction function
def sync_predict(review_text: str):
    prediction = model.predict([review_text])
    sentiment = label_encoder.inverse_transform(prediction)[0]
    return sentiment

@app.post("/predict_sentiment/")
def predict_sentiment(review: ReviewInput):
    prediction = model.predict([review.review_text])
    sentiment = label_encoder.inverse_transform(prediction)[0]
    return {"sentiment": sentiment}

@app.post("/predict_sentiment-async/")
async def predict_sentiment(review: ReviewInput):
    sentiment = await asyncio.get_event_loop().run_in_executor(
        executor, sync_predict, review.review_text
    )
    return {"sentiment": sentiment}


# To run: uvicorn sentiment_api:app --reload


# use the below curl command 

# curl -X POST "http://127.0.0.1:8000/predict_sentiment/" -H "Content-Type: application/json" -d "{\"review_text\": \"This is an amazing product. I absolutely love it!\"}"
# output {"sentiment":"positive"}




## Note IMP 

## These Libs are sync and blocking 
# scikit-learn and joblib are not async-aware (since they are like CPU-bound and blocking), FastAPI can still benefits from defining async routes for I/O-bound tasks and  better concurrency handling.
# But calling synchronous model.predict(...) inside an async function is not truly non-blocking unless we can delegate it to a thread pool using run_in_executor

# Flow Kafka IP -> Async API -> Model Prediction -> Response -> Kafka OP
