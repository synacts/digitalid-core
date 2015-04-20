#!/bin/sh

sudo rm -Rf ~/File\ Service\ Local/External/*
sudo rm -Rf ~/File\ Service\ Mirror/External/*
sudo rm -Rf ~/File\ Service\ Terminal/External/*
# rm -Rf ~/.DigitalID/Logs/*
rm -Rf ~/.DigitalID/Data/Local.db
rm -Rf ~/.DigitalID/Data/Local.db-journal
rm -Rf ~/.DigitalID/Data/Mirror.db
rm -Rf ~/.DigitalID/Data/Mirror.db-journal
rm -Rf ~/.DigitalID/Data/Terminal.db
rm -Rf ~/.DigitalID/Data/Terminal.db-journal

mysql -u root -p-ro48OT* -D digitalid -e 'DROP DATABASE digitalid;' > /dev/null 2>&1
