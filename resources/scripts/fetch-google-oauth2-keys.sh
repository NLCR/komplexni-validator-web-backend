#!/bin/bash
# Fetch the Google OAuth2 public keys and save them to a local file
# https://www.googleapis.com/oauth2/v1/certs
# should be run regularly (cron) to keep the keys up to date

remote_url="https://www.googleapis.com/oauth2/v1/certs"
local_file="$HOME/.komplexni-validator/google-oauth2-public-keys.pem"

# Use curl to fetch the remote file and overwrite the local file
curl -o "$local_file" -L "$remote_url"

# Check if the curl command was successful (exit code 0) or not
if [ $? -eq 0 ]; then
    echo "File fetched and saved successfully."
else
    echo "Error: Unable to fetch the file."
fi
