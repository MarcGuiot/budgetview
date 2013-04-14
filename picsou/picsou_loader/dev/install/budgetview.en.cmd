rem -Dbudgetview.jar.path=path_to_jar (the new version are dowloaded in this directory)
rem -Dbudgetview.data.path=path_to_data (can be a shared folder)

java -Xmx128m -Dbudgetview.data.path=%cd% -Dbudgetview.jar.path=%cd% -Dbudgetview.exe.dir=%cd% -cp %cd%\budgetviewloader-1.0.jar com.budgetview.Main -l fr "$@"

