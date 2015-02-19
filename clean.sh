#!/bin/sh

rm -Rf ~/File\ Service/External/*
rm -Rf ~/.VirtualID/Logs/*
rm -Rf ~/.VirtualID/Data/FileService.db

mysql -u root -p-ro48OT* -D virtualid -e 'DROP DATABASE virtualid;' > /dev/null 2>&1
cp ~/Documents/Beruf/VirtualID/Code/Services/FileService/dist/FileService.jar ~/.VirtualID/Services/FileService.jar
