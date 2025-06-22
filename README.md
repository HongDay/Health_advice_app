# 📱 Health_advice_app

**20201325 홍대의**  
Implemented Health Advice app via 'context awareness' and 'mobile machine learning'.

My app detect the user's current activity (Sleeping, Studying, In class, Exercising, Else) by context data.

For context data, our app used GPS, MIC, Light sensor, Accelerometer, GyroSensor, Wifi sensing, etc.

Every day, our app provide the useful advice and feedback to improve user's health by analyzing user's daily activity routine.

Our app can be treated as **Health Lifestyle Assistant**

 <br/>

## 🔧 Main features
- **AI model optimization**
  : User can enter what they're doing, and then our app collect the context data and fine-tune the AI model so that it can personalized to specific user.
- **Detect User current activity**
  : By mobile LSTM model which is converted by TFLite, we can inference the user activity in-app.
- **Daily log timetable check**
  : User can check how many time they spent for each activities
- **Health advice & feedback**
  : Our app analyze for each activity's time duration, give user proper advice and feedback.

 <br/>

## 🗒️ Current progress and Future plan
So far, all frontend tasks have been completed, including mobile machine learning pre-training, model loading into the app, and implementation of the app’s core features.

Moving forward, we plan to develop a backend server to enhance the app's utility by enabling richer context data storage, more efficient preprocessing, support for complex models, and on-the-fly fine-tuning.
 

## 📂 Repository Overview

### Algorithm_assignment1
- `App manual video.mp4`
  : Video for 'how to use this app'
- Machine Learning / EDA code
  : Code for training LSTM model, convert with TFLite, collected context data analysis
- Screenshot
  : App usage captured screen
- Context data
  : Collected context data in .txt format
- TFLite ML model
