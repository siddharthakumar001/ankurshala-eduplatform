# GitHub Secrets Setup Guide

This guide will help you set up the required secrets for the AnkurShala CI/CD pipeline to work with Azure Storage and VM deployment.

## 🔐 Required Secrets

You need to add the following secrets to your GitHub repository:

### 1. STORAGE_KEY
- **Purpose:** Azure Storage Account access key
- **Value:** Your Azure storage account primary key
- **How to get it:**
  1. Go to [Azure Portal](https://portal.azure.com)
  2. Navigate to your storage account: `ankurimage`
  3. Go to **Access keys** in the left menu
  4. Copy the **key** value from **key1**
  5. The key should look like: `your-storage-account-key-here`

### 2. SSH_PRIVATE_KEY (Optional for now)
- **Purpose:** SSH private key for VM access
- **Value:** Private key content for SSH authentication
- **Note:** This is optional for now since the current pipeline uses password authentication

## 📋 How to Add Secrets

### Step 1: Navigate to Repository Settings
1. Go to your GitHub repository: `https://github.com/siddharthakumar001/ankurshala-eduplatform`
2. Click on the **Settings** tab
3. In the left sidebar, click on **Secrets and variables** → **Actions**

### Step 2: Add STORAGE_KEY Secret
1. Click **New repository secret**
2. **Name:** `STORAGE_KEY`
3. **Secret:** Paste your Azure storage account key
4. Click **Add secret**

### Step 3: Verify Secret Addition
- You should see `STORAGE_KEY` listed in the secrets
- The value will be masked for security

## 🔧 Testing the Setup

### Option 1: Use the Fix Script
Run the Azure Storage fix script to test your setup:

```bash
# Set your storage key
export STORAGE_KEY="your-actual-storage-key-here"

# Run the fix script
./scripts/fix-azure-storage.sh
```

### Option 2: Test via GitHub Actions
1. Make a small commit to trigger the CI/CD pipeline
2. Check the GitHub Actions logs
3. Look for the "Setup Azure Storage Container" step

## 🚨 Common Issues and Solutions

### Issue 1: "STORAGE_KEY secret is not set"
**Solution:** Make sure you've added the `STORAGE_KEY` secret to your GitHub repository

### Issue 2: "Azure CLI authentication failed"
**Solution:** 
- Verify your storage account key is correct
- Check that the storage account name is `ankurimage`
- Ensure the key hasn't expired or been rotated

### Issue 3: "Container creation failed"
**Solution:**
- Check storage account permissions
- Verify the storage account is in the correct region
- Ensure you have the necessary Azure permissions

### Issue 4: "Container verification failed"
**Solution:**
- Wait a few minutes and try again (Azure propagation delay)
- Check if the container was created but with a different name
- Verify the container name is exactly `docker-images`

## 🔍 Debugging Steps

### 1. Check Storage Account Status
```bash
# Test storage account access
az storage account show --name ankurimage --key YOUR_STORAGE_KEY
```

### 2. List Existing Containers
```bash
# List all containers
az storage container list --account-name ankurimage --account-key YOUR_STORAGE_KEY
```

### 3. Test Container Creation
```bash
# Create container manually
az storage container create \
  --name docker-images \
  --account-name ankurimage \
  --account-key YOUR_STORAGE_KEY \
  --public-access off
```

## 📊 Expected Results

After successful setup, you should see:
- ✅ Azure CLI authentication successful
- ✅ Container 'docker-images' is ready for use
- ✅ CI/CD pipeline proceeds to image building and upload

## 🔒 Security Best Practices

1. **Never commit secrets to code** - Always use GitHub secrets
2. **Rotate keys regularly** - Update storage account keys periodically
3. **Use least privilege** - Only grant necessary permissions
4. **Monitor access** - Check Azure activity logs regularly

## 📞 Support

If you encounter issues:
1. Check the GitHub Actions logs for detailed error messages
2. Verify your Azure storage account configuration
3. Ensure all secrets are correctly set
4. Test the fix script locally first

---

**Next Steps:** After setting up the secrets, the CI/CD pipeline should work without Azure Storage errors. The pipeline will automatically create the container if it doesn't exist and proceed with Docker image uploads.