# Railway Deployment Guide for Fake OAuth Canvas

## 🚂 Deploy to Railway - Step by Step

### Prerequisites
- GitHub account
- Railway account (sign up at [railway.app](https://railway.app))
- Your code pushed to GitHub

---

## Step 1: Create Railway Account

1. Go to [railway.app](https://railway.app)
2. Click **"Start a New Project"**
3. Sign up with **GitHub** (recommended) or email
4. Authorize Railway to access your GitHub repositories

---

## Step 2: Create New Project

1. In Railway dashboard, click **"New Project"**
2. Select **"Deploy from GitHub repo"**
3. Choose your repository: `fake-oauth-canvas` (or whatever you named it)
4. Railway will automatically:
   - Detect it's a Spring Boot application
   - Start building your project
   - Create a deployment

**Note:** Railway uses Nixpacks to auto-detect and build your app. It will detect Maven and Java 17.

---

## Step 3: Wait for Initial Build

- Railway will start building automatically
- First build takes 5-10 minutes
- You'll see build logs in real-time
- Wait for "Build Succeeded" message

---

## Step 4: Get Your Deployment URL

1. After build completes, Railway will show your service URL
2. It will look like: `https://fake-oauth-canvas-production.up.railway.app`
3. **Copy this URL** - you'll need it for the next step

---

## Step 5: Set Environment Variables

1. In your Railway project, click on your service
2. Go to the **"Variables"** tab
3. Click **"New Variable"** and add each variable one by one:

### Required Variables (Copy & Paste Each):

```bash
# Server Configuration
SERVER_PORT=8457
PROVIDER_BASE_URL=https://your-app-name.up.railway.app
```
⚠️ **Important:** Replace `your-app-name` with your actual Railway URL from Step 4

```bash
# OAuth Client Configuration
OAUTH_CLIENT_ID=fake-client-id
OAUTH_CLIENT_SECRET=fake-client-secret
```

```bash
# Instructor OAuth Tokens (REQUIRED)
OAUTH_INSTRUCTOR_ACCESS_TOKEN=4511~eTxfPztKa3uBaKFk4GXKnB8WT3h4vUMuULLEXfY8kAYenAVLenrE2DQCxCVUeHaV
```

```bash
OAUTH_INSTRUCTOR_REFRESH_TOKEN=refresh-token-instructor-67890
```

```bash
OAUTH_INSTRUCTOR_ID=101
```

```bash
OAUTH_INSTRUCTOR_NAME=Kashyap Kale
```

```bash
# Student OAuth Tokens (REQUIRED)
OAUTH_STUDENT_ACCESS_TOKEN=4511~BZDWGxJvCmMvTyQfUEXnGLJavQttXBEk8EPCvUKCN3aJL3CX79DYVF3xxaLUeKu6
```

```bash
OAUTH_STUDENT_REFRESH_TOKEN=refresh-token-student-fghij
```

```bash
OAUTH_STUDENT_ID=202
```

```bash
OAUTH_STUDENT_NAME=Sarthak Raut
```

### Quick Copy-Paste (All at Once)

If Railway supports bulk import, you can add all at once:

```bash
SERVER_PORT=8457
PROVIDER_BASE_URL=https://your-app-name.up.railway.app
OAUTH_CLIENT_ID=fake-client-id
OAUTH_CLIENT_SECRET=fake-client-secret
OAUTH_INSTRUCTOR_ACCESS_TOKEN=4511~eTxfPztKa3uBaKFk4GXKnB8WT3h4vUMuULLEXfY8kAYenAVLenrE2DQCxCVUeHaV
OAUTH_INSTRUCTOR_REFRESH_TOKEN=refresh-token-instructor-67890
OAUTH_INSTRUCTOR_ID=101
OAUTH_INSTRUCTOR_NAME=Kashyap Kale
OAUTH_STUDENT_ACCESS_TOKEN=4511~BZDWGxJvCmMvTyQfUEXnGLJavQttXBEk8EPCvUKCN3aJL3CX79DYVF3xxaLUeKu6
OAUTH_STUDENT_REFRESH_TOKEN=refresh-token-student-fghij
OAUTH_STUDENT_ID=202
OAUTH_STUDENT_NAME=Sarthak Raut
```

**Remember:** Update `PROVIDER_BASE_URL` with your actual Railway URL!

---

## Step 6: Redeploy After Setting Variables

1. After adding all environment variables, Railway will **automatically redeploy**
2. Wait for the new deployment to complete
3. Check the **"Deployments"** tab to see build progress

---

## Step 7: Verify Deployment

1. **Test Health Endpoint:**
   ```bash
   curl https://your-app-name.up.railway.app/health
   ```
   Should return: `{"status":"ok",...}`

2. **Test OAuth Authorization:**
   Open in browser:
   ```
   https://your-app-name.up.railway.app/login/oauth2/auth?client_id=fake-client-id&response_type=code&redirect_uri=http://localhost:3000/auth/callback
   ```

3. **Check Logs:**
   - Go to **"Deployments"** tab
   - Click on latest deployment
   - View logs to see if app started successfully

---

## Step 8: Update Your Backend Configuration

Once your OAuth service is deployed, update your Java backend:

1. Go to your AWS ECS task definition or environment variables
2. Set:
   ```bash
   OAUTH_SERVER_URL=https://your-app-name.up.railway.app
   ```
3. Redeploy your backend service

---

## 🎯 Railway Configuration Tips

### Custom Domain (Optional)
1. In Railway, go to **"Settings"** → **"Networking"**
2. Click **"Generate Domain"** or add custom domain
3. Update `PROVIDER_BASE_URL` to match new domain

### Monitoring
- Railway provides built-in metrics
- Check **"Metrics"** tab for CPU, Memory, Network usage
- View logs in **"Deployments"** tab

### Auto-Deploy
- Railway automatically deploys on every push to your main branch
- You can disable this in **"Settings"** → **"Source"**

---

## 🐛 Troubleshooting

### Build Fails
- **Check logs** in Railway dashboard
- Ensure `pom.xml` is correct
- Verify Java 17 is specified (already done)

### App Won't Start
- **Check environment variables** - all required vars must be set
- **Verify `PROVIDER_BASE_URL`** matches your Railway URL exactly
- **Check logs** for error messages

### Port Issues
- Railway sets `PORT` automatically
- Your `application.properties` already handles this: `server.port=${PORT:${SERVER_PORT:8457}}`
- No action needed

### CORS Errors
- Add `CORS_ORIGIN` environment variable:
  ```bash
  CORS_ORIGIN=https://your-frontend-url.com
  ```

---

## 💰 Railway Pricing

- **Free Trial**: 30 days with $5 credit
- **Free Plan**: $1/month credit (services pause if exceeded)
- **Hobby Plan**: $5/month (recommended for always-on)

For testing purposes, the free plan should be sufficient.

---

## ✅ Deployment Checklist

- [ ] Code pushed to GitHub
- [ ] Railway account created
- [ ] Project created and connected to GitHub repo
- [ ] Initial build completed successfully
- [ ] Copied Railway deployment URL
- [ ] All environment variables set (especially `PROVIDER_BASE_URL`)
- [ ] Service redeployed after setting variables
- [ ] Health check endpoint works
- [ ] OAuth authorization page loads
- [ ] Backend `OAUTH_SERVER_URL` updated

---

## 🎉 Success!

Once all steps are complete, your Mock Canvas OAuth service will be live and accessible from anywhere, allowing your AWS backend to authenticate users via OAuth!

**Your OAuth Service URL:** `https://your-app-name.up.railway.app`

**Backend Configuration:**
```bash
OAUTH_SERVER_URL=https://your-app-name.up.railway.app
```

