# hiking-emergency-alert

![Static Badge](https://img.shields.io/badge/Type-Desktop_App-blue?link=https://en.wikipedia.org/wiki/Desktop_computer)
![Static Badge](https://img.shields.io/badge/Language-Java_17-darkgreen?link=https://openjdk.org/)
![Static Badge](https://img.shields.io/badge/UI_System-Swing-darkgreen?link=https://docs.oracle.com/javase/tutorial/uiswing/index.html)
![Static Badge](https://img.shields.io/badge/Application_JAR-1.86_MB-darkgreen)

![Static Badge](https://img.shields.io/github/license/fritzthecap/hiking-emergency-alert?color=pink)
![GitHub Created At](https://img.shields.io/github/created-at/fritzthecap/hiking-emergency-alert?color=pink)
![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/fritzthecap/hiking-emergency-alert?color=pink)
![GitHub repo size](https://img.shields.io/github/repo-size/fritzthecap/hiking-emergency-alert?color=pink)
![GitHub last commit](https://img.shields.io/github/last-commit/fritzthecap/hiking-emergency-alert?color=pink)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/fritzthecap/hiking-emergency-alert/total?color=pink)
 
A desktop application that runs at home while you are hiking. It starts to send alert e-mails when you are overdue.

You need an e-mail account and Internet connection at your home. You run this application before you leave for a hike.
When you are not back within the scheduled time and push the "Home Again" button, it will start to send alert e-mails to the mail contacts you configured.
The first contact will receive it at your scheduled home-time. If this contact does not send a reply mail within an hour, the next contact will be alerted.
This goes on until either a contact replied, or there are no more contacts. Of course all intervals and mail texts are configurable.

It makes sense that you configure yourself as first contact when you have a mobile phone that can send and receive e-mails. That way you can prevent alert mails using your phone if you are just late but not helpless in a ditch without phone connection.

Starting with version **1.2** you can stop the alert chain at any time by replying to the mail that gets sent to you on activation, see [issue #15](https://github.com/fritzthecap/hiking-emergency-alert/issues/15). 

Starting with version **1.3** you can create additional hike-days on route-and-times page, see [issue #1](https://github.com/fritzthecap/hiking-emergency-alert/issues/1). Each day has its own route description, including images. There is only one start-date/time, but every day has its own end-date/time. The mail communication would be the same on each day. These hike-days are not bound to natural days, more precisely they are "hike periods".

Starting with version **1.9** you can activate the hike observation remotely, see [issue #28](https://github.com/fritzthecap/hiking-emergency-alert/issues/28).
On last "Forward" click you will be asked whether you want to activate the hike "Now" or "Later". If you click "Later", you can activate it when you safely arrived at your hiking trail. You will receive a reminder mail, and answering it would activate the hike observation. 

**IMPORTANT:** Any of these reply mails must contain the received MAIL-ID either in content-text or in the attached original mail!

Supported languages:
- German
- English
- French
- Italian
- Spanish

# Install

To use this application you need at least **Java 17** installed on your computer. Java is available for nearly any operating-system.
You can download it from the [Open Java website](https://jdk.java.net/25/), or [from Oracle](https://www.oracle.com/de/java/technologies/downloads/). If you are on a 32-bit platform, the [bellsoft JDK downloads](https://fritzthecat-blog.blogspot.com/2026/02/installing-java-17-on-32-bit-linux.html)  may help you.
As soon as you have installed the Java Runtime Environment, make sure the _java_ interpreter is in your execution PATH and [download hiking-emergency-alert.jar](https://github.com/fritzthecap/hiking-emergency-alert/raw/refs/heads/main/hiking-emergency-alert.jar). Then launch

    java -jar hiking-emergency-alert.jar

Following JVM-argument would give you bigger fonts:

    java -DfontPercent=120 -jar hiking-emergency-alert.jar

The application stores your hike-data by default in _$HOME/hiking-emergency-alert/hike.json_ directory.
This directory can be changed by adding commandline argument _-Dhike.home=/mydirectory_, the name of the file can be changed by  _-Dhike.file=myhike.json_.

You can also pass any mail-property to the application via _-D...._ commandline arguments, e.g. _-Dmail.smtp.ssl.enable=true_ or _-Dmail.smtp.port=465_.

If you call the application with a commandline argument (e.g. "_java -jar hiking-emergency-alert.jar myhike.json_"),
it will execute the given JSON file without opening a graphical user-interface.
But I bet you will lose patience on editing the long JSON text lines (JSON does not support line breaks:-).

To get rid of the application, delete the _hiking-emergency-alert.jar_ file and the _hiking-emergency-alert_ directory. There are no registry entries or other magic tricks.

# Try Out

You can test the aplication easily by providing your own e-mail address several times in contacts list, choose different names. The shortest hike is 2 minutes, because the shortest polling interval is 1 minute, and the hike must be longer than that.

You would get an activation mail immedately after activation of the hike. If you respond to it, no alert mails would be sent. If not, alerts would be sent to all contacts (that would be you). Mind that a "passing-to-next" mail would never be sent to a contact that has the same mail address as the next contact.

<img width="517" height="61" alt="write-to-email" src="https://github.com/user-attachments/assets/8829f9c5-6ef6-4cc1-822d-d94ca6133d44" />

----

# Tech Notes

This project started 2026-01-01 and had its first release 2026-02-02.
It was implemented using _Java 21_ (compatible with 17), the UI is good old _Swing_.
Persisted hike data are in _JSON_ format (_gson 2.13.2_).
E-mail goes with _jakarta-mail 2.1.5_ and _angus-mail 2.0.5_ libraries.

For German readers, I have written a [Blog article](https://fritzthecat-retired.blogspot.com/2025/12/wandern-notfallmeldung.html) about this idea.

----

Below you see a simplified **UML activity-diagram** of a hike.

<img width="658" height="825" alt="hiking-emergency-alert_states" src="https://github.com/user-attachments/assets/73278624-7f67-4b3d-a16f-3c1f15f51712" />

There is also a more precise **[State/Transition-Table](https://html-preview.github.io/?url=https://github.com/fritzthecap/hiking-emergency-alert/blob/main/src/main/resources/fri/servers/hiking/emergencyalert/statemachine/state-transition-diagram.html)**.

# Screenshots

Here are screenshots of all wizard-pages of the user-interface:

<img width="1028" height="629" alt="hiking-LanguageAndFiles" src="https://github.com/user-attachments/assets/d64185fa-c84c-46df-bc4e-66a9e6bad388" />

----

<img width="1028" height="629" alt="hiking-Contacts" src="https://github.com/user-attachments/assets/7bbb6049-6035-42c4-98c6-feaea3b118e1" />

----

<img width="1028" height="629" alt="hiking-MailTexts" src="https://github.com/user-attachments/assets/b1ce8f98-ed2a-4cf5-86d2-8d2207b24c58" />

----

<img width="1028" height="629" alt="hiking-MailConfiguration" src="https://github.com/user-attachments/assets/b3b4d489-c276-4fc5-ab24-f535b549ecbc" />

----

<img width="477" height="698" alt="hiking-SecureMailProperties" src="https://github.com/user-attachments/assets/f6c283a4-7de8-4731-8145-a9442e0de18e" />

----

<img width="1028" height="629" alt="hiking-TimesAndRoute" src="https://github.com/user-attachments/assets/6b7dc3b2-66f2-4675-81d0-b232701374ac" />

----

<img width="1028" height="629" alt="hiking-Activation" src="https://github.com/user-attachments/assets/0c9d4150-9a74-4b6a-99a4-c4ba1d08fbc9" />

<img width="993" height="597" alt="hiking-ActivationDialog" src="https://github.com/user-attachments/assets/e82d0ac5-307b-4e98-af01-b5c6711d46e6" />

----

<img width="1028" height="629" alt="hiking-Observation" src="https://github.com/user-attachments/assets/b242e2e3-9b43-4e62-86af-96b6da75f30e" />





