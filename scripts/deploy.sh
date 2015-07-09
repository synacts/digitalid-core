#!/bin/sh

scp /Users/kasparetter/Documents/Career/DigitalID/Code/DigitalID/dist/DigitalID-Server.jar vid@idserver:~/DigitalID-Server.jar
scp /Users/kasparetter/Documents/Career/DigitalID/Code/Service/FileService/dist/FileService.jar vid@idserver:~/.DigitalID/Services/FileService.jar
ssh vid@idserver "screen -S DigitalID -p 0 -X stuff \"0$(printf \\r)\"; screen -d -m -S DigitalID java -jar DigitalID-Server.jar;"

scp /Users/kasparetter/Documents/Career/DigitalID/Code/Desktop/Contacts/dist/Desktop-Contacts-Bundled.jar root@webserver:/web/digitalid/webroot/download/Contacts.jar
scp /Users/kasparetter/Documents/Career/DigitalID/Code/DigitalID/dist/DigitalID-Client.jar root@webserver:/web/digitalid/webroot/libraries/DigitalID-Client.jar
scp /Users/kasparetter/Documents/Career/DigitalID/Code/DigitalID/dist/DigitalID-Server.jar root@webserver:/web/digitalid/webroot/libraries/DigitalID-Server.jar
scp /Users/kasparetter/Documents/Career/DigitalID/Code/DigitalID/dist/DigitalID.jar root@webserver:/web/digitalid/webroot/libraries/DigitalID.jar
scp /Users/kasparetter/Documents/Career/DigitalID/Code/Service/FileService/dist/FileService-Client.jar root@webserver:/web/digitalid/webroot/libraries/FileService-Client.jar
scp /Users/kasparetter/Documents/Career/DigitalID/Code/Service/FileService/dist/FileService.jar root@webserver:/web/digitalid/webroot/libraries/FileService.jar
scp /Users/kasparetter/Documents/Career/DigitalID/Code/Desktop/General/dist/Desktop-General.jar root@webserver:/web/digitalid/webroot/libraries/Desktop-General.jar
scp /Users/kasparetter/Documents/Career/DigitalID/Code/Android/General/dist/Android-General.jar root@webserver:/web/digitalid/webroot/libraries/Android-General.jar
scp /Users/kasparetter/Documents/Career/DigitalID/Code/Android/General/dist/DigitalID-Android.jar root@webserver:/web/digitalid/webroot/libraries/DigitalID-Android.jar
