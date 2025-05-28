import pandas as pd
import joblib
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.pipeline import Pipeline
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import LabelEncoder

df = pd.read_excel("sample_reviews.xlsx")

le = LabelEncoder()
df["label_encoded"] = le.fit_transform(df["label"])

X_train, X_test, y_train, y_test = train_test_split(
    df["review_text"], df["label_encoded"], test_size=0.2, random_state=42
)

model = Pipeline([
    ("tfidf", TfidfVectorizer()),
    ("clf", LogisticRegression(max_iter=1000))
])

model.fit(X_train, y_train)

joblib.dump(model, "model/sentiment_model.pkl")
joblib.dump(le, "model/label_encoder.pkl")
