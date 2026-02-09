# hiking-emergency-alert

**CAUTION:** secure mail properties handling is buggy in 1.0, fix is on the way, use the coming version 1.2 !

A desktop application that runs at home while you are hiking. It starts to send alert e-mails when you are overdue.

You need an e-mail account and Internet connection at your home. You run this application before you leave for a hike.
When you are not back within the scheduled time and push the "Home Again" button, it will start to send alert e-mails to the mail contacts you configured.
The first contact will receive it at your scheduled home-time. If this contact does not send a response mail within an hour, the next contact will be alerted.
This goes on until either a contact responded, or there are no more contacts. Of course all intervals and mail texts are configurable.

It makes sense that you configure yourself as first contact. That way you can stop the alert chain when you are just late but not helpless in a ditch without phone connection. That will work in case you have your phone with you and can receive and send e-mail on it. The response e-mail must either contain the original e-mail as attachment, or the MAIL-ID that can be found in the original's content text.

Supported languages:
- German
- English
- French
- Italian
- Spanish

To use this application you need at least Java 17 installed on your computer. This is freely available on the [Open Java website](https://jdk.java.net/25/), or [from Oracle](https://www.oracle.com/de/java/technologies/downloads/).
As soon as you have installed the Java Runtime Environment and downloaded _hiking-emergency-alert.jar_, launch

    java -jar hiking-emergency-alert.jar

The application stores your hike-data by default in _$HOME/hiking-emergency-alert_ directory, that gets created as soon as you once have configured your mail connection successfully. You can generate different hikes and load them on the first page of the "wizard", see screenshots below.

----

This project started 2026-01-01 and had its first release 2026-02-02.
It was implemented using _Java 21_ (compatible with 17), the UI is good old _Swing_.
Persisted hike data are in _JSON_ format (_gson 2.13.2_).
E-mail goes with _jakarta-mail 2.1.5_ and _angus-mail 2.0.5_ libraries.

For German readers, I have written a [Blog article](https://fritzthecat-retired.blogspot.com/2025/12/wandern-notfallmeldung.html) about this idea.

----

Screenshots:

<img width="900" height="600" alt="Hiking-Emergency-Alert_Language" src="https://github.com/user-attachments/assets/7ec48e7c-0811-4208-b1fb-4060d69e4cda" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_Contacts" src="https://github.com/user-attachments/assets/e66c028e-8ed5-445e-9a57-4ec11a35154f" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_MailTexts" src="https://github.com/user-attachments/assets/7caeac1b-1d19-49ac-9ee6-e358888e1bf5" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_MailConfiguration" src="https://github.com/user-attachments/assets/4f2f1db6-bdd8-45ba-a5b9-73382e5a663f" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_Route" src="https://github.com/user-attachments/assets/25308d9d-3160-41bb-8fc2-2bfee98cced3" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_Activation" src="https://github.com/user-attachments/assets/65430141-2819-4f68-b993-8593e13dec96" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_Observation" src="https://github.com/user-attachments/assets/20fb8b18-0134-4b8e-bf23-5e3bf6a3f5a0" />

----
