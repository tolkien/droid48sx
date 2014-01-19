# Filename: Makefile
# Copyright (C) 2012 Olivier Sirol <czo@free.fr>
# License: GPL (http://www.gnu.org/copyleft/gpl.html)
# Started: Jan 2012
# Last Change: Sunday 19 January 2014, 11:50
# Edit Time: 1:28:26
# Description:
#
# $Id: $
#

all:
	ndk-build V=1
	./resupdate
	ant release
	scp bin/droid48sx-release.apk czo@ananas:/var/www
	@echo "<- done!"

debug:
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

