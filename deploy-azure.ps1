# Script para fazer deploy no Azure App Service
# Uso: .\deploy-azure.ps1

param(
    [string]$ResourceGroup = "ouvidoria-senai-rg",
    [string]$AppServiceName = "ouvidoria-senai",
    [string]$JarFile = "target/Senai-0.0.1-SNAPSHOT.jar"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Deploy para Azure App Service" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Verificar se o JAR existe
if (-not (Test-Path $JarFile)) {
    Write-Host "❌ Erro: Arquivo $JarFile não encontrado!" -ForegroundColor Red
    Write-Host "Execute primeiro: .\mvnw clean package -DskipTests" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ JAR encontrado: $JarFile" -ForegroundColor Green

# Obter credenciais de publicação
Write-Host "`n📋 Obtendo credenciais de publicação..." -ForegroundColor Cyan
$publishProfile = az webapp deployment list-publishing-profiles `
    --resource-group $ResourceGroup `
    --name $AppServiceName `
    --query "[?publishMethod=='FTP'].{url:publishUrl, username:userName, password:userPWD}" `
    --output json | ConvertFrom-Json

if (-not $publishProfile) {
    Write-Host "❌ Erro ao obter credenciais. Verifique:" -ForegroundColor Red
    Write-Host "   - Resource Group: $ResourceGroup" -ForegroundColor Yellow
    Write-Host "   - App Service: $AppServiceName" -ForegroundColor Yellow
    Write-Host "   - Azure CLI instalado e autenticado" -ForegroundColor Yellow
    exit 1
}

$ftpUrl = $publishProfile.url
$ftpUser = $publishProfile.username
$ftpPass = $publishProfile.password

Write-Host "✅ Credenciais obtidas" -ForegroundColor Green

# Fazer upload via FTP
Write-Host "`n📤 Fazendo upload do JAR..." -ForegroundColor Cyan

$ftpRequest = [System.Net.FtpWebRequest]::Create("$ftpUrl/site/wwwroot/app.jar")
$ftpRequest.Method = [System.Net.WebRequestMethods+Ftp]::UploadFile
$ftpRequest.Credentials = New-Object System.Net.NetworkCredential($ftpUser, $ftpPass)
$ftpRequest.UseBinary = $true
$ftpRequest.KeepAlive = $false

$fileStream = [System.IO.File]::OpenRead($JarFile)
$ftpStream = $ftpRequest.GetRequestStream()
$fileStream.CopyTo($ftpStream)
$ftpStream.Close()
$fileStream.Close()

Write-Host "✅ Upload concluído!" -ForegroundColor Green

# Reiniciar o App Service
Write-Host "`n🔄 Reiniciando App Service..." -ForegroundColor Cyan
az webapp restart --resource-group $ResourceGroup --name $AppServiceName
Write-Host "✅ App Service reiniciado!" -ForegroundColor Green

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "✅ Deploy concluído com sucesso!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Acesse: https://$AppServiceName.azurewebsites.net" -ForegroundColor Yellow
