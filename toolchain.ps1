function Resolve-AncJdk([string]$Requested) {
 $candidates=@($Requested,$env:JAVA_HOME,'C:\Program Files\Android\Android Studio\jbr')
 foreach($candidate in $candidates){if($candidate -and (Test-Path "$candidate\bin\javac.exe")){return $candidate}}
 throw 'JDK required: pass -Jdk with a JDK 17 or newer directory, or set JAVA_HOME.'
}
