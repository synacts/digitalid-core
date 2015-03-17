#!/bin/sh

sudo rm -Rf ~/File\ Service/External/*
rm -Rf ~/.DigitalID/Logs/*
rm -Rf ~/.DigitalID/Data/FileService.db
rm -Rf ~/.DigitalID/Data/FileService.db-journal
rm -Rf ~/.DigitalID/Data/FileMirror.db
rm -Rf ~/.DigitalID/Data/FileMirror.db-journal
rm -Rf ~/.DigitalID/Data/Contacts.db
rm -Rf ~/.DigitalID/Data/Contacts.db-journal

mysql -u root -p-ro48OT* -D digitalid -e 'DROP DATABASE digitalid;' > /dev/null 2>&1
