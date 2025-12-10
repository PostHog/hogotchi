#!/usr/bin/env python3
"""
Send test push notifications to Hogotchi app.

Usage:
    python test_notifications.py [notification_type]

Notification types:
    feed    - "Max is hungry!" notification
    play    - "Playtime?" notification
    sleep   - "Sleepy hog" notification
    all     - Send all three with delays
"""

import sys
import json
import time
from pathlib import Path

import jwt
import requests

# Configuration
SERVICE_ACCOUNT_PATH = Path("/Users/mattbrooker/Downloads/posthog-7d74b-firebase-adminsdk-fbsvc-f742855424.json")
FCM_TOKEN = "eCgCbWK9Tmi3tJPp7fpSVA:APA91bEde4-K9uSL9S5dnuls9vby8s5uNK5gOpFD-DcgOFZa9aOdmuCIDqztGFSNO6N03zOdOuORNTQXuIbopdrmEc_Kkd6fWDaCv_M1o0FtsXZf59eCR_8"

# Hogotchi notification templates
NOTIFICATIONS = {
    "feed": {
        "title": "Max is hungry!",
        "body": "Feed your hog before it's too late!",
        "action": "feed"
    },
    "play": {
        "title": "Playtime?",
        "body": "Max wants to play with you!",
        "action": "play"
    },
    "sleep": {
        "title": "Sleepy hog",
        "body": "Max is getting tired...",
        "action": "sleep"
    },
    "critical": {
        "title": "Max needs you NOW!",
        "body": "Your hog is in critical condition!",
        "action": "feed"
    },
    "levelup": {
        "title": "Level Up!",
        "body": "Max reached a new level! Keep caring for your hog!",
        "action": ""
    }
}


def get_access_token(service_account: dict) -> str:
    """Generate a short-lived access token from service account credentials."""
    now = int(time.time())

    payload = {
        "iss": service_account["client_email"],
        "sub": service_account["client_email"],
        "aud": "https://oauth2.googleapis.com/token",
        "iat": now,
        "exp": now + 3600,
        "scope": "https://www.googleapis.com/auth/firebase.messaging",
    }

    signed_jwt = jwt.encode(
        payload,
        service_account["private_key"],
        algorithm="RS256",
    )

    response = requests.post(
        "https://oauth2.googleapis.com/token",
        data={
            "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
            "assertion": signed_jwt,
        },
    )

    if response.status_code != 200:
        print(f"Failed to get access token: {response.status_code}")
        print(response.text)
        sys.exit(1)

    return response.json()["access_token"]


def send_notification(project_id: str, access_token: str, notif_type: str) -> bool:
    """Send a Hogotchi notification."""
    notif = NOTIFICATIONS.get(notif_type)
    if not notif:
        print(f"Unknown notification type: {notif_type}")
        return False

    url = f"https://fcm.googleapis.com/v1/projects/{project_id}/messages:send"

    headers = {
        "Authorization": f"Bearer {access_token}",
        "Content-Type": "application/json",
    }

    payload = {
        "message": {
            "token": FCM_TOKEN,
            "notification": {
                "title": notif["title"],
                "body": notif["body"],
            },
            "data": {
                "action": notif["action"],
                "source": "hogotchi_test",
            },
        }
    }

    response = requests.post(url, headers=headers, json=payload)

    if response.status_code == 200:
        print(f"Sent: {notif['title']}")
        return True
    else:
        print(f"Failed to send {notif_type}: {response.status_code}")
        print(response.text)
        return False


def main():
    notif_type = sys.argv[1] if len(sys.argv) > 1 else "feed"

    if not SERVICE_ACCOUNT_PATH.exists():
        print(f"Service account not found: {SERVICE_ACCOUNT_PATH}")
        sys.exit(1)

    print("Loading Firebase credentials...")
    with open(SERVICE_ACCOUNT_PATH) as f:
        service_account = json.load(f)

    project_id = service_account["project_id"]
    print(f"Project: {project_id}")

    print("Getting access token...")
    access_token = get_access_token(service_account)
    print("Ready!\n")

    if notif_type == "all":
        print("Sending all notifications (5 second delays)...\n")
        print("Make sure Hogotchi is BACKGROUNDED to see notifications!\n")
        for ntype in ["feed", "play", "sleep"]:
            send_notification(project_id, access_token, ntype)
            time.sleep(5)
    elif notif_type == "list":
        print("Available notification types:")
        for name, notif in NOTIFICATIONS.items():
            print(f"  {name:10} - {notif['title']}")
    else:
        print(f"Sending '{notif_type}' notification...")
        print("Make sure Hogotchi is BACKGROUNDED to see the notification!\n")
        send_notification(project_id, access_token, notif_type)

    print("\nDone!")


if __name__ == "__main__":
    main()
