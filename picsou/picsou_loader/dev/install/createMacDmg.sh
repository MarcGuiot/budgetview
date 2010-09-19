#
# Creation of the Mac DMG file based on a template located in
#             ./dev/dmg/dmg_template.dmg
#

if [ -a /Volumes/BudgetView ]
then
  echo "Unmounting BudgetView..."
  diskutil unmount BudgetView
fi

TMP_DMG=./target/BudgetView-tmp.dmg
OUT_DMG=./target/BudgetView-out.dmg

cp ./dev/dmg/dmg_template.dmg $TMP_DMG

open -W $TMP_DMG

cp -r ./BudgetView/BudgetView.app /Volumes/BudgetView/

diskutil unmount BudgetView

hdiutil attach $TMP_DMG -noautoopen -quiet -mountpoint ./target/dmg

hdiutil convert $TMP_DMG -format UDZO -imagekey zlib-level=9 -o OUT_DMG

hdiutil detach $TMP_DMG -quiet -force


