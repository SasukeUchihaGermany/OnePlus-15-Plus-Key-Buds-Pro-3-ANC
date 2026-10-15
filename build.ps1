param([string]$Sdk="$env:LOCALAPPDATA\Android\Sdk",[string]$Jdk="")
$ErrorActionPreference='Stop'
$project=Split-Path -Parent $MyInvocation.MyCommand.Path
. "$project\toolchain.ps1"
$Jdk=Resolve-AncJdk $Jdk
$env:JAVA_HOME=$Jdk
$env:Path="$Jdk\bin;$env:Path"
$bt="$Sdk\build-tools\36.1.0"
$android="$Sdk\platforms\android-36\android.jar"
$build="$project\.build"
$key="$project\.local\local.p12"
New-Item -ItemType Directory -Force -Path "$build\classes","$build\dex" | Out-Null
Get-ChildItem "$build\classes" -Recurse -File -Filter '*.class' | ForEach-Object {Remove-Item -LiteralPath $_.FullName}
function Check {if($LASTEXITCODE -ne 0){throw "Build command failed: $LASTEXITCODE"}}
$sources=Get-ChildItem "$project\src" -Recurse -Filter '*.java' | ForEach-Object FullName
$libraries=Get-ChildItem "$project\libs" -Filter '*.jar' | ForEach-Object FullName
$classpath=$android+';'+($libraries -join ';')
& "$Jdk\bin\javac.exe" -encoding UTF-8 -source 8 -target 8 -classpath $classpath -d "$build\classes" $sources
Check
& "$Jdk\bin\jar.exe" cf "$build\classes.jar" -C "$build\classes" .
Check
& "$bt\d8.bat" --lib $android --min-api 31 --output "$build\dex" "$build\classes.jar" $libraries
Check
& "$bt\aapt2.exe" link -I $android --manifest "$project\AndroidManifest.xml" -o "$build\unsigned.apk"
Check
Copy-Item "$build\dex\classes.dex" "$build\classes.dex" -Force
Push-Location $build
try { & "$Jdk\bin\jar.exe" uf unsigned.apk classes.dex; Check } finally {Pop-Location}
& "$bt\zipalign.exe" -f 4 "$build\unsigned.apk" "$build\aligned.apk"
Check
if(!(Test-Path $key)){
 New-Item -ItemType Directory -Force "$project\.local" | Out-Null
 & "$Jdk\bin\keytool.exe" -genkeypair -keystore $key -storepass android -keypass android -alias local -dname 'CN=Local ANC Lab' -keyalg RSA -validity 3650
 Check
}
& "$bt\apksigner.bat" sign --ks $key --ks-pass pass:android --out "$project\BudsAncLab.apk" "$build\aligned.apk"
Check
& "$bt\apksigner.bat" verify "$project\BudsAncLab.apk"
Check
Write-Output "Built $project\BudsAncLab.apk"
