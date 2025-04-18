#!/usr/bin/env bash
set -e

AUTH=http://127.0.0.1:8071
CLIENT_ID=gateway-server
CLIENT_SECRET=secret
REDIRECT_URI=http://127.0.0.1:9999/callback      # «фиктивный» redirect
USER=john_doe
PASS=mySecurePass123

# 1) забираем CSRF с /login
curl -c cookie -s $AUTH/login -o login.html
CSRF=$(sed -n 's/.*name="_csrf" value="\([^"]*\)".*/\1/p' login.html)
echo $CSRF
# 2) логинимся
curl -b cookie -c cookie -s $AUTH/login \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "username=$USER" -d "password=$PASS" -d "_csrf=$CSRF" \
     -o /dev/null

# 3) запрашиваем code
curl -b cookie -c cookie -Ls -D headers.txt -o /dev/null \
  "$AUTH/oauth2/authorize?response_type=code&client_id=$CLIENT_ID&redirect_uri=$REDIRECT_URI&scope=openid&state=xyz"

CODE=$(awk '/Location: / {
              match($0, /code=([^&]+)/, m); if (m[1]) print m[1]
            }' headers.txt | tail -1)

# 4) Обмениваем code -> JWT
TOKEN=$(curl -u $CLIENT_ID:$CLIENT_SECRET -s $AUTH/oauth2/token \
         -H "Content-Type: application/x-www-form-urlencoded" \
         -d "grant_type=authorization_code" \
         -d "code=$CODE" \
         -d "redirect_uri=$REDIRECT_URI" | jq -r '.access_token')

echo "== ACCESS TOKEN =="
echo $TOKEN
