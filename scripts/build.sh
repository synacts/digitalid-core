#!/bin/sh

/Applications/NetBeans/NetBeans\ 8.0.2.app/Contents/Resources/NetBeans/extide/ant/bin/ant -f /Users/kasparetter/Documents/Career/DigitalID/Code/Desktop/Contacts clean jar > /dev/null 2>&1;

mkdir -p ~/.DigitalID/Services;
cp -Rf /Users/kasparetter/Documents/Career/DigitalID/Code/Service/FileService/dist/FileService.jar ~/.DigitalID/Services/FileService.jar


/Applications/NetBeans/NetBeans\ 8.0.2.app/Contents/Resources/NetBeans/extide/ant/bin/ant -f /Users/kasparetter/Documents/Career/DigitalID/Code/Android/General clean jar > /dev/null 2>&1;

cp -Rf /Users/kasparetter/Documents/Career/DigitalID/Code/Android/General/dist/DigitalID-Android.jar /Users/kasparetter/Documents/Career/DigitalID/Code/Android/Test/app/libs/DigitalID-Android.jar
cp -Rf /Users/kasparetter/Documents/Career/DigitalID/Code/Android/General/dist/DigitalID-Android.jar /Users/kasparetter/Documents/Career/DigitalID/Code/Android/Contacts/Jarvis/app/libs/DigitalID-Android.jar
