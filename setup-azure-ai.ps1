# Azure AI Services Setup Script for AnkurShala
# This script creates Azure OpenAI and Azure Speech services using Azure CLI

param(
    [string]$ResourceGroupName = "ankurshala-ai-rg",
    [string]$Location = "eastus",
    [string]$OpenAIServiceName = "ankurshala-openai",
    [string]$SpeechServiceName = "ankurshala-speech",
    [string]$SubscriptionId = ""
)

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Azure AI Services Setup for AnkurShala" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Check if Azure CLI is installed
Write-Host "Checking Azure CLI installation..." -ForegroundColor Yellow
$azVersion = az version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Azure CLI is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Azure CLI from: https://aka.ms/installazurecliwindows" -ForegroundColor Yellow
    exit 1
}
Write-Host "Azure CLI is installed" -ForegroundColor Green
Write-Host ""

# Check if logged in
Write-Host "Checking Azure login status..." -ForegroundColor Yellow
$account = az account show 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Not logged in. Please login..." -ForegroundColor Yellow
    az login
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to login to Azure" -ForegroundColor Red
        exit 1
    }
}
Write-Host "Logged in to Azure" -ForegroundColor Green
Write-Host ""

# Set subscription if provided
if ($SubscriptionId) {
    Write-Host "Setting subscription to: $SubscriptionId" -ForegroundColor Yellow
    az account set --subscription $SubscriptionId
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to set subscription" -ForegroundColor Red
        exit 1
    }
}

# Get current subscription
$currentSub = az account show --query id -o tsv
Write-Host "Current subscription: $currentSub" -ForegroundColor Cyan
Write-Host ""

# Create resource group
Write-Host "Creating resource group: $ResourceGroupName" -ForegroundColor Yellow
az group create --name $ResourceGroupName --location $Location 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Resource group may already exist, continuing..." -ForegroundColor Yellow
} else {
    Write-Host "Resource group created" -ForegroundColor Green
}
Write-Host ""

# Create Azure OpenAI Service
Write-Host "Creating Azure OpenAI service: $OpenAIServiceName" -ForegroundColor Yellow
Write-Host "This may take a few minutes..." -ForegroundColor Yellow

$openaiExists = az cognitiveservices account show --name $OpenAIServiceName --resource-group $ResourceGroupName 2>&1
if ($LASTEXITCODE -ne 0) {
    az cognitiveservices account create `
        --name $OpenAIServiceName `
        --resource-group $ResourceGroupName `
        --kind OpenAI `
        --sku S0 `
        --location $Location `
        --yes 2>&1 | Out-Null
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to create Azure OpenAI service" -ForegroundColor Red
        Write-Host "Note: Azure OpenAI may not be available in all regions or may require approval" -ForegroundColor Yellow
        Write-Host "Please check: https://aka.ms/oai/access" -ForegroundColor Yellow
        exit 1
    }
    Write-Host "Azure OpenAI service created" -ForegroundColor Green
} else {
    Write-Host "Azure OpenAI service already exists" -ForegroundColor Green
}
Write-Host ""

# Get OpenAI endpoint and key
Write-Host "Retrieving Azure OpenAI credentials..." -ForegroundColor Yellow
$openaiEndpoint = az cognitiveservices account show --name $OpenAIServiceName --resource-group $ResourceGroupName --query properties.endpoint -o tsv
$openaiKey = az cognitiveservices account keys list --name $OpenAIServiceName --resource-group $ResourceGroupName --query key1 -o tsv

if (-not $openaiEndpoint -or -not $openaiKey) {
    Write-Host "ERROR: Failed to retrieve Azure OpenAI credentials" -ForegroundColor Red
    exit 1
}

Write-Host "Azure OpenAI Endpoint: $openaiEndpoint" -ForegroundColor Cyan
Write-Host ""

# Create Azure Speech Service
Write-Host "Creating Azure Speech service: $SpeechServiceName" -ForegroundColor Yellow
$speechExists = az cognitiveservices account show --name $SpeechServiceName --resource-group $ResourceGroupName 2>&1
if ($LASTEXITCODE -ne 0) {
    az cognitiveservices account create `
        --name $SpeechServiceName `
        --resource-group $ResourceGroupName `
        --kind SpeechServices `
        --sku S0 `
        --location $Location `
        --yes 2>&1 | Out-Null
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to create Azure Speech service" -ForegroundColor Red
        exit 1
    }
    Write-Host "Azure Speech service created" -ForegroundColor Green
} else {
    Write-Host "Azure Speech service already exists" -ForegroundColor Green
}
Write-Host ""

# Get Speech endpoint and key
Write-Host "Retrieving Azure Speech credentials..." -ForegroundColor Yellow
$speechEndpoint = az cognitiveservices account show --name $SpeechServiceName --resource-group $ResourceGroupName --query properties.endpoint -o tsv
$speechKey = az cognitiveservices account keys list --name $SpeechServiceName --resource-group $ResourceGroupName --query key1 -o tsv
$speechRegion = $Location

if (-not $speechEndpoint -or -not $speechKey) {
    Write-Host "ERROR: Failed to retrieve Azure Speech credentials" -ForegroundColor Red
    exit 1
}

Write-Host "Azure Speech Endpoint: $speechEndpoint" -ForegroundColor Cyan
Write-Host "Azure Speech Region: $speechRegion" -ForegroundColor Cyan
Write-Host ""

# Create deployment in OpenAI (if needed)
Write-Host "Checking for OpenAI deployments..." -ForegroundColor Yellow
$deployments = az cognitiveservices account deployment list --name $OpenAIServiceName --resource-group $ResourceGroupName --query "[].name" -o tsv 2>&1

if ($LASTEXITCODE -ne 0 -or -not $deployments) {
    Write-Host "No deployments found. You'll need to create a deployment manually." -ForegroundColor Yellow
    Write-Host "Please visit: https://portal.azure.com -> Your OpenAI resource -> Model deployments" -ForegroundColor Yellow
    Write-Host "Recommended models: gpt-4o-mini, gpt-4o, text-embedding-ada-002" -ForegroundColor Yellow
} else {
    Write-Host "Found deployments: $deployments" -ForegroundColor Green
}
Write-Host ""

# Generate .env file
Write-Host "Generating environment variables..." -ForegroundColor Yellow
$envContent = @"
# Azure OpenAI Configuration
AZURE_OPENAI_ENDPOINT=$openaiEndpoint
AZURE_OPENAI_API_KEY=$openaiKey
AZURE_OPENAI_DEPLOYMENT_NAME=gpt-4o-mini
AZURE_OPENAI_EMBEDDING_DEPLOYMENT_NAME=text-embedding-ada-002

# Azure Speech Configuration
AZURE_SPEECH_KEY=$speechKey
AZURE_SPEECH_REGION=$speechRegion
AZURE_SPEECH_LANGUAGE=en-IN

# AI Configuration
AI_ENABLED=true
AI_PROVIDER=azure-openai
AI_DEV_MODE=false
"@

$envFile = ".env.azure"
$envContent | Out-File -FilePath $envFile -Encoding utf8
Write-Host "Environment variables saved to: $envFile" -ForegroundColor Green
Write-Host ""

# Summary
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "Setup Complete!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Review and copy the environment variables from $envFile" -ForegroundColor White
Write-Host "2. Add them to your .env file or export them in your shell" -ForegroundColor White
Write-Host "3. Create model deployments in Azure Portal if needed" -ForegroundColor White
Write-Host "4. Restart your backend service" -ForegroundColor White
Write-Host ""
Write-Host "Resource Group: $ResourceGroupName" -ForegroundColor Cyan
Write-Host "Location: $Location" -ForegroundColor Cyan
Write-Host ""
Write-Host "IMPORTANT: Keep your API keys secure and never commit them to version control!" -ForegroundColor Red
Write-Host ""

