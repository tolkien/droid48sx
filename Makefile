# Filename: Makefile
# Copyright (C) 2012 Olivier Sirol <czo@free.fr>
# License: GPL (http://www.gnu.org/copyleft/gpl.html)
# Started: Jan 2012
# Last Change: Sunday 26 January 2014, 17:40
# Edit Time: 2:25:52
# Description:
#
# $Id: $
#

release:
	perl -i -pe 'BEGIN {$$A=`git describe --long`; chomp $$A} END ; s:<string name="build_tag">.*</string>:<string name="build_tag">$$A</string>: ' res/values/strings.xml
	perl -i -pe 'BEGIN {$$A=`date`; chomp $$A} END ; s:<string name="build_date">.*</string>:<string name="build_date">$$A</string>: ' res/values/strings.xml
	ndk-build V=1
	./resupdate
	ant release
	scp bin/droid48sx-release.apk czo@ananas:/var/www
	@echo "<- done!"

debug:
	perl -i -pe 'BEGIN {$$A=`git describe --long`; chomp $$A} END ; s:<string name="build_tag">.*</string>:<string name="build_tag">$$A (DEBUG)</string>: ' res/values/strings.xml
	perl -i -pe 'BEGIN {$$A=`date`; chomp $$A} END ; s:<string name="build_date">.*</string>:<string name="build_date">$$A</string>: ' res/values/strings.xml
	ndk-build V=1 NDK_DEBUG=1
	./resupdate
	ant debug
	scp bin/droid48sx-debug.apk czo@ananas:/var/www
	@echo "<- done!"

ndk:
	ndk-build
	@echo "<- done!"

clean:
	ndk-build clean
	ndk-build NDK_DEBUG=1 clean
	ant clean
	rm -f res/drawable/k*
	rm -f res/drawable-large/k*
	rm -f res/drawable-large-hdpi/k*
	rm -f res/drawable-large-xhdpi/k*
	rm -f res/drawable-ldpi/k*
	rm -f res/drawable-xhdpi/k*
	rm -f res/drawable-xlarge/k*
	@echo "<- done!"

