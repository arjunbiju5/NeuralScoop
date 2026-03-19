import os
import requests
from dotenv import load_dotenv
from groq import Groq

load_dotenv()
client = Groq(api_key= os.getenv("GROQAPI"))

def fetch(category="technology"):
    print(f"--Fetching {category} news--")

    api_key= os.getenv('NEWSAPI')
    url = f"https://newsapi.org/v2/top-headlines?category={category}&language=en&pageSize=3&apiKey={api_key}"
    
    resp = requests.get(url).json()
    if resp.get("status") != "ok":
        print("Error")
        print(f"API Error: {resp.get('code')} - {resp.get('message')}")
        return

    articles = resp.get("articles", [])
    for item in articles:
        title = item.get('title')
        desc = item.get('description')

        print(f"Summarize: {title}")

        try:
            completion = client.chat.completions.create(
                model="llama-3.1-8b-instant",
                messages=[
                    {"role": "system", "content": "Summarize this news in exactly 2 short sentences0" },
                    {"role": "user", "content": f"Title: {title}\nDescription: {desc}"}
                ]
            )
            summary = completion.choices[0].message.content
            print(f"AI Summary: {summary}")
        except Exception as e:
            print("Error:", e)
if __name__ == "__main__":
    fetch()