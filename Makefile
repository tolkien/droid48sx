# Filename: Makefile
# Author: Olivier Sirol <czo@free.fr>
# License: GPL-2.0 (http://www.gnu.org/copyleft)
# File Created: Jan 2012
# Last Modified: samedi 14 mai 2022, 11:44
# Edit Time: 4:08:28
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

debug:
	./gradlew assembleDebug --warning-mode all
	@echo "<- debug done!"

scp: release
	scp ./app/build/outputs/apk/release/org.czo.droid48sx-`date +%Y%m%d`-release.apk czo@ananas:/tank/data/czo/www/ananas.czo.wf/intranet/download/apk

re: clean debug
	@echo "<- rebuild done!"

clean:
	./gradlew clean
	rm -fr app/libs
	rm -fr app/release
	rm -fr app/.externalNativeBuild
	rm -fr app/.cxx
	@echo "<- clean done!"

realclean: clean
	rm -fr .gradle
	rm -fr .idea
	rm -f ./*.iml ./app/app.iml
	@echo "<- realclean done!"

