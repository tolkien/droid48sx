# Filename: Makefile
# Copyright (C) 2012 Olivier Sirol <czo@free.fr>
# License: GPL (http://www.gnu.org/copyleft/gpl.html)
# Started: Jan 2012
# Last Change: jeudi 06 août 2015, 13:39
# Edit Time: 2:32:23
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
	cp bin/droid48sx-release.apk ../droid48sx-release-`date +%Y%m%d`.apk
#	scp bin/droid48sx-release.apk czo@ananas:/var/www
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
	rm -fr res/drawable-large
	rm -fr res/drawable-large-hdpi
	rm -fr res/drawable-large-xhdpi
	rm -fr res/drawable-xlarge
	rm -fr res/drawable-ldpi
	rm -fr res/drawable-mdpi
	rm -fr res/drawable-hdpi
	rm -fr res/drawable-xhdpi
	rm -fr res/drawable-xxhdpi
	@echo "<- done!"

