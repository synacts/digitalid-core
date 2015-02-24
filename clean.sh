#!/bin/sh

rm -Rf ~/File\ Service/External/*
rm -Rf ~/.DigitalID/Logs/*
rm -Rf ~/.DigitalID/Data/FileService.db
rm -Rf ~/.DigitalID/Data/FilesMirrored.db

mysql -u root -p-ro48OT* -D digitalid -e 'DROP DATABASE digitalid;' > /dev/null 2>&1
