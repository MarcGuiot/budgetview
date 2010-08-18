#
# Creation of the Mac DMG file based on a template located in
#             ./dev/dmg/dmg_template.dmg
#

if [ -a /Volumes/CashPilot ]
then
  echo "Unmounting CashPilot..."
  diskutil unmount CashPilot
fi

TMP_DMG=./target/CashPilot-tmp.dmg
OUT_DMG=./target/CashPilot-out.dmg

cp ./dev/dmg/dmg_template.dmg $TMP_DMG

open -W $TMP_DMG

cp -r ./CashPilot/CashPilot.app /Volumes/CashPilot/

diskutil unmount CashPilot

hdiutil attach $TMP_DMG -noautoopen -quiet -mountpoint ./target/dmg

hdiutil convert $TMP_DMG -format UDZO -imagekey zlib-level=9 -o OUT_DMG

hdiutil detach $TMP_DMG -quiet -force


