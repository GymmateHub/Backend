REM Generate JWT Secret - Windows Batch Script
@echo off
echo ================================================================================
echo JWT SECRET GENERATOR
echo ================================================================================
echo.
echo Generating a secure Base64-encoded JWT secret...
echo.
echo Option 1: Use the PowerShell script
echo Run: powershell -ExecutionPolicy Bypass -File generate-jwt-secret.ps1
echo.
echo Option 2: Use an online generator
echo Visit: https://generate-random.org/encryption-key-generator
echo Select: 512-bit, Format: Base64, Click Generate
echo.
echo Option 3: Generate manually with this PowerShell one-liner
echo Copy and paste this into PowerShell:
echo.
echo $bytes = New-Object byte[] 64
echo [System.Security.Cryptography.RNGCryptoServiceProvider]::new().GetBytes($bytes)
echo $secret = [Convert]::ToBase64String($bytes)
echo Write-Host "JWT_SECRET=$secret"
echo.
echo ================================================================================
echo TEMPORARY FIX: Your app will now work with plain text secrets
echo but for PRODUCTION use a proper Base64-encoded secret!
echo ================================================================================
pause

