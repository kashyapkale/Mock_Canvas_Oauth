# Deployment Guide for Fake OAuth Canvas

## Option 1: Railway (Recommended - Easiest)

### Step 1: Create Railway Account
1. Go to [railway.app](https://railway.app)
2. Sign up with GitHub (recommended) or email

### Step 2: Create New Project
1. Click **"New Project"**
2. Select **"Deploy from GitHub repo"** (if you've pushed to GitHub)
   - OR select **"Empty Project"** and connect GitHub later

### Step 3: Deploy the Application
1. If using GitHub:
   - Select your repository
   - Railway will auto-detect it's a Spring Boot app
   - Click **"Deploy Now"**

2. If using Empty Project:
   - Click **"New"** → **"GitHub Repo"**
   - Select your repository
   - Railway will start building automatically

### Step 4: Set Environment Variables
1. In your Railway project dashboard, click on your service
2. Go to **"Variables"** tab
3. Click **"New Variable"** and add each of these:

```bash
# Server Configuration
SERVER_PORT=8457
PROVIDER_BASE_URL=https://your-app-name.up.railway.app

# OAuth Client Configuration
OAUTH_CLIENT_ID=fake-client-id
OAUTH_CLIENT_SECRET=fake-client-secret

# Instructor OAuth Tokens (REQUIRED)
OAUTH_INSTRUCTOR_ACCESS_TOKEN=4511~eTxfPztKa3uBaKFk4GXKnB8WT3h4vUMuULLEXfY8kAYenAVLenrE2DQCxCVUeHaV
OAUTH_INSTRUCTOR_REFRESH_TOKEN=refresh-token-instructor-67890
OAUTH_INSTRUCTOR_ID=101
OAUTH_INSTRUCTOR_NAME=Kashyap Kale

# Student OAuth Tokens (REQUIRED)
OAUTH_STUDENT_ACCESS_TOKEN=4511~BZDWGxJvCmMvTyQfUEXnGLJavQttXBEk8EPCvUKCN3aJL3CX79DYVF3xxaLUeKu6
OAUTH_STUDENT_REFRESH_TOKEN=refresh-token-student-fghij
OAUTH_STUDENT_ID=202
OAUTH_STUDENT_NAME=Sarthak Raut

# Database (optional - H2 will use in-memory if not set)
DATASOURCE_URL=jdbc:h2:mem:oauth2-db
DATASOURCE_USERNAME=sa
DATASOURCE_PASSWORD=
```

**Important:** 
- After adding `PROVIDER_BASE_URL`, Railway will give you a URL like `https://your-app-name.up.railway.app`
- Update `PROVIDER_BASE_URL` to match that URL
- Railway will automatically redeploy when you change variables

### Step 5: Get Your Deployment URL
1. After deployment completes, Railway will show your app URL
2. It will be something like: `https://fake-oauth-canvas-production.up.railway.app`
3. Copy this URL - you'll need it for your backend configuration

### Step 6: Update Backend Configuration
In your Java backend's ECS task definition or environment variables, set:
```bash
OAUTH_SERVER_URL=https://your-railway-app-url.up.railway.app
```

---

## Option 2: Render (Alternative)

### Step 1: Create Render Account
1. Go to [render.com](https://render.com)
2. Sign up with GitHub

### Step 2: Create New Web Service
1. Click **"New +"** → **"Web Service"**
2. Connect your GitHub repository
3. Select the repository and branch

### Step 3: Configure Build Settings
- **Name:** `fake-oauth-canvas`
- **Environment:** `Java`
- **Build Command:** `./mvnw clean package -DskipTests`
- **Start Command:** `java -jar target/fake_oauth_canvas-0.0.1-SNAPSHOT.jar`

### Step 4: Set Environment Variables
In the **"Environment"** section, add all the variables listed above (same as Railway)

### Step 5: Deploy
Click **"Create Web Service"** and wait for deployment

---

## Option 3: Fly.io (Alternative)

### Step 1: Install Fly CLI
```bash
curl -L https://fly.io/install.sh | sh
```

### Step 2: Login
```bash
fly auth login
```

### Step 3: Create Fly App
```bash
cd /path/to/fake_oauth_canvas
fly launch
```

### Step 4: Set Environment Variables
```bash
fly secrets set OAUTH_INSTRUCTOR_ACCESS_TOKEN="4511~eTxfPztKa3uBaKFk4GXKnB8WT3h4vUMuULLEXfY8kAYenAVLenrE2DQCxCVUeHaV"
fly secrets set OAUTH_STUDENT_ACCESS_TOKEN="4511~BZDWGxJvCmMvTyQfUEXnGLJavQttXBEk8EPCvUKCN3aJL3CX79DYVF3xxaLUeKu6"
# ... add all other variables
```

---

## Testing Your Deployment

After deployment, test these endpoints:

1. **Health Check:**
   ```bash
   curl https://your-app-url.up.railway.app/health
   ```

2. **OAuth Authorization:**
   ```
   https://your-app-url.up.railway.app/login/oauth2/auth?client_id=fake-client-id&response_type=code&redirect_uri=http://localhost:3000/auth/callback
   ```

3. **Token Exchange:**
   ```bash
   curl -X POST https://your-app-url.up.railway.app/login/oauth2/token \
     -d "grant_type=authorization_code&client_id=fake-client-id&client_secret=fake-client-secret&code=YOUR_CODE&redirect_uri=http://localhost:3000/auth/callback"
   ```

---

## Troubleshooting

### Build Fails
- Check Railway/Render logs for Maven errors
- Ensure Java 17 is specified in `pom.xml` (already done)

### App Won't Start
- Check that all required environment variables are set
- Verify `PROVIDER_BASE_URL` matches your deployment URL

### CORS Errors
- Update `CORS_ORIGIN` environment variable to match your frontend URL
- Or set it to `*` for testing (not recommended for production)

---

## Quick Reference: Environment Variables Checklist

- [ ] `SERVER_PORT` (default: 8457)
- [ ] `PROVIDER_BASE_URL` (your deployment URL)
- [ ] `OAUTH_CLIENT_ID`
- [ ] `OAUTH_CLIENT_SECRET`
- [ ] `OAUTH_INSTRUCTOR_ACCESS_TOKEN` ⚠️ REQUIRED
- [ ] `OAUTH_INSTRUCTOR_REFRESH_TOKEN`
- [ ] `OAUTH_INSTRUCTOR_ID`
- [ ] `OAUTH_INSTRUCTOR_NAME`
- [ ] `OAUTH_STUDENT_ACCESS_TOKEN` ⚠️ REQUIRED
- [ ] `OAUTH_STUDENT_REFRESH_TOKEN`
- [ ] `OAUTH_STUDENT_ID`
- [ ] `OAUTH_STUDENT_NAME`

