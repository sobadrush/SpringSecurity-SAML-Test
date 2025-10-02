#!/bin/bash

# SAML Key and Certificate Generator
# This script generates a self-signed certificate and private key for SAML authentication
# These files should NOT be committed to version control

echo "==================================="
echo "SAML Key and Certificate Generator"
echo "==================================="
echo ""

# Define paths
SAML_DIR="src/main/resources/saml"
KEY_FILE="$SAML_DIR/saml-key.pem"
CERT_FILE="$SAML_DIR/saml-cert.crt"

# Create directory if it doesn't exist
if [ ! -d "$SAML_DIR" ]; then
    echo "Creating directory: $SAML_DIR"
    mkdir -p "$SAML_DIR"
fi

# Check if files already exist
if [ -f "$KEY_FILE" ] || [ -f "$CERT_FILE" ]; then
    echo "⚠️  Warning: SAML key or certificate already exists!"
    echo ""
    echo "Existing files:"
    [ -f "$KEY_FILE" ] && echo "  - $KEY_FILE"
    [ -f "$CERT_FILE" ] && echo "  - $CERT_FILE"
    echo ""
    read -p "Do you want to overwrite them? (yes/no): " -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy][Ee][Ss]$ ]]; then
        echo "Operation cancelled."
        exit 0
    fi
fi

# Generate private key and self-signed certificate
echo "Generating private key and self-signed certificate..."
echo ""

# Prevent MSYS2/Git Bash path conversion from breaking the -subj value on Windows
MSYS2_ARG_CONV_EXCL="*" openssl req -x509 -newkey rsa:2048 -keyout "$KEY_FILE" -out "$CERT_FILE" -days 365 -nodes \
    -subj "/C=TW/ST=Taiwan/L=Taipei/O=Example/OU=IT/CN=localhost"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Successfully generated:"
    echo "  - Private Key: $KEY_FILE"
    echo "  - Certificate: $CERT_FILE"
    echo ""
    echo "⚠️  IMPORTANT SECURITY NOTES:"
    echo "  1. These files contain sensitive cryptographic material"
    echo "  2. They are already in .gitignore and should NOT be committed to Git"
    echo "  3. For production, use certificates from a trusted Certificate Authority"
    echo "  4. Keep your private key secure and never share it"
    echo ""
    echo "Valid for: 365 days"
    echo ""
else
    echo ""
    echo "❌ Error: Failed to generate keys"
    exit 1
fi
