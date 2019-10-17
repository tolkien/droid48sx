# Filename: Makefile
# Author: Olivier Sirol <czo@free.fr>
# License: GPL-2.0
# File Created: Jan 2012
# Last Modified: jeudi 17 octobre 2019, 21:30
# Edit Time: 4:03:38
# Description:
#
# $Id: $
#

release:
	./gradlew assembleRelease
	@echo "<- release done!"

#ant release
#./resupdate
#./resupdatedrawable

scp:
	scp ./app/build/outputs/apk/release/droid48sx-`date +%Y%m%d`-release.apk czo@ananas:/tank/data/czo/www/ananas.czo.wf/intranet/download/apk

debug:
	./gradlew assembleDebug
	@echo "<- debug done!"

re: clean debug
	@echo "<- rebuild done!"

clean:
	./gradlew clean
	rm -fr app/libs
	rm -fr app/release
	rm -fr app/.externalNativeBuild
	rm -fr app/.cxx
#	rm -f res/drawable/k*
#	rm -fr res/drawable-large
#	rm -fr res/drawable-large-hdpi
#	rm -fr res/drawable-large-xhdpi
#	rm -fr res/drawable-xlarge
#	rm -fr res/drawable-ldpi
#	rm -fr res/drawable-mdpi
#	rm -fr res/drawable-hdpi
#	rm -fr res/drawable-xhdpi
#	rm -fr res/drawable-xxhdpi
	@echo "<- clean done!"

realclean: clean
	rm -fr .gradle
	rm -fr .idea
	rm -f ./*.iml ./app/app.iml
	@echo "<- realclean done!"

