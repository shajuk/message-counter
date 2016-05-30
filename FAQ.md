---
title: message-counter FAQs
---

Frequently Asked Questions
==========================

##1. Sent Message count mismatch / Does not count
It has been noticed that at times, the sent count is not updated correctly by the app. 

We use a background service to track whenever you send an SMS. Android kills background services and apps when there is a need for memory. This is a known Android feature and SMSes sent during this time before the service was restarted will not be counted. 

It has been noted that if you use a **_task killer or memory cleaner_**, the background service may get stopped.

1. First of all make sure that you have enabled the "Count Sent Messages" in the application settings. Once all set, you should see more details in the "Counter" page. At this point, the background service that counts sent messages should be started.

2. You can make sure that the service is running by going to Settings -> Apps -> Running and check if Message Counter service is running. If it is, your sent messages should be counted correctly.

3. We have only tested this app in a generic list of devices, ROMS and firmwares. If by any chance your specific device does not support any feature required by the app, the sent count may not be displayed correctly.

##2. Received count is displayed correctly, while sent count is not
Received count is displayed from the messages database stored in your phone, while the sent message counts are calculated by a service which can only count messages that are sent once it is started. See FAQ#1 or send a message and check back to see if it is updated correctly

##3. Incorrect translations
If you notice any incorrect translations, kindly report with the correct translations so this can be updated. The developer is not native to the additional languages supported by this application and uses Google Translate for translation purposes. Kindly send an email to [aeapplabs-support at live.in](sendto:aeapplabs-support@live.in)

##4. Permissions and Privacy Concerns
Your privacy is very important to us. We do not use collect or use any personal information by this app. The following permissions are requested
* Contacts - To display the contact name and image in the received page
* Messaging - To count the number of messages you have received
* Run At Startup - If you have enabled the "count sent messages" feature, the app will run a background service to keep track of the sent messages count

We do not request the Internet permission, so there is no communication from the app to the outside world.

[Privacy Policy](https://www.google.com/url?q=http://bit.ly/14rEIKa&sa=D&usg=AFQjCNGL0h4lmW0zxJJXVUvGpbC42_j-xQ)
