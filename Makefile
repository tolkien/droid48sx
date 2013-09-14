# Filename: Makefile
# Copyright (C) 2012 Olivier Sirol <czo@free.fr>
# License: GPL (http://www.gnu.org/copyleft/gpl.html)
# Started: Jan 2012
# Last Change: samedi 14 septembre 2013, 17:12
# Edit Time: 0:59:13
# Description:
#
# $Id: $
#


all:
	ndk-build
	./resupdate
	ant release
	@echo "<- done!"

clean:
	ndk-build clean
	ant clean
	rm -f res/drawable/k*
	rm -f res/drawable-large/k*
	rm -f res/drawable-large-hdpi/k*
	rm -f res/drawable-large-xhdpi/k*
	rm -f res/drawable-ldpi/k*
	rm -f res/drawable-xhdpi/k*
	rm -f res/drawable-xlarge/k*
	@echo "<- done!"

