# hiking-emergency-alert
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

----

This project started 2026-01-01 and had its first release 2026-02-02.
It was implemented using Java 21 (compatible with 17), the UI is good old Swing.
Persisted hike data are in JSON format (gson 2.13.2).
E-mail goes with jakarta mail 2.1.5 and angus-mail 2.0.5 libraries.

----

Screenshots:

<img width="900" height="600" alt="Hiking-Emergency-Alert_Language" src="https://github.com/user-attachments/assets/7ec48e7c-0811-4208-b1fb-4060d69e4cda" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_Contacts" src="https://github.com/user-attachments/assets/7c34a821-992d-41ac-9fda-a88b1da63156" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_MailTexts" src="https://github.com/user-attachments/assets/7caeac1b-1d19-49ac-9ee6-e358888e1bf5" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_MailConfiguration" src="https://github.com/user-attachments/assets/57b3fecd-2a2b-4eb5-85a1-2cea3d37babb" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_Route" src="https://github.com/user-attachments/assets/25308d9d-3160-41bb-8fc2-2bfee98cced3" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_Activation" src="https://github.com/user-attachments/assets/ee6a34ea-d76d-481b-870f-2c97ac4825da" />

----

<img width="900" height="600" alt="Hiking-Emergency-Alert_Observation" src="https://github.com/user-attachments/assets/20fb8b18-0134-4b8e-bf23-5e3bf6a3f5a0" />

----
