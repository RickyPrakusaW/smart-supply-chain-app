# AgriMitra Backend Service (Node.js/Express + MongoDB)

Represents the web service and cloud database component of the AgriMitra application, fulfilling the **Hosting Web Service and DB (10 Points)** complexity requirement.

---

## 🛠️ Tech Stack & Features
- **Engine**: Node.js & Express.js
- **Database**: MongoDB Atlas (via Mongoose ODM)
- **Security**: JWT (JSON Web Tokens) Authentication
- **Default Seeding**: Seed default products/farmers automatically on first run
- **Hosting-ready**: Configured for Render and Railway out-of-the-box

---

## 📂 File Structure
```text
backend/
├── package.json   # Backend dependency catalog
├── server.js       # Main server logic & Mongoose models
└── README.md      # Instructions & Deployment steps (This file)
```

---

## 🚀 Local Quickstart (Development)

To test the backend on your laptop before deploying to the cloud:
1. Open your terminal in the `backend/` folder.
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run the development server:
   ```bash
   npm start
   ```
4. The server will run on `http://localhost:5000` (in-memory mode if MongoDB environment variables are not set).

---

## ☁️ Step 1: Setup Free MongoDB Atlas (Cloud Database)
1. Go to **[MongoDB Atlas](https://www.mongodb.com/cloud/atlas)** and sign up for a free account.
2. Create a new cluster (select the **M0 Shared Free Tier**).
3. Under **Database Access**, create a database user (e.g. username `agriadmin` with a password).
4. Under **Network Access**, click **Add IP Address** and select **Allow Access from Anywhere** (`0.0.0.0/0`) so your Render/Railway hosted server can access it.
5. Click on **Database > Connect > Drivers** and copy the Connection String.
   - It will look like: `mongodb+srv://agriadmin:<password>@cluster0.xxxx.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0`
   - Replace `<password>` with the password you created for the database user.

---

## 🌐 Step 2: Deploy Backend to Render (Free Hosting)
Render allows you to host web servers for free directly from your GitHub repository.
1. Push your updated code to your GitHub repository.
2. Go to **[Render Dashboard](https://dashboard.render.com/)** and log in with GitHub.
3. Click **New > Web Service**.
4. Connect your GitHub repository.
5. In the configuration settings, fill in:
   - **Name**: `agrimitra-backend` (or any custom name)
   - **Runtime**: `Node`
   - **Root Directory**: `backend` (CRITICAL! This tells Render that your backend code is inside the `backend/` folder, not at the root of the project).
   - **Build Command**: `npm install`
   - **Start Command**: `node server.js`
   - **Instance Type**: `Free`
6. Click **Advanced** to add **Environment Variables** (`Environment Variables` section):
   - `MONGO_URI`: `(Paste your MongoDB connection string from Step 1)`
   - `JWT_SECRET`: `agrimitra_jwt_super_secret_key` (or any secret password string)
7. Click **Create Web Service**. 
8. Render will build and deploy your app. Once deployed, copy your live URL (e.g., `https://agrimitra-backend.onrender.com`).

---

## 📱 Step 3: Connect Android App to the Hosted Backend
Once your Render web service is live, update the connection inside your Android app:
1. Open [ApiClient.kt](file:///Users/agustinaseli/Documents/RickyPrakusa/Project-MDP/smart-supply-chain-flutter/app/src/main/java/com/agroSystem/app/data/remote/ApiClient.kt) in Android Studio.
2. Change the `BACKEND_BASE_URL` constant to point to your live Render API URL:
   ```kotlin
   // Before:
   private const val BACKEND_BASE_URL = "https://run.mocky.io/v3/"

   // After (replace with your actual Render URL, make sure to add "/api/v1/"):
   private const val BACKEND_BASE_URL = "https://agrimitra-backend.onrender.com/api/v1/"
   ```
3. Sync and build the Android application.
4. **Done!** Any Google Login, Phone Login, or profile updates will now sync directly to MongoDB Atlas in the cloud!
