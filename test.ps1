param([string]$Jdk='')
$ErrorActionPreference='Stop'
$project=Split-Path -Parent $MyInvocation.MyCommand.Path
. "$project\toolchain.ps1"
$Jdk=Resolve-AncJdk $Jdk
$testBuild="$project\.build\test-classes"
New-Item -ItemType Directory -Force $testBuild | Out-Null
& "$Jdk\bin\javac.exe" -d $testBuild "$project\src\dev\local\anc\Anc.java" "$project\src\dev\local\anc\Protocol.java" "$project\src\dev\local\anc\Gestures.java" "$project\tests\CoreTest.java"
if($LASTEXITCODE -ne 0){throw 'Test compilation failed'}
& "$Jdk\bin\java.exe" -cp $testBuild dev.local.anc.CoreTest
if($LASTEXITCODE -ne 0){throw 'Tests failed'}
