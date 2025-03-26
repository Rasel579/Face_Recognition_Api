#!/bin/bash

BOT_TOKEN=${API_TELEGRAM}
CHAT_WITH_USER_ID=${TELEGRAM_USER_ID}

if [ "$CI_JOB_STATUS" == "success" ]; then
  MESSAGE="$CI_JOB_STAGE ✅"
else
  MESSAGE="$CI_JOB_STAGE 🚫"
fi

curl -s -X POST https://api.telegram.org/bot${BOT_TOKEN}/sendMessage -d chat_id=${CHAT_WITH_USER_ID} -d text="${MESSAGE}" -d parse_mode="html"
